package dao;

import model.User;
import java.sql.*;

public class UserDAO {
    
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
    
    // Authenticate user login
    public User authenticateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, username);
            pst.setString(2, password);
            
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("full_name"),
                    rs.getString("role")
                );
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null; // Login failed
    }
}