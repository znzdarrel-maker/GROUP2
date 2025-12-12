import dao.PaymentDAO;
import dao.TenantDAO;
import model.Payment;
import model. Tenant;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AddPaymentDialog extends JDialog {
    
    private PaymentDAO paymentDAO;
    private TenantDAO tenantDAO;
    
    private JComboBox<String> cmbTenantName;
    private JTextField txtRoomNumber, txtAmountPaid, txtBalance, txtPaymentDate;
    private JComboBox<String> cmbMonth, cmbPaymentType;
    private JButton btnSave, btnCancel;
    
    private double roomPrice = 0.0;
    
    // ✅ Interface for payment callbacks
    public interface PaymentListener {
        void onPaymentAdded();
    }
    
    private PaymentListener paymentListener;
    
    public void setPaymentListener(PaymentListener listener) {
        this.paymentListener = listener;
    }
    
    public AddPaymentDialog(JFrame parent, PaymentDAO paymentDAO, TenantDAO tenantDAO) {
        super(parent, "Add Payment", true);
        this.paymentDAO = paymentDAO;
        this.tenantDAO = tenantDAO;
        
        initComponents();
        loadTenantNames();
        setDefaultPaymentDate();
    }
    
    private void initComponents() {
        setSize(500, 600);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(null);
        
        // Title
        JLabel lblTitle = new JLabel("Add New Payment");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(15, 23, 42));
        lblTitle.setBounds(30, 20, 440, 35);
        add(lblTitle);
        
        JLabel lblSubtitle = new JLabel("Enter payment details below");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle. setForeground(new Color(100, 116, 139));
        lblSubtitle. setBounds(30, 55, 440, 20);
        add(lblSubtitle);
        
        // Separator
        JSeparator separator = new JSeparator();
        separator.setBounds(30, 85, 440, 2);
        add(separator);
        
        // Tenant Name
        createLabel("Tenant Name *", 110);
        cmbTenantName = new JComboBox<>();
        cmbTenantName.setBounds(30, 135, 440, 40);
        cmbTenantName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbTenantName.setBackground(Color.WHITE);
        cmbTenantName.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        cmbTenantName.addActionListener(e -> loadTenantDetails());
        add(cmbTenantName);
        
        // Room Number (Auto-filled, read-only)
        createLabel("Room Number", 190);
        txtRoomNumber = createTextField(215);
        txtRoomNumber.setEditable(false);
        txtRoomNumber.setBackground(new Color(241, 245, 249));
        
        // Month
        createLabel("Payment Month *", 270);
        cmbMonth = new JComboBox<>(new String[]{
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        });
        cmbMonth.setBounds(30, 295, 210, 40);
        cmbMonth.setFont(new Font("Segoe UI", Font. PLAIN, 14));
        cmbMonth.setBackground(Color. WHITE);
        cmbMonth.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        add(cmbMonth);
        
        // Payment Type
        JLabel lblPaymentType = new JLabel("Payment Type");
        lblPaymentType.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPaymentType. setForeground(new Color(51, 65, 85));
        lblPaymentType. setBounds(260, 270, 210, 20);
        add(lblPaymentType);
        
        cmbPaymentType = new JComboBox<>(new String[]{"Full Payment", "Partial Payment", "Advance"});
        cmbPaymentType.setBounds(260, 295, 210, 40);
        cmbPaymentType.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbPaymentType.setBackground(Color.WHITE);
        cmbPaymentType.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        cmbPaymentType.addActionListener(e -> calculateBalance());
        add(cmbPaymentType);
        
        // Amount Paid
        createLabel("Amount Paid (₱) *", 350);
        txtAmountPaid = createTextField(375);
        txtAmountPaid.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java. awt.event.KeyEvent evt) {
                calculateBalance();
            }
        });
        
        // Remaining Balance (Auto-calculated)
        createLabel("Remaining Balance (₱)", 430);
        txtBalance = createTextField(455);
        txtBalance.setEditable(false);
        txtBalance.setBackground(new Color(241, 245, 249));
        txtBalance.setForeground(new Color(239, 68, 68));
        txtBalance.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // Payment Date
        JLabel lblDate = new JLabel("Payment Date");
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDate.setForeground(new Color(100, 116, 139));
        lblDate.setBounds(30, 510, 150, 15);
        add(lblDate);
        
        txtPaymentDate = new JTextField();
        txtPaymentDate.setBounds(150, 508, 120, 20);
        txtPaymentDate.setFont(new Font("Segoe UI", Font. PLAIN, 11));
        txtPaymentDate. setEditable(false);
        txtPaymentDate.setBackground(new Color(241, 245, 249));
        txtPaymentDate. setBorder(BorderFactory.createEmptyBorder());
        add(txtPaymentDate);
        
        // Buttons
        btnSave = new JButton("💾 Save Payment");
        btnSave.setBounds(30, 540, 200, 40);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setBackground(new Color(34, 197, 94));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setBorderPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> savePayment());
        add(btnSave);
        
        btnCancel = new JButton("Cancel");
        btnCancel.setBounds(245, 540, 225, 40);
        btnCancel.setFont(new Font("Segoe UI", Font. BOLD, 14));
        btnCancel.setBackground(new Color(148, 163, 184));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.setBorderPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dispose());
        add(btnCancel);
        
        getContentPane().setBackground(Color.WHITE);
    }
    
    private void createLabel(String text, int y) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font. BOLD, 13));
        label.setForeground(new Color(51, 65, 85));
        label.setBounds(30, y, 440, 20);
        add(label);
    }
    
    private JTextField createTextField(int y) {
        JTextField field = new JTextField();
        field.setBounds(30, y, 440, 40);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        add(field);
        return field;
    }
    
    private void loadTenantNames() {
        List<String> tenantNames = paymentDAO.getAllTenantNames();
        cmbTenantName.removeAllItems();
        cmbTenantName.addItem("-- Select Tenant --");
        
        for (String name : tenantNames) {
            cmbTenantName.addItem(name);
        }
    }
    
    private void loadTenantDetails() {
        String selectedTenant = (String) cmbTenantName.getSelectedItem();
        
        if (selectedTenant == null || selectedTenant.equals("-- Select Tenant --")) {
            txtRoomNumber.setText("");
            roomPrice = 0.0;
            txtBalance.setText("0.00");
            return;
        }
        
        // Get room number from tenant
        String roomNumber = paymentDAO.getRoomNumberByTenant(selectedTenant);
        txtRoomNumber.setText(roomNumber);
        
        // Get room price
        try {
            int roomNo = Integer.parseInt(roomNumber);
            roomPrice = tenantDAO.getRoomPrice(roomNo);
            calculateBalance();
        } catch (NumberFormatException e) {
            roomPrice = 0.0;
        }
    }
    
    private void calculateBalance() {
        String paymentType = (String) cmbPaymentType.getSelectedItem();
        String amountText = txtAmountPaid.getText().trim();
        
        if (amountText.isEmpty()) {
            txtBalance.setText("0.00");
            return;
        }
        
        try {
            double amountPaid = Double.parseDouble(amountText);
            double balance = 0.0;
            
            if ("Full Payment".equals(paymentType)) {
                balance = 0.0;
            } else if ("Partial Payment".equals(paymentType)) {
                balance = roomPrice - amountPaid;
            } else if ("Advance".equals(paymentType)) {
                balance = 0.0;
            }
            
            txtBalance.setText(String.format("%.2f", balance));
            
            // Color code balance
            if (balance > 0) {
                txtBalance.setForeground(new Color(239, 68, 68)); // Red for remaining
            } else {
                txtBalance.setForeground(new Color(34, 197, 94)); // Green for paid
            }
            
        } catch (NumberFormatException e) {
            txtBalance.setText("0.00");
        }
    }
    
    private void setDefaultPaymentDate() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        txtPaymentDate.setText(today. format(formatter));
    }
    
    private void savePayment() {
        // Validation
        String tenantName = (String) cmbTenantName.getSelectedItem();
        if (tenantName == null || tenantName.equals("-- Select Tenant --")) {
            JOptionPane.showMessageDialog(this, "Please select a tenant!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String roomNumber = txtRoomNumber.getText().trim();
        if (roomNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Room number is missing!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String amountText = txtAmountPaid.getText().trim();
        if (amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter amount paid!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            double amountPaid = Double.parseDouble(amountText);
            if (amountPaid <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be greater than 0!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String month = (String) cmbMonth.getSelectedItem();
            String paymentType = (String) cmbPaymentType.getSelectedItem();
            double balance = Double.parseDouble(txtBalance.getText());
            LocalDate paymentDate = LocalDate. parse(txtPaymentDate.getText());
            
            // Use roomPrice as total amount for this tenant's month
            double totalAmount = roomPrice;
            
            // Determine status based on payment type and amount
            String status = "Pending";
            if ("Full Payment". equals(paymentType) && amountPaid >= totalAmount) {
                status = "Fully Paid";
            } else if (amountPaid > 0 && amountPaid < totalAmount) {
                status = "Partial Payment";
            }
            
            // Create Payment object
            Payment payment = new Payment(
                tenantName,
                roomNumber,
                totalAmount,
                amountPaid,
                paymentType,
                balance,
                month,
                paymentDate,
                status,
                ""
            );
            
            // Save to database
            boolean saved = paymentDAO.addPayment(payment);
            
            if (saved) {
                JOptionPane.showMessageDialog(this, 
                    "Payment saved successfully! ",
                    "Success",
                    JOptionPane. INFORMATION_MESSAGE);
                
                // ✅ NOTIFY LISTENER TO REFRESH
                if (paymentListener != null) {
                    paymentListener.onPaymentAdded();
                }
                
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to save payment.  Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Invalid amount! Please enter a valid number.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "An error occurred: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}