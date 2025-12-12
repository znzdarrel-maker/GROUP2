package dao;
import model.Room;
import java.sql.*;
import java.util.*;

public class RoomDAO {
    private final String url = "jdbc:mysql://localhost/houserent";
    private final String user = "root";
    private final String pass = "";
    
    // Get all rooms
    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, user, pass);
            String sql = "SELECT id, room_number, room_type, capacity, price, status, description FROM rooms";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                Room room = new Room(
                    rs.getInt("id"),
                    rs.getString("room_number"),
                    rs.getString("room_type"),
                    rs.getInt("capacity"),
                    rs.getDouble("price"),
                    rs.getString("status"),
                    rs.getString("description")
                );
                rooms.add(room);
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rooms;
    }
    
    // Status counts for filters
    public Map<String, Integer> getStatusCounts() {
        Map<String, Integer> counts = new HashMap<>();
        int total = 0;
        int available = 0, occupied = 0, maintenance = 0, underRepair = 0;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, user, pass);
            String sql = "SELECT status, COUNT(*) as cnt FROM rooms GROUP BY status";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                total += rs.getInt("cnt");
                String status = rs.getString("status");
                if (status == null) continue;
                switch (status.toLowerCase()) {
                    case "available":
                        available += rs.getInt("cnt");
                        break;
                    case "occupied":
                        occupied += rs.getInt("cnt");
                        break;
                    case "maintenance":
                        maintenance += rs.getInt("cnt");
                        break;
                    case "under repair":
                        underRepair += rs.getInt("cnt");
                        break;
                    default:
                        break;
                }
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        counts.put("All", total);
        counts.put("Available", available);
        counts.put("Occupied", occupied);
        counts.put("Maintenance", maintenance);
        counts.put("Under Repair", underRepair);
        return counts;
    }
    
    // ✅ NEW: Update room status (for Recommendation #2)
    public boolean updateRoomStatus(int roomNumber, String status) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, user, pass);
            
            String sql = "UPDATE rooms SET status = ? WHERE room_number = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            
            pst.setString(1, status);
            pst.setString(2, String.valueOf(roomNumber));
            
            int rowsAffected = pst.executeUpdate();
            con.close();
            
            return rowsAffected > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Add a room to database
    public boolean addRoom(Room room) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, user, pass);
            
            String sql = "INSERT INTO rooms (room_number, room_type, capacity, price, status, description) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            
            pst.setString(1, room.getRoomNumber());
            pst.setString(2, room.getRoomType());
            pst.setInt(3, room.getCapacity());
            pst.setDouble(4, room.getPrice());
            pst.setString(5, room.getStatus());
            pst.setString(6, room.getDescription());
            
            int rowsAffected = pst.executeUpdate();
            con.close();
            
            return rowsAffected > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Update a room
    public boolean updateRoom(Room room) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, user, pass);
            
            String sql = "UPDATE rooms SET room_number=?, room_type=?, capacity=?, price=?, status=?, description=? WHERE id=?";
            PreparedStatement pst = con.prepareStatement(sql);
            
            pst.setString(1, room.getRoomNumber());
            pst.setString(2, room.getRoomType());
            pst.setInt(3, room.getCapacity());
            pst.setDouble(4, room.getPrice());
            pst.setString(5, room.getStatus());
            pst.setString(6, room.getDescription());
            pst.setInt(7, room.getId());
            
            int rowsAffected = pst.executeUpdate();
            con.close();
            
            return rowsAffected > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Delete a room
    public boolean deleteRoom(int id) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, user, pass);
            
            String sql = "DELETE FROM rooms WHERE id=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, id);
            
            int rowsAffected = pst.executeUpdate();
            con.close();
            
            return rowsAffected > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}