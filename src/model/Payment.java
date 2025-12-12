package model;

import java.time.LocalDate;

public class Payment {
    private int id;              // payment_id
    private String tenantName;
    private String roomNumber;
    private double totalAmount;       // NEW - total amount for the month
    private double amountPaid;
    private String paymentType;
    private double remainingBalance;
    private String month;
    private LocalDate paymentDate;    // paid_date
    private String status;            // NEW - status (Fully Paid / Pending / Overdue / etc.)
    private String notes;             // NEW - notes field

    // Constructor with all fields (for DB read)
    public Payment(int id, String tenantName, String roomNumber, double totalAmount,
                   double amountPaid, String paymentType, double remainingBalance,
                   String month, LocalDate paymentDate, String status, String notes) {
        this.id = id;
        this.tenantName = tenantName;
        this.roomNumber = roomNumber;
        this.totalAmount = totalAmount;
        this.amountPaid = amountPaid;
        this.paymentType = paymentType;
        this.remainingBalance = remainingBalance;
        this.month = month;
        this.paymentDate = paymentDate;
        this.status = status;
        this.notes = notes;
    }

    // Constructor without id (for inserts) - keeps backwards compatibility (no status/notes)
    public Payment(String tenantName, String roomNumber, double totalAmount,
                   double amountPaid, String paymentType, double remainingBalance,
                   String month, LocalDate paymentDate) {
        this(0, tenantName, roomNumber, totalAmount, amountPaid, paymentType, remainingBalance, month, paymentDate, "Pending", "");
    }

    // Constructor without id but with status/notes
    public Payment(String tenantName, String roomNumber, double totalAmount,
                   double amountPaid, String paymentType, double remainingBalance,
                   String month, LocalDate paymentDate, String status, String notes) {
        this(0, tenantName, roomNumber, totalAmount, amountPaid, paymentType, remainingBalance, month, paymentDate, status, notes);
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public double getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(double remainingBalance) { this.remainingBalance = remainingBalance; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}