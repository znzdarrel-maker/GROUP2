package dao;

import model.Payment;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PaymentDAO {
    
    private final String url = "jdbc:mysql://localhost/houserent";
    private final String user = "root";
    private final String pass = "";
    
    // ✅ Your table name from database
    private final String TABLE_NAME = "payments";
    
    // Get database connection
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, user, pass);
        } catch (ClassNotFoundException ex) {
            throw new SQLException("MySQL Driver not found", ex);
        }
    }
    
    // Helper to safely get string with default
    private String getStringOrDefault(ResultSet rs, String columnName, String defaultValue) {
        try {
            String value = rs.getString(columnName);
            return value != null ? value : defaultValue;
        } catch (SQLException e) {
            return defaultValue;
        }
    }
    
    // Helper to safely get double with default
    private double getDoubleOrDefault(ResultSet rs, String columnName, double defaultValue) {
        try {
            double v = rs.getDouble(columnName);
            if (rs.wasNull()) return defaultValue;
            return v;
        } catch (SQLException e) {
            return defaultValue;
        }
    }
    
    // Existing - Get all payments (unchanged)
    public List<Payment> getAllPayments() {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY payment_date DESC";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int id = rs.getInt("payment_id");
                String tenantName = getStringOrDefault(rs, "tenant_name", "N/A");
                String roomNumber = getStringOrDefault(rs, "room_number", "");
                double totalAmount = getDoubleOrDefault(rs, "total_amount", 0.0);
                double amountPaid = getDoubleOrDefault(rs, "amount_paid", 0.0);
                String paymentType = getStringOrDefault(rs, "payment_type", "Full Payment");
                double remainingBalance = getDoubleOrDefault(rs, "remaining_balance", 0.0);
                String month = getStringOrDefault(rs, "month", "N/A");
                Date dt = null;
                try {
                    dt = rs.getDate("payment_date");
                } catch (SQLException ex) { dt = null; }
                LocalDate paymentDate = dt != null ? dt.toLocalDate() : null;
                String status = getStringOrDefault(rs, "status", "Pending");
                String notes = getStringOrDefault(rs, "notes", "");
                
                Payment p = new Payment(id, tenantName, roomNumber, totalAmount, amountPaid, paymentType, remainingBalance, month, paymentDate, status, notes);
                payments.add(p);
            }
        } catch (SQLException ex) {
            System.err.println("Error loading payments: " + ex.getMessage());
            ex.printStackTrace();
        }
        return payments;
    }

    /**
     * NEW: Return a payment row for every tenant for the given month.
     * If a payment row for that tenant/month does not exist, create a default one:
     *  - amount_paid = 0
     *  - payment_type = "Full Payment"
     *  - remaining_balance = total_amount
     *  - payment_date = NULL
     *  - status = "Pending"
     *
     * Month format: used as-is. Recommended format: "MMMM yyyy" (e.g. "December 2025").
     */
   /**
 * Return a payment row for every tenant for the given month. 
 * If a payment row for that tenant/month does not exist, create a default one.
 */
public List<Payment> getPaymentsForMonth(String month) {
    List<Payment> payments = new ArrayList<>();
    String tenantSql = "SELECT id, name, houseno, payment FROM records ORDER BY houseno";
    String paymentLookupSql = "SELECT * FROM " + TABLE_NAME + " WHERE (tenant_name = ?  OR room_number = ?) AND month = ?  LIMIT 1";
    
    try (Connection conn = getConnection();
         PreparedStatement tenantPst = conn.prepareStatement(tenantSql);
         ResultSet trs = tenantPst.executeQuery()) {
        
        while (trs.next()) {
            String tenantName = trs. getString("name");
            int houseno = trs.getInt("houseno");
            String roomNumber = String.valueOf(houseno);
            double totalAmount = trs.getDouble("payment");
            
            // Check if payment exists for this tenant+month
            try (PreparedStatement pst = conn.prepareStatement(paymentLookupSql)) {
                pst.setString(1, tenantName);
                pst.setString(2, roomNumber);
                pst.setString(3, month);
                try (ResultSet rs = pst. executeQuery()) {
                    if (rs.next()) {
                        // Payment exists - add it
                        int id = rs.getInt("payment_id");
                        String tName = getStringOrDefault(rs, "tenant_name", tenantName);
                        String rNumber = getStringOrDefault(rs, "room_number", roomNumber);
                        double tot = getDoubleOrDefault(rs, "total_amount", totalAmount);
                        double amountPaid = getDoubleOrDefault(rs, "amount_paid", 0.0);
                        String paymentType = getStringOrDefault(rs, "payment_type", "Full Payment");
                        double remainingBalance = getDoubleOrDefault(rs, "remaining_balance", tot - amountPaid);
                        String m = getStringOrDefault(rs, "month", month);
                        Date dt = null;
                        try { dt = rs.getDate("payment_date"); } catch (SQLException ex) { dt = null; }
                        LocalDate paymentDate = dt != null ? dt. toLocalDate() : null;
                        String status = getStringOrDefault(rs, "status", "Pending");
                        String notes = getStringOrDefault(rs, "notes", "");
                        
                        Payment p = new Payment(id, tName, rNumber, tot, amountPaid, paymentType, remainingBalance, m, paymentDate, status, notes);
                        payments.add(p);
                        continue;
                    }
                }
            }
            
            // Payment doesn't exist - create default
            int generatedId = -1;
            String insertSql = "INSERT INTO " + TABLE_NAME + " (tenant_name, room_number, total_amount, amount_paid, payment_type, remaining_balance, month, payment_date, status, notes) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ins = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ins. setString(1, tenantName);
                ins.setString(2, roomNumber);
                ins.setDouble(3, totalAmount);
                ins.setDouble(4, 0.0);
                ins. setString(5, "Full Payment");
                ins.setDouble(6, totalAmount);
                ins.setString(7, month);
                ins.setNull(8, Types.DATE);
                ins.setString(9, "Pending");
                ins.setString(10, "");
                
                int rows = ins.executeUpdate();
                if (rows > 0) {
                    try (ResultSet gk = ins.getGeneratedKeys()) {
                        if (gk.next()) {
                            generatedId = gk.getInt(1);
                        }
                    }
                }
            } catch (SQLException ex) {
                System.err.println("Warning: could not persist default payment for tenant=" + tenantName + " :  " + ex.getMessage());
            }
            
            Payment p = new Payment(
                generatedId > 0 ? generatedId :  0,
                tenantName,
                roomNumber,
                totalAmount,
                0.0,
                "Full Payment",
                totalAmount,
                month,
                null,
                "Pending",
                ""
            );
            payments.add(p);
        }
        
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
    return payments;
}
    /**
     * Ensure a default payment record exists for the given tenant/roomNumber/month.
     * Returns true if a row existed or was created successfully.
     */
    public boolean ensurePaymentRecordExistsForTenantAndMonth(String tenantName, String roomNumber, double totalAmount, String month) {
        String paymentLookupSql = "SELECT payment_id FROM " + TABLE_NAME + " WHERE (tenant_name = ? OR room_number = ?) AND month = ? LIMIT 1";
        String insertSql = "INSERT INTO " + TABLE_NAME + " (tenant_name, room_number, total_amount, amount_paid, payment_type, remaining_balance, month, payment_date, status, notes) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection()) {
            try (PreparedStatement pst = conn.prepareStatement(paymentLookupSql)) {
                pst.setString(1, tenantName);
                pst.setString(2, roomNumber);
                pst.setString(3, month);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return true; // already exists
                    }
                }
            }
            // insert default
            try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                ins.setString(1, tenantName);
                ins.setString(2, roomNumber);
                ins.setDouble(3, totalAmount);
                ins.setDouble(4, 0.0);
                ins.setString(5, "Full Payment");
                ins.setDouble(6, totalAmount);
                ins.setString(7, month);
                ins.setNull(8, Types.DATE);
                ins.setString(9, "Pending");
                ins.setString(10, "");
                int rows = ins.executeUpdate();
                return rows > 0;
            } catch (SQLException ex) {
                System.err.println("Failed to insert default payment record: " + ex.getMessage());
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    // Other existing methods left unchanged...
    // addPayment, updatePayment, deletePayment, searchPayments, getPaymentsByTenant, getPaymentsByRoom, getTotalRevenue, getAllTenantNames, getRoomNumberByTenant
    // (kept below for brevity - they remain the same as in your original file)
    
    // Add new payment
    public boolean addPayment(Payment payment) {
        String sql = "INSERT INTO " + TABLE_NAME + 
                     " (tenant_name, room_number, total_amount, amount_paid, payment_type, remaining_balance, month, payment_date, status, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, payment.getTenantName());
            pst.setString(2, payment.getRoomNumber());
            pst.setDouble(3, payment.getTotalAmount());
            pst.setDouble(4, payment.getAmountPaid());
            pst.setString(5, payment.getPaymentType());
            pst.setDouble(6, payment.getRemainingBalance());
            pst.setString(7, payment.getMonth());
            if (payment.getPaymentDate() != null) pst.setDate(8, Date.valueOf(payment.getPaymentDate()));
            else pst.setNull(8, Types.DATE);
            pst.setString(9, payment.getStatus());
            pst.setString(10, payment.getNotes());
            
            int rowsAffected = pst.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException ex) {
            System.err.println("Error adding payment: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
    
    // Update payment - uses payment_id
    public boolean updatePayment(Payment payment) {
        String sql = "UPDATE " + TABLE_NAME + 
                     " SET tenant_name = ?, room_number = ?, total_amount = ?, amount_paid = ?, payment_type = ?, remaining_balance = ?, month = ?, payment_date = ?, status = ?, notes = ? " +
                     "WHERE payment_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, payment.getTenantName());
            pst.setString(2, payment.getRoomNumber());
            pst.setDouble(3, payment.getTotalAmount());
            pst.setDouble(4, payment.getAmountPaid());
            pst.setString(5, payment.getPaymentType());
            pst.setDouble(6, payment.getRemainingBalance());
            pst.setString(7, payment.getMonth());
            if (payment.getPaymentDate() != null) pst.setDate(8, Date.valueOf(payment.getPaymentDate()));
            else pst.setNull(8, Types.DATE);
            pst.setString(9, payment.getStatus());
            pst.setString(10, payment.getNotes());
            pst.setInt(11, payment.getId());
            
            int rowsAffected = pst.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    // Delete payment
    public boolean deletePayment(int paymentId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE payment_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, paymentId);
            int rowsAffected = pst.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    // Search payments
    public List<Payment> searchPayments(String searchValue) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + 
                     " WHERE tenant_name LIKE ? OR room_number LIKE ? OR month LIKE ? " +
                     "ORDER BY payment_date DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + searchValue + "%";
            pst.setString(1, searchPattern);
            pst.setString(2, searchPattern);
            pst.setString(3, searchPattern);
            
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("payment_id");
                String tenantName = getStringOrDefault(rs, "tenant_name", "N/A");
                String roomNumber = getStringOrDefault(rs, "room_number", "");
                double totalAmount = getDoubleOrDefault(rs, "total_amount", 0.0);
                double amountPaid = getDoubleOrDefault(rs, "amount_paid", 0.0);
                String paymentType = getStringOrDefault(rs, "payment_type", "Full Payment");
                double remainingBalance = getDoubleOrDefault(rs, "remaining_balance", 0.0);
                String month = getStringOrDefault(rs, "month", "N/A");
                Date dt = null;
                try {
                    dt = rs.getDate("payment_date");
                } catch (SQLException ex) { dt = null; }
                LocalDate paymentDate = dt != null ? dt.toLocalDate() : null;
                String status = getStringOrDefault(rs, "status", "Pending");
                String notes = getStringOrDefault(rs, "notes", "");
                
                Payment p = new Payment(id, tenantName, roomNumber, totalAmount, amountPaid, paymentType, remainingBalance, month, paymentDate, status, notes);
                payments.add(p);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return payments;
    }
    
    // Get payments by tenant name
    public List<Payment> getPaymentsByTenant(String tenantName) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + 
                     " WHERE tenant_name = ? ORDER BY payment_date DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, tenantName);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("payment_id");
                String tName = getStringOrDefault(rs, "tenant_name", "N/A");
                String roomNumber = getStringOrDefault(rs, "room_number", "");
                double totalAmount = getDoubleOrDefault(rs, "total_amount", 0.0);
                double amountPaid = getDoubleOrDefault(rs, "amount_paid", 0.0);
                String paymentType = getStringOrDefault(rs, "payment_type", "Full Payment");
                double remainingBalance = getDoubleOrDefault(rs, "remaining_balance", 0.0);
                String month = getStringOrDefault(rs, "month", "N/A");
                Date dt = null;
                try {
                    dt = rs.getDate("payment_date");
                } catch (SQLException ex) { dt = null; }
                LocalDate paymentDate = dt != null ? dt.toLocalDate() : null;
                String status = getStringOrDefault(rs, "status", "Pending");
                String notes = getStringOrDefault(rs, "notes", "");
                
                Payment p = new Payment(id, tName, roomNumber, totalAmount, amountPaid, paymentType, remainingBalance, month, paymentDate, status, notes);
                payments.add(p);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return payments;
    }
    
    // Get payments by room number
    public List<Payment> getPaymentsByRoom(String roomNumber) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + 
                     " WHERE room_number = ? ORDER BY payment_date DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, roomNumber);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("payment_id");
                String tName = getStringOrDefault(rs, "tenant_name", "N/A");
                String rNumber = getStringOrDefault(rs, "room_number", "");
                double totalAmount = getDoubleOrDefault(rs, "total_amount", 0.0);
                double amountPaid = getDoubleOrDefault(rs, "amount_paid", 0.0);
                String paymentType = getStringOrDefault(rs, "payment_type", "Full Payment");
                double remainingBalance = getDoubleOrDefault(rs, "remaining_balance", 0.0);
                String month = getStringOrDefault(rs, "month", "N/A");
                Date dt = null;
                try {
                    dt = rs.getDate("payment_date");
                } catch (SQLException ex) { dt = null; }
                LocalDate paymentDate = dt != null ? dt.toLocalDate() : null;
                String status = getStringOrDefault(rs, "status", "Pending");
                String notes = getStringOrDefault(rs, "notes", "");
                
                Payment p = new Payment(id, tName, rNumber, totalAmount, amountPaid, paymentType, remainingBalance, month, paymentDate, status, notes);
                payments.add(p);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return payments;
    }
    
    // Get total revenue
    public double getTotalRevenue() {
        String sql = "SELECT SUM(amount_paid) as total FROM " + TABLE_NAME;
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0.0;
    }
    
    // Get tenant names (for dropdown/autocomplete)
    public List<String> getAllTenantNames() {
        List<String> names = new ArrayList<>();
        String sql = "SELECT DISTINCT tenant_name FROM " + TABLE_NAME + " ORDER BY tenant_name";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                names.add(rs.getString("tenant_name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return names;
    }
    
    // Get tenant info by name (to auto-fill room number)
    public String getRoomNumberByTenant(String tenantName) {
        String sql = "SELECT room_number FROM " + TABLE_NAME + " WHERE tenant_name = ? LIMIT 1";
        
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, tenantName);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return String.valueOf(rs.getInt("room_number"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "";
    }
    
    /**
 * Get payment for specific tenant and month
 * Returns null if not found
 */
public Payment getPaymentForTenantAndMonth(String tenantName, String roomNumber, String month) {
    String sql = "SELECT * FROM " + TABLE_NAME + 
                 " WHERE (tenant_name = ? OR room_number = ?) AND month = ? LIMIT 1";
    
    try (Connection conn = getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {
        
        pst.setString(1, tenantName);
        pst.setString(2, roomNumber);
        pst.setString(3, month);
        
        ResultSet rs = pst.executeQuery();
        
        if (rs.next()) {
            int id = rs.getInt("payment_id");
            String tName = getStringOrDefault(rs, "tenant_name", "N/A");
            String rNumber = getStringOrDefault(rs, "room_number", "");
            double totalAmount = getDoubleOrDefault(rs, "total_amount", 0.0);
            double amountPaid = getDoubleOrDefault(rs, "amount_paid", 0.0);
            String paymentType = getStringOrDefault(rs, "payment_type", "Full Payment");
            double remainingBalance = getDoubleOrDefault(rs, "remaining_balance", 0.0);
            String m = getStringOrDefault(rs, "month", "N/A");
            Date dt = null;
            try {
                dt = rs.getDate("payment_date");
            } catch (SQLException ex) { dt = null; }
            LocalDate paymentDate = dt != null ? dt.toLocalDate() : null;
            String status = getStringOrDefault(rs, "status", "Pending");
            String notes = getStringOrDefault(rs, "notes", "");
            
            return new Payment(id, tName, rNumber, totalAmount, amountPaid, paymentType, remainingBalance, m, paymentDate, status, notes);
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
    return null;
}
}

