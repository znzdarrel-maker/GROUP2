public class TestRoomDAO {
    public static void main(String[] args) {
        dao.RoomDAO dao = new dao.RoomDAO();
        java.util.List<model.Room> rooms = dao.getAllRooms();
        for (model.Room room : rooms) {
            System.out.println(room.getRoomNumber() + " (" + room.getRoomType() + ")");
        }
    }
}