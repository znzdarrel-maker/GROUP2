package model;

public class Tenant {
    private int tenantId;
    private String name;
    private String contact;
    private int roomNumber;
    private double payment;
    private String month;
    private String gender;  // ✅ ADDED: Gender field
    
    // Constructor for INSERT (no ID, with gender)
    public Tenant(String name, String contact, int roomNumber, double payment, String month, String gender) {
        this.name = name;
        this.contact = contact;
        this.roomNumber = roomNumber;
        this.payment = payment;
        this.month = month;
        this.gender = gender;  // ✅ ADDED
    }
    
    // Constructor for SELECT (with ID from database, with gender)
    public Tenant(int tenantId, String name, String contact, int roomNumber, double payment, String month, String gender) {
        this.tenantId = tenantId;
        this.name = name;
        this.contact = contact;
        this.roomNumber = roomNumber;
        this.payment = payment;
        this.month = month;
        this.gender = gender;  // ✅ ADDED
    }
    
    // ✅ BACKWARDS COMPATIBILITY: Old constructor without gender (sets gender to "N/A")
    public Tenant(String name, String contact, int roomNumber, double payment, String month) {
        this(name, contact, roomNumber, payment, month, "N/A");
    }
    
    public Tenant(int tenantId, String name, String contact, int roomNumber, double payment, String month) {
        this(tenantId, name, contact, roomNumber, payment, month, "N/A");
    }
    
    // Getters
    public int getTenantId() {
        return tenantId;
    }
    
    public String getName() {
        return name;
    }
    
    public String getContact() {
        return contact;
    }
    
    public int getRoomNumber() {
        return roomNumber;
    }
    
    public double getPayment() {
        return payment;
    }
    
    public String getMonth() {
        return month;
    }
    
    public String getGender() {  // ✅ ADDED
        return gender;
    }
    
    // Setters
    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setContact(String contact) {
        this.contact = contact;
    }
    
    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }
    
    public void setPayment(double payment) {
        this.payment = payment;
    }
    
    public void setMonth(String month) {
        this.month = month;
    }
    
    public void setGender(String gender) {  // ✅ ADDED
        this.gender = gender;
    }
}