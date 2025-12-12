package util;

import java.sql.*;

/**
 * DatabaseConnection - Centralized database connection utility
 * Use this instead of creating connections everywhere
 */
public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost/houserent";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    /**
     * Get database connection
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found", e);
        }
    }
    
    /**
     * Test if connection works
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}