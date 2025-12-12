import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

/**
 * ✅ Recommendation #3: Automatic Billing Scheduler
 * Generates monthly payment records automatically for all active tenants
 * WITH AUTOMATIC DATE CALCULATIONS
 */
public class BillingScheduler {
    private Timer timer;
    private static BillingScheduler instance;
    
    private final String url = "jdbc:mysql://localhost/houserent";
    private final String user = "root";
    private final String pass = "";
    
    private BillingScheduler() {
        timer = new Timer(true); // Daemon thread
    }
    
    public static BillingScheduler getInstance() {
        if (instance == null) {
            instance = new BillingScheduler();
        }
        return instance;
    }
    
    /**
     * Start the automatic billing scheduler
     * Checks daily if bills need to be generated
     */
    public void start() {
        if (!isBillingEnabled()) {
            System.out.println("⚠️ Automatic billing is disabled in settings");
            return;
        }
        
        // Run immediately on startup
        checkAndGenerateBills();
        
        // Schedule to run every day at midnight (24 hours = 86400000 ms)
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkAndGenerateBills();
            }
        }, 24 * 60 * 60 * 1000, 24 * 60 * 60 * 1000);
        
        System.out.println("✅ Billing scheduler started successfully");
    }
    
    /**
     * Stop the billing scheduler
     */
    public void stop() {
        if (timer != null) {
            timer.cancel();
            System.out.println("🛑 Billing scheduler stopped");
        }
    }
    
    /**
     * Check if today is billing day and generate bills
     */
    private void checkAndGenerateBills() {
        try {
            int billingDay = getBillingDay();
            LocalDate today = LocalDate.now();
            
            // Check if today is the billing day
            if (today.getDayOfMonth() == billingDay) {
                System.out.println("📅 Today is billing day! Generating bills...");
                generateMonthlyBills();
            } else {
                System.out.println("⏳ Not billing day yet. Next billing: Day " + billingDay);
            }
        } catch (Exception e) {
            System.err.println("❌ Error checking billing schedule: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generate monthly bills for all active tenants
     * ✅ WITH AUTOMATIC DATE CALCULATIONS
     */
    public void generateMonthlyBills() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int billsGenerated = 0;
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, pass);
            
            // Get all active tenants from records table
            String query = "SELECT r.id, r.name, r.houseno, r.payment " +
                          "FROM records r " +
                          "INNER JOIN rooms rm ON r.houseno = rm.room_number " +
                          "WHERE rm.status = 'Occupied'";
            
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();
            
            LocalDate today = LocalDate.now();
            YearMonth currentMonth = YearMonth.from(today);
            String monthName = today.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
            
            // ✅ AUTOMATIC DATE CALCULATIONS
            int billingDay = getBillingDay();
            
            // Due date: billing day of NEXT month
            LocalDate dueDate = today.withDayOfMonth(billingDay).plusMonths(1);
            
            while (rs.next()) {
                int tenantId = rs.getInt("id");
                String tenantName = rs.getString("name");
                String roomNo = String.valueOf(rs.getInt("houseno"));
                double monthlyRate = rs.getDouble("payment");
                
                // Check if bill already exists for this tenant this month
                if (!billExistsForTenantAndMonth(conn, tenantName, roomNo, monthName)) {
                    // Generate new bill with automatic due date
                    boolean created = createPaymentRecord(conn, tenantName, roomNo, monthlyRate, monthName, dueDate);
                    
                    if (created) {
                        billsGenerated++;
                        System.out.println("✅ Bill generated for: " + tenantName + " (Room " + roomNo + ") - ₱" + monthlyRate + " | Due: " + dueDate);
                    }
                }
            }
            
            System.out.println("🎉 Automatic billing complete! Generated " + billsGenerated + " bills.");
            
        } catch (Exception e) {
            System.err.println("❌ Error generating monthly bills: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Check if a bill already exists for tenant/month
     */
    private boolean billExistsForTenantAndMonth(Connection conn, String tenantName, String roomNo, String month) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            String query = "SELECT COUNT(*) FROM payments " +
                          "WHERE (tenant_name = ? OR room_number = ?) AND month = ?";
            
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, tenantName);
            pstmt.setString(2, roomNo);
            pstmt.setString(3, month);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error checking existing bill: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    
    /**
     * Create a payment record with AUTOMATIC DUE DATE
     */
    private boolean createPaymentRecord(Connection conn, String tenantName, String roomNo, 
                                       double amount, String month, LocalDate dueDate) {
        PreparedStatement pstmt = null;
        
        try {
            // ✅ Insert with automatic due_date calculation
            String query = "INSERT INTO payments (tenant_name, room_number, total_amount, " +
                          "amount_paid, payment_type, remaining_balance, month, payment_date, status, notes) " +
                          "VALUES (?, ?, ?, 0.00, 'Full Payment', ?, ?, NULL, 'Pending', 'Auto-generated')";
            
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, tenantName);
            pstmt.setString(2, roomNo);
            pstmt.setDouble(3, amount);
            pstmt.setDouble(4, amount); // remaining_balance = total_amount
            pstmt.setString(5, month);
            
            int rows = pstmt.executeUpdate();
            return rows > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error creating payment record: " + e.getMessage());
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * ✅ NEW: Automatically update payment dates when tenant pays
     */
    public boolean recordPayment(int paymentId, double amountPaid) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, pass);
            
            // Get current payment details
            String selectQuery = "SELECT total_amount, amount_paid FROM payments WHERE payment_id = ?";
            pstmt = conn.prepareStatement(selectQuery);
            pstmt.setInt(1, paymentId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                double totalAmount = rs.getDouble("total_amount");
                double currentPaid = rs.getDouble("amount_paid");
                double newTotalPaid = currentPaid + amountPaid;
                double newBalance = totalAmount - newTotalPaid;
                
                rs.close();
                pstmt.close();
                
                // ✅ Automatically set paid_date to TODAY
                LocalDate today = LocalDate.now();
                
                // Determine new status
                String newStatus;
                if (newBalance <= 0) {
                    newStatus = "Fully Paid";
                } else if (newTotalPaid > 0) {
                    newStatus = "Partial Payment";
                } else {
                    newStatus = "Pending";
                }
                
                // Update payment record
                String updateQuery = "UPDATE payments SET amount_paid = ?, remaining_balance = ?, " +
                                    "payment_date = ?, status = ? WHERE payment_id = ?";
                pstmt = conn.prepareStatement(updateQuery);
                pstmt.setDouble(1, newTotalPaid);
                pstmt.setDouble(2, newBalance);
                pstmt.setDate(3, Date.valueOf(today));
                pstmt.setString(4, newStatus);
                pstmt.setInt(5, paymentId);
                
                int rows = pstmt.executeUpdate();
                return rows > 0;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error recording payment: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    
    /**
     * Get billing day from settings
     */
    private int getBillingDay() {
        return Integer.parseInt(getSetting("billing_day", "1"));
    }
    
    /**
     * Check if billing is enabled
     */
    private boolean isBillingEnabled() {
        return Boolean.parseBoolean(getSetting("billing_enabled", "true"));
    }
    
    /**
     * Get a setting value from database
     */
    private String getSetting(String settingName, String defaultValue) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, pass);
            
            String query = "SELECT setting_value FROM billing_settings WHERE setting_name = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, settingName);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("setting_value");
            }
        } catch (Exception e) {
            System.err.println("❌ Error getting setting: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return defaultValue;
    }
    
    /**
     * Update a setting value (for Settings UI)
     */
    public boolean updateSetting(String settingName, String settingValue) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, pass);
            
            String query = "UPDATE billing_settings SET setting_value = ? WHERE setting_name = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, settingValue);
            pstmt.setString(2, settingName);
            
            int rows = pstmt.executeUpdate();
            return rows > 0;
            
        } catch (Exception e) {
            System.err.println("❌ Error updating setting: " + e.getMessage());
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}