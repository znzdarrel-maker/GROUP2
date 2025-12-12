package dao;

import model.Tenant;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TenantDAO {

    private final String url = "jdbc:mysql://localhost/houserent";
    private final String user = "root";
    private final String pass = "";

    // Get database connection
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, user, pass);
        } catch (ClassNotFoundException ex) {
            throw new SQLException("MySQL Driver not found", ex);
        }
    }

    // ✅ NEW: Check if room can accommodate more tenants (Recommendation #2)
    public boolean canAddTenantToRoom(int roomNumber) {
        try {
            int capacity = getRoomCapacity(roomNumber);
            int currentTenants = countTenantsInRoom(roomNumber);
            
            return currentTenants < capacity;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ✅ NEW: Get room capacity from rooms table
    public int getRoomCapacity(int roomNumber) {
        String sql = "SELECT capacity FROM rooms WHERE room_number = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, String.valueOf(roomNumber));
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("capacity");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // ✅ NEW: Count current tenants in a room
    public int countTenantsInRoom(int roomNumber) {
        String sql = "SELECT COUNT(*) as tenant_count FROM records WHERE houseno = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, roomNumber);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("tenant_count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // ✅ NEW: Get list of tenant names in a specific room
    public List<String> getTenantNamesInRoom(int roomNumber) {
        List<String> names = new ArrayList<>();
        String sql = "SELECT name FROM records WHERE houseno = ? ORDER BY name";
        
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, roomNumber);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return names;
    }

    // Get room price by room number
    public double getRoomPrice(int roomNumber) {
        String sql = "SELECT price FROM rooms WHERE room_number = ?";

        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, roomNumber);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getDouble("price");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0.0;
    }

    // Get all tenants with gender
    public List<Tenant> getAllTenants() {
        List<Tenant> tenants = new ArrayList<>();
        String sql = "SELECT * FROM records ORDER BY houseno";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String gender = "N/A";
                try {
                    gender = rs.getString("gender");
                    if (gender == null) gender = "N/A";
                } catch (SQLException e) {
                    // Column doesn't exist in old database, use default
                }
                
                Tenant tenant = new Tenant(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("contact"),
                    rs.getInt("houseno"),
                    rs.getDouble("payment"),
                    rs.getString("month"),
                    gender
                );
                tenants.add(tenant);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return tenants;
    }

    // ✅ UPDATED: Add new tenant with capacity checking (Recommendation #2)
    public boolean addTenant(Tenant tenant) {
        String checkRoomSql = "SELECT status FROM rooms WHERE room_number = ?";
        
        String insertSql = "INSERT INTO records (name, contact, houseno, payment, month, gender) VALUES (?, ?, ?, ?, ?, ?)";
        String updateRoomSql = "UPDATE rooms SET status = 'Occupied' WHERE room_number = ?";

        try (Connection conn = getConnection()) {

            // 1. Check if room exists
            try (PreparedStatement pst = conn.prepareStatement(checkRoomSql)) {
                pst.setInt(1, tenant.getRoomNumber());
                ResultSet rs = pst.executeQuery();

                if (!rs.next()) {
                    System.out.println("Room does not exist!");
                    return false;
                }

                String status = rs.getString("status");
                // ✅ CHANGED: Allow adding to occupied rooms if capacity permits
                if ("Maintenance".equalsIgnoreCase(status) || "Under Repair".equalsIgnoreCase(status)) {
                    System.out.println("Room is under maintenance or repair!");
                    return false;
                }
            }

            // ✅ NEW: Check room capacity before adding tenant
            if (!canAddTenantToRoom(tenant.getRoomNumber())) {
                int capacity = getRoomCapacity(tenant.getRoomNumber());
                int current = countTenantsInRoom(tenant.getRoomNumber());
                System.out.println("Room is at full capacity! Current: " + current + " / Max: " + capacity);
                return false;
            }

            // 3. Insert tenant with gender
            try (PreparedStatement pst = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                pst.setString(1, tenant.getName());
                pst.setString(2, tenant.getContact());
                pst.setInt(3, tenant.getRoomNumber());
                pst.setDouble(4, tenant.getPayment());
                pst.setString(5, tenant.getMonth());
                pst.setString(6, tenant.getGender());

                int rowsAffected = pst.executeUpdate();

                if (rowsAffected > 0) {
                    // 4. Update room status to Occupied if it has tenants
                    try (PreparedStatement updatePst = conn.prepareStatement(updateRoomSql)) {
                        updatePst.setInt(1, tenant.getRoomNumber());
                        updatePst.executeUpdate();
                    }
                    
                    // 5. Ensure a default payment record exists for the current month
                    try {
                        dao.PaymentDAO paymentDAO = new dao.PaymentDAO();
                        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
                        paymentDAO.ensurePaymentRecordExistsForTenantAndMonth(
                            tenant.getName(), 
                            String.valueOf(tenant.getRoomNumber()), 
                            tenant.getPayment(), 
                            currentMonth
                        );
                    } catch (Exception ex) {
                        System.err.println("Warning: failed to create default payment record for new tenant: " + ex.getMessage());
                    }
                    
                    return true;
                }
            } catch (SQLException ex) {
                // If gender column doesn't exist, try without it
                System.out.println("Trying insert without gender column...");
                String insertSqlNoGender = "INSERT INTO records (name, contact, houseno, payment, month) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pst = conn.prepareStatement(insertSqlNoGender, Statement.RETURN_GENERATED_KEYS)) {
                    pst.setString(1, tenant.getName());
                    pst.setString(2, tenant.getContact());
                    pst.setInt(3, tenant.getRoomNumber());
                    pst.setDouble(4, tenant.getPayment());
                    pst.setString(5, tenant.getMonth());

                    int rowsAffected = pst.executeUpdate();

                    if (rowsAffected > 0) {
                        try (PreparedStatement updatePst = conn.prepareStatement(updateRoomSql)) {
                            updatePst.setInt(1, tenant.getRoomNumber());
                            updatePst.executeUpdate();
                        }
                        
                        try {
                            dao.PaymentDAO paymentDAO = new dao.PaymentDAO();
                            String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
                            paymentDAO.ensurePaymentRecordExistsForTenantAndMonth(
                                tenant.getName(), 
                                String.valueOf(tenant.getRoomNumber()), 
                                tenant.getPayment(), 
                                currentMonth
                            );
                        } catch (Exception ex2) {
                            System.err.println("Warning: failed to create default payment record: " + ex2.getMessage());
                        }
                        return true;
                    }
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    // Update tenant with gender
    public boolean updateTenant(Tenant tenant) {
        String sql = "UPDATE records SET name = ?, contact = ?, houseno = ?, payment = ?, month = ?, gender = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, tenant.getName());
            pst.setString(2, tenant.getContact());
            pst.setInt(3, tenant.getRoomNumber());
            pst.setDouble(4, tenant.getPayment());
            pst.setString(5, tenant.getMonth());
            pst.setString(6, tenant.getGender());
            pst.setInt(7, tenant.getTenantId());

            int rowsAffected = pst.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException ex) {
            // If gender column doesn't exist, try without it
            try (Connection conn = getConnection()) {
                String sqlNoGender = "UPDATE records SET name = ?, contact = ?, houseno = ?, payment = ?, month = ? WHERE id = ?";
                PreparedStatement pst = conn.prepareStatement(sqlNoGender);
                
                pst.setString(1, tenant.getName());
                pst.setString(2, tenant.getContact());
                pst.setInt(3, tenant.getRoomNumber());
                pst.setDouble(4, tenant.getPayment());
                pst.setString(5, tenant.getMonth());
                pst.setInt(6, tenant.getTenantId());

                int rowsAffected = pst.executeUpdate();
                return rowsAffected > 0;
            } catch (SQLException ex2) {
                ex2.printStackTrace();
                return false;
            }
        }
    }

    // ✅ UPDATED: Delete tenant and update room status if no tenants remain
    public boolean deleteTenantById(int tenantId) {
        String deleteSql = "DELETE FROM records WHERE id = ?";
        String deletePaymentsSql = "DELETE FROM payments WHERE tenant_name = ? AND room_number = ?";
        String updateRoomSql = "UPDATE rooms SET status = 'Available' WHERE room_number = ?";

        try (Connection conn = getConnection()) {
            // 1. Get tenant info before deleting
            int roomNumber = -1;
            String tenantName = "";
            String getRoomSql = "SELECT houseno, name FROM records WHERE id = ?";
            
            try (PreparedStatement pst = conn.prepareStatement(getRoomSql)) {
                pst.setInt(1, tenantId);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    roomNumber = rs.getInt("houseno");
                    tenantName = rs.getString("name");
                } else {
                    return false;
                }
            }

            // 2. Delete tenant from records
            try (PreparedStatement pst = conn.prepareStatement(deleteSql)) {
                pst.setInt(1, tenantId);
                int rowsAffected = pst.executeUpdate();
                
                if (rowsAffected > 0) {
                    // 3. Delete all payment records for this tenant
                    try (PreparedStatement deletePmt = conn.prepareStatement(deletePaymentsSql)) {
                        deletePmt.setString(1, tenantName);
                        deletePmt.setString(2, String.valueOf(roomNumber));
                        deletePmt.executeUpdate();
                        System.out.println("Deleted payment records for: " + tenantName);
                    }
                    
                    // 4. ✅ NEW: Only set room to Available if NO tenants remain
                    if (roomNumber > 0) {
                        int remainingTenants = countTenantsInRoom(roomNumber);
                        if (remainingTenants == 0) {
                            try (PreparedStatement updatePst = conn.prepareStatement(updateRoomSql)) {
                                updatePst.setInt(1, roomNumber);
                                updatePst.executeUpdate();
                                System.out.println("Room " + roomNumber + " set to Available (no tenants remaining)");
                            }
                        } else {
                            System.out.println("Room " + roomNumber + " still has " + remainingTenants + " tenant(s), keeping Occupied status");
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean deleteTenant(int roomNumber) {
        String deleteSql = "DELETE FROM records WHERE houseno = ?";
        String updateRoomSql = "UPDATE rooms SET status = 'Available' WHERE room_number = ?";

        try (Connection conn = getConnection()) {
            try (PreparedStatement pst = conn.prepareStatement(deleteSql)) {
                pst.setInt(1, roomNumber);
                int rowsAffected = pst.executeUpdate();

                if (rowsAffected > 0) {
                    try (PreparedStatement updatePst = conn.prepareStatement(updateRoomSql)) {
                        updatePst.setInt(1, roomNumber);
                        updatePst.executeUpdate();
                    }
                    return true;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    // Search tenants with gender
    public List<Tenant> searchTenants(String searchValue) {
        List<Tenant> tenants = new ArrayList<>();
        String sql = "SELECT * FROM records WHERE name LIKE ? OR contact LIKE ? OR houseno LIKE ? ORDER BY houseno";

        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchValue + "%";
            pst.setString(1, searchPattern);
            pst.setString(2, searchPattern);
            pst.setString(3, searchPattern);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String gender = "N/A";
                try {
                    gender = rs.getString("gender");
                    if (gender == null) gender = "N/A";
                } catch (SQLException e) {
                    // Column doesn't exist
                }
                
                Tenant tenant = new Tenant(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("contact"),
                    rs.getInt("houseno"),
                    rs.getDouble("payment"),
                    rs.getString("month"),
                    gender
                );
                tenants.add(tenant);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return tenants;
    }
}