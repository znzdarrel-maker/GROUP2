package model;

public class Room {
    private int id; // or roomId if you wish, match your DB primary key
    private String roomNumber;
    private String roomType;
    private int capacity;
    private double price;
    private String status;
    private String description;

    public Room(int id, String roomNumber, String roomType, int capacity, double price, String status, String description) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacity = capacity;
        this.price = price;
        this.status = status;
        this.description = description;
    }

    public Room(String roomNumber, String roomType, int capacity, double price, String status, String description) {
        // for inserts when id is auto-incremented
        this(0, roomNumber, roomType, capacity, price, status, description);
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}