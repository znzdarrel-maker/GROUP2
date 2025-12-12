import dao.PaymentDAO;
import dao.TenantDAO;
import model.Payment;
import model.User;
import util.NotificationManager;
import util.ThemeManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PaymentRecords extends JFrame implements ThemeManager.ThemeChangeListener {
     
    private PaymentDAO paymentDAO;
    private TenantDAO tenantDAO;
    private User currentUser;
    
    private JTable tablePayments;
    private JTextField txtSearch;
    private JComboBox<String> cmbStatusFilter, cmbMonthFilter;
    private JLabel lblTotalCollected, lblTotalPaid, lblPending, lblOverdue;
    private JScrollPane scrollPane;
    private JPanel mainPanel;
    
    // Edit dialog components
    private JDialog editDialog;
    private JComboBox<String> editCmbMonth, editCmbStatus;
    private JTextField editTxtTotalAmount, editTxtAmountPaid, editTxtDueDate, editTxtPaidDate, editTxtNotes;
    private JLabel editLblBalance, editLblTenantName, editLblRoomNo;
    private int selectedPaymentId = -1;
    
    public PaymentRecords(User user) {
        this.currentUser = user;
        this.paymentDAO = new PaymentDAO();
        this.tenantDAO = new TenantDAO();
        
        ThemeManager.addThemeChangeListener(this);
        
        initComponents();
        loadPaymentRecords();
        calculateStats();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustTableArea();
            }
            
            @Override
            public void componentShown(ComponentEvent e) {
                adjustTableArea();
            }

            private void adjustTableArea() {
                if (scrollPane != null) {
                    int frameWidth = getWidth();
                    int frameHeight = getHeight();
                    
                    int availableWidth = frameWidth - 235 - 80;
                    int availableHeight = frameHeight - 500;
                    
                    availableWidth = Math.max(availableWidth, 800);
                    availableHeight = Math.max(availableHeight, 300);
                    
                    scrollPane.setBounds(40, 400, availableWidth, availableHeight);
                    scrollPane.revalidate();
                    scrollPane.repaint();
                }
            }
        });
    }
    
    @Override
    public void onThemeChanged(boolean isDark) {
        applyTheme();
    }
    
    private void applyTheme() {
        mainPanel.setBackground(ThemeManager.getMainPanelColor());
        
        tablePayments.setBackground(ThemeManager.getTableBackground());
        tablePayments.setGridColor(ThemeManager.getTableGrid());
        tablePayments.setSelectionBackground(ThemeManager.getTableSelection());
        tablePayments.setForeground(ThemeManager.getTextPrimary());
        
        txtSearch.setBackground(ThemeManager.getInputBackground());
        txtSearch.setForeground(ThemeManager.getTextPrimary());
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 10)
        ));
        
        cmbStatusFilter.setBackground(ThemeManager.getInputBackground());
        cmbStatusFilter.setForeground(ThemeManager.getTextPrimary());
        cmbStatusFilter.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1));
        
        cmbMonthFilter.setBackground(ThemeManager.getInputBackground());
        cmbMonthFilter.setForeground(ThemeManager.getTextPrimary());
        cmbMonthFilter.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1));
        
        if (scrollPane != null) {
            scrollPane.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1));
            scrollPane.getViewport().setBackground(ThemeManager.getTableBackground());
        }
        
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    private ImageIcon loadIcon(String filename, int width, int height) {
        try {
            String path = "src/icons/" + filename;
            File file = new File(path);
            if (file.exists()) {
                ImageIcon icon = new ImageIcon(path);
                Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private void initComponents() {
        setTitle("RentEase - Payment Records");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);
        
        mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);
        
        applyTheme();
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(235, 850));
        sidebar.setBackground(ThemeManager.getSidebarColor());
        sidebar.setLayout(null);
        
        JLabel lblLogo = new JLabel("RentEase");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setBounds(25, 30, 180, 35);
        sidebar.add(lblLogo);
        
        JLabel lblSubtitle = new JLabel("Management System");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(148, 163, 184));
        lblSubtitle.setBounds(25, 65, 180, 20);
        sidebar.add(lblSubtitle);
        
        JPanel userPanel = new JPanel();
        userPanel.setBackground(ThemeManager.getSidebarHover());
        userPanel.setBounds(18, 110, 199, 65);
        userPanel.setLayout(null);
        
        JLabel lblWelcome = new JLabel("Welcome, " + currentUser.getFullName());
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblWelcome.setForeground(Color.WHITE);
        lblWelcome.setBounds(10, 10, 180, 20);
        userPanel.add(lblWelcome);
        
        JLabel lblActive = new JLabel("100% ACTIVE");
        lblActive.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblActive.setForeground(new Color(74, 222, 128));
        lblActive.setBounds(10, 35, 180, 15);
        userPanel.add(lblActive);
        sidebar.add(userPanel);
        
        int yPos = 210;
        String[] menuItems = {"Dashboard", "Tenant Management", "Room Management", "Payment Records", "Settings"};
        String[] iconFiles = {"dashboard.png", "tenant.png", "room.png", "peso.png", "setting.png"};
        
        for (int i = 0; i < menuItems.length; i++) {
            JButton btn = createMenuButton(menuItems[i], iconFiles[i], yPos);
            if (i == 3) btn.setBackground(ThemeManager.getSidebarHover());
            
            final int index = i;
            btn.addActionListener(e -> {
                switch(index) {
                    case 0: this.dispose(); new Dashboard(currentUser).setVisible(true); break;
                    case 1: this.dispose(); new HouseRent(currentUser).setVisible(true); break;
                    case 2: this.dispose(); new AddRoom(currentUser).setVisible(true); break;
                    case 4: this.dispose(); new Settings(currentUser).setVisible(true); break;
                }
            });
            sidebar.add(btn);
            yPos += 55;
        }
        
        JButton btnLogout = new JButton();
        ImageIcon logoutIcon = loadIcon("logout.png", 20, 20);
        if (logoutIcon != null) {
            btnLogout.setIcon(logoutIcon);
            btnLogout.setText("  Logout");
            btnLogout.setIconTextGap(10);
        } else {
            btnLogout.setText("⎋ Logout");
        }
        btnLogout.setBounds(18, 750, 199, 45);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogout.setBackground(new Color(239, 68, 68));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> logout());
        sidebar.add(btnLogout);
        
        return sidebar;
    }
    
    private JButton createMenuButton(String text, String iconFile, int yPos) {
    JButton btn = new JButton();
    btn.setBounds(18, yPos, 199, 45);
    btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    btn.setBackground(ThemeManager.getSidebarColor());
    btn.setForeground(Color.WHITE);
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    
    // ✅ ADD THESE LINES TO REMOVE CLICK ANIMATION
    btn.setContentAreaFilled(false);
    btn.setOpaque(true);
    btn.setRolloverEnabled(false);
    
    btn.setHorizontalAlignment(SwingConstants.LEFT);
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btn.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
    
    ImageIcon icon = loadIcon(iconFile, 20, 20);
    if (icon != null) {
        btn.setIcon(icon);
        btn.setText("  " + text);
        btn.setIconTextGap(10);
    } else {
        btn.setText(text);
    }
    
    btn.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            if (!btn.getText().contains("Dashboard")) { // Change this condition per panel
                btn.setBackground(ThemeManager.getSidebarHover());
            }
        }
        public void mouseExited(java.awt.event.MouseEvent evt) {
            if (!btn.getText().contains("Dashboard")) { // Change this condition per panel
                btn.setBackground(ThemeManager.getSidebarColor());
            }
        }
    });
    
    return btn;
}
    
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(ThemeManager.getMainPanelColor());
        mainPanel.setLayout(null);
        
        JLabel lblTitle = new JLabel("Payment Records");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(ThemeManager.getTextPrimary());
        lblTitle.setBounds(40, 30, 350, 45);
        mainPanel.add(lblTitle);
        
        JLabel lblSubtitle = new JLabel("Track and manage all tenant payments");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(ThemeManager.getTextSecondary());
        lblSubtitle.setBounds(40, 75, 350, 20);
        mainPanel.add(lblSubtitle);
        
        JButton btnExport = new JButton("📥 Export Records");
        btnExport.setBounds(1045, 35, 200, 42);
        btnExport.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnExport.setBackground(new Color(34, 197, 94));
        btnExport.setForeground(Color.WHITE);
        btnExport.setFocusPainted(false);
        btnExport.setBorderPainted(false);
        btnExport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExport.addActionListener(e -> exportRecords());
        mainPanel.add(btnExport);
        
        createStatCard(mainPanel, "₱0.00", "Total Collected", new Color(34, 197, 94), 40, 110, lblTotalCollected = new JLabel("₱0.00"));
        createStatCard(mainPanel, "0", "Total Paid", new Color(59, 130, 246), 305, 110, lblTotalPaid = new JLabel("0"));
        createStatCard(mainPanel, "11", "Pending Payments", new Color(251, 191, 36), 570, 110, lblPending = new JLabel("11"));
        createStatCard(mainPanel, "0", "Overdue", new Color(239, 68, 68), 835, 110, lblOverdue = new JLabel("0"));

        if (lblTotalCollected != null) {
            lblTotalCollected.setFont(new Font("Segoe UI", Font.BOLD, 34));
        }
        
        createFilterSection(mainPanel);
        createTableSection(mainPanel);
        
        return mainPanel;
    }
    
    private void createStatCard(JPanel parent, String value, String title, Color color, int x, int y, JLabel valueLabel) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        
        card.setBounds(x, y, 250, 118);
        card.setBackground(color);
        card.setLayout(null);
        card.setOpaque(false);
        
        valueLabel.setText(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setBounds(20, 15, 210, 60);
        card.add(valueLabel);
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBounds(20, 80, 210, 20);
        card.add(lblTitle);
        
        parent.add(card);
    }
    
    private void createFilterSection(JPanel parent) {
        JLabel lblSearch = new JLabel("Search");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSearch.setForeground(ThemeManager.getTextSecondary());
        lblSearch.setBounds(40, 260, 100, 20);
        parent.add(lblSearch);
        
        txtSearch = new JTextField("Search by name or room...");
        txtSearch.setBounds(40, 285, 280, 42);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setForeground(Color.GRAY);
        txtSearch.setBackground(ThemeManager.getInputBackground());
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 10)
        ));
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().equals("Search by name or room...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(ThemeManager.getTextPrimary());
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText("Search by name or room...");
                    txtSearch.setForeground(Color.GRAY);
                }
            }
        });
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filterPayments();
            }
        });
        parent.add(txtSearch);
        
        JLabel lblStatus = new JLabel("Status");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStatus.setForeground(ThemeManager.getTextSecondary());
        lblStatus.setBounds(340, 260, 100, 20);
        parent.add(lblStatus);
        
        cmbStatusFilter = new JComboBox<>(new String[]{"All", "Fully Paid", "Pending", "Half Payment", "Deposit", "Partial Payment", "Overdue"});
        cmbStatusFilter.setBounds(340, 285, 180, 42);
        cmbStatusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbStatusFilter.setBackground(ThemeManager.getInputBackground());
        cmbStatusFilter.setForeground(ThemeManager.getTextPrimary());
        cmbStatusFilter.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1));
        cmbStatusFilter.addActionListener(e -> filterPayments());
        parent.add(cmbStatusFilter);
        
        JLabel lblMonth = new JLabel("Month");
        lblMonth.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMonth.setForeground(ThemeManager.getTextSecondary());
        lblMonth.setBounds(540, 260, 100, 20);
        parent.add(lblMonth);
        
        cmbMonthFilter = new JComboBox<>(new String[]{"All", "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"});
        cmbMonthFilter.setBounds(540, 285, 180, 42);
        cmbMonthFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbMonthFilter.setBackground(ThemeManager.getInputBackground());
        cmbMonthFilter.setForeground(ThemeManager.getTextPrimary());
        cmbMonthFilter.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1));
        cmbMonthFilter.addActionListener(e -> filterPayments());
        parent.add(cmbMonthFilter);
        
        JButton btnClearFilters = new JButton("Clear Filters");
        btnClearFilters.setBounds(740, 285, 140, 42);
        btnClearFilters.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnClearFilters.setBackground(new Color(148, 163, 184));
        btnClearFilters.setForeground(Color.WHITE);
        btnClearFilters.setFocusPainted(false);
        btnClearFilters.setBorderPainted(false);
        btnClearFilters.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClearFilters.addActionListener(e -> clearFilters());
        parent.add(btnClearFilters);
    }

    private void createTableSection(JPanel parent) {
        JLabel lblTitle = new JLabel("All Payment Records");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(ThemeManager.getTextPrimary());
        lblTitle.setBounds(40, 360, 250, 25);
        parent.add(lblTitle);
        
       
        
        tablePayments = new JTable();
        tablePayments.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablePayments.setRowHeight(50);
        tablePayments.setShowGrid(true);
        tablePayments.setGridColor(ThemeManager.getTableGrid());
        tablePayments.setIntercellSpacing(new Dimension(1, 1));
        tablePayments.setBackground(ThemeManager.getTableBackground());
        tablePayments.setSelectionBackground(ThemeManager.getTableSelection());
        tablePayments.setSelectionForeground(ThemeManager.getTextPrimary());
        tablePayments.setForeground(ThemeManager.getTextPrimary());
        tablePayments.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.setColumnIdentifiers(new String[]{"ID", "Tenant Name", "Room No", "Month", "Total Amount", "Amount Paid", "Balance", "Due Date", "Paid Date", "Status", "Notes", "Actions"});
        tablePayments.setModel(model);
        
        // Hide ID column
        tablePayments.getColumnModel().getColumn(0).setMinWidth(0);
        tablePayments.getColumnModel().getColumn(0).setMaxWidth(0);
        tablePayments.getColumnModel().getColumn(0).setWidth(0);
        
        // Header styling
        javax.swing.table.JTableHeader header = tablePayments.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(59, 130, 246));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 50));
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
        
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value == null ? "" : value.toString());
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                label.setBackground(new Color(59, 130, 246));
                label.setForeground(Color.WHITE);
                label.setOpaque(true);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return label;
            }
        };
        
        for (int i = 0; i < tablePayments.getColumnCount(); i++) {
            tablePayments.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
        
        // Status column with colored badges
        tablePayments.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel panel = new JPanel();
                panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 12));
                panel.setOpaque(true);
                panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                
                JLabel label = new JLabel();
                label.setOpaque(true);
                label.setFont(new Font("Segoe UI", Font.BOLD, 11));
                
                String status = value == null ? "" : value.toString();
                ImageIcon statusIcon = null;
                
                switch (status) {
                    case "Fully Paid":
                        statusIcon = loadIcon("paid.png", 16, 16);
                        label.setBackground(new Color(220, 252, 231));
                        label.setForeground(new Color(22, 163, 74));
                        break;
                    case "Pending":
                        statusIcon = loadIcon("pending.png", 16, 16);
                        label.setBackground(new Color(254, 243, 199));
                        label.setForeground(new Color(180, 83, 9));
                        break;
                    case "Half Payment":
                        statusIcon = loadIcon("pending.png", 16, 16);
                        label.setBackground(new Color(219, 234, 254));
                        label.setForeground(new Color(29, 78, 216));
                        break;
                    case "Deposit":
                        statusIcon = loadIcon("pending.png", 16, 16);
                        label.setBackground(new Color(233, 213, 255));
                        label.setForeground(new Color(107, 33, 168));
                        break;
                    case "Partial Payment":
                        statusIcon = loadIcon("pending.png", 16, 16);
                        label.setBackground(new Color(224, 231, 255));
                        label.setForeground(new Color(67, 56, 202));
                        break;
                    case "Overdue":
                        statusIcon = loadIcon("over.png", 16, 16);
                        label.setBackground(new Color(254, 226, 226));
                        label.setForeground(new Color(220, 38, 38));
                        break;
                    default:
                        label.setBackground(new Color(241, 245, 249));
                        label.setForeground(new Color(100, 116, 139));
                }
                
                label.setText(status);
                if (statusIcon != null) {
                    label.setIcon(statusIcon);
                    label.setHorizontalTextPosition(SwingConstants.RIGHT);
                    label.setIconTextGap(8);
                }
                
                label.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
                panel.add(label);
                return panel;
            }
        });
        
        // Center align numeric columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tablePayments.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tablePayments.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tablePayments.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        tablePayments.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        tablePayments.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
        tablePayments.getColumnModel().getColumn(8).setCellRenderer(centerRenderer);
        
        // Actions column with blue edit button
        tablePayments.getColumnModel().getColumn(11).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel panel = new JPanel();
                panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 5));
                panel.setOpaque(true);
                panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                
                JButton btnEdit = new JButton();
                btnEdit.setPreferredSize(new Dimension(80, 32));
                btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 11));
                btnEdit.setBackground(new Color(59, 130, 246));
                btnEdit.setForeground(Color.WHITE);
                btnEdit.setFocusPainted(false);
                btnEdit.setBorderPainted(false);
                btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));
                
                ImageIcon editIcon = loadIcon("edit.png", 14, 14);
                if (editIcon != null) {
                    btnEdit.setIcon(editIcon);
                    btnEdit.setText(" Edit");
                    btnEdit.setHorizontalTextPosition(SwingConstants.RIGHT);
                    btnEdit.setIconTextGap(5);
                } else {
                    btnEdit.setText("✎ Edit");
                }
                btnEdit.setToolTipText("Edit");
                
                panel.add(btnEdit);
                return panel;
            }
        });
        
        // Column sizing
        tablePayments.getColumnModel().getColumn(1).setPreferredWidth(180);
        tablePayments.getColumnModel().getColumn(2).setPreferredWidth(90);
        tablePayments.getColumnModel().getColumn(3).setPreferredWidth(100);
        tablePayments.getColumnModel().getColumn(4).setPreferredWidth(110);
        tablePayments.getColumnModel().getColumn(5).setPreferredWidth(110);
        tablePayments.getColumnModel().getColumn(6).setPreferredWidth(90);
        tablePayments.getColumnModel().getColumn(7).setPreferredWidth(110);
        tablePayments.getColumnModel().getColumn(8).setPreferredWidth(110);
        tablePayments.getColumnModel().getColumn(9).setPreferredWidth(120);
        tablePayments.getColumnModel().getColumn(10).setPreferredWidth(150);
        tablePayments.getColumnModel().getColumn(11).setPreferredWidth(95);
        
        tablePayments.getColumnModel().getColumn(2).setMaxWidth(110);
        tablePayments.getColumnModel().getColumn(11).setMaxWidth(110);
        
        // Row click listener - THIS IS THE CRITICAL PART FOR EDITING!
        tablePayments.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tablePayments.getSelectedRow();
                if (row != -1) {
                    openEditDialog(row);
                }
            }
        });
        
        scrollPane = new JScrollPane(tablePayments);
        scrollPane.setBounds(40, 400, 1205, 490);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1));
        scrollPane.getViewport().setBackground(ThemeManager.getTableBackground());
        parent.add(scrollPane);
    }
    
    // ===== COMPLETE EDIT DIALOG METHOD =====
    private void openEditDialog(int viewRow) {
        DefaultTableModel model = (DefaultTableModel) tablePayments.getModel();
        int row = tablePayments.convertRowIndexToModel(viewRow);
        
        Object idObj = model.getValueAt(row, 0);
        selectedPaymentId = idObj != null ? Integer.parseInt(idObj.toString()) : -1;
        
        String tenantName = model.getValueAt(row, 1).toString();
        String roomNo = model.getValueAt(row, 2).toString();
        String month = model.getValueAt(row, 3).toString();
        String totalAmount = model.getValueAt(row, 4).toString().replace("₱", "").replace(",", "");
        String amountPaid = model.getValueAt(row, 5).toString().replace("₱", "").replace(",", "");
        String balance = model.getValueAt(row, 6).toString().replace("₱", "").replace(",", "");
        String dueDate = model.getValueAt(row, 7).toString();
        String paidDate = model.getValueAt(row, 8).toString();
        String status = model.getValueAt(row, 9).toString();
        String notes = model.getValueAt(row, 10) != null ? model.getValueAt(row, 10).toString() : "";
        
        editDialog = new JDialog(this, "Update Payment Record", true);
        editDialog.setSize(600, 750);
        editDialog.setLocationRelativeTo(this);
        editDialog.setResizable(false);
        editDialog.setLayout(null);
        editDialog.getContentPane().setBackground(Color.WHITE);
        
        JLabel lblHeader = new JLabel("Editing: " + tenantName + " - Room " + roomNo);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeader.setForeground(new Color(15, 23, 42));
        lblHeader.setBounds(30, 25, 540, 30);
        editDialog.add(lblHeader);
        
        JSeparator sep = new JSeparator();
        sep.setBounds(30, 65, 540, 2);
        editDialog.add(sep);
        
        // Tenant info (read-only)
        JLabel lblTenant = new JLabel("Tenant Name");
        lblTenant.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTenant.setForeground(new Color(51, 65, 85));
        lblTenant.setBounds(30, 85, 250, 20);
        editDialog.add(lblTenant);
        
        editLblTenantName = new JLabel(tenantName);
        editLblTenantName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        editLblTenantName.setForeground(new Color(100, 116, 139));
        editLblTenantName.setBounds(30, 110, 250, 35);
        editLblTenantName.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        editLblTenantName.setOpaque(true);
        editLblTenantName.setBackground(new Color(241, 245, 249));
        editDialog.add(editLblTenantName);
        
        JLabel lblRoom = new JLabel("Room Number");
        lblRoom.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRoom.setForeground(new Color(51, 65, 85));
        lblRoom.setBounds(300, 85, 270, 20);
        editDialog.add(lblRoom);
        
        editLblRoomNo = new JLabel(roomNo);
        editLblRoomNo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        editLblRoomNo.setForeground(new Color(100, 116, 139));
        editLblRoomNo.setBounds(300, 110, 270, 35);
        editLblRoomNo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        editLblRoomNo.setOpaque(true);
        editLblRoomNo.setBackground(new Color(241, 245, 249));
        editDialog.add(editLblRoomNo);
        
        // Month dropdown
        JLabel lblMonth = new JLabel("Month");
        lblMonth.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMonth.setForeground(new Color(51, 65, 85));
        lblMonth.setBounds(30, 165, 540, 20);
        editDialog.add(lblMonth);
        
        editCmbMonth = new JComboBox<>(new String[]{"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"});
        editCmbMonth.setSelectedItem(month);
        editCmbMonth.setBounds(30, 190, 540, 40);
        editCmbMonth.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        editCmbMonth.setBackground(Color.WHITE);
        editCmbMonth.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        editDialog.add(editCmbMonth);
        
        // Total Amount
        JLabel lblTotal = new JLabel("Total Amount (₱)");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotal.setForeground(new Color(51, 65, 85));
        lblTotal.setBounds(30, 250, 250, 20);
        editDialog.add(lblTotal);
        
        editTxtTotalAmount = new JTextField(totalAmount);
        editTxtTotalAmount.setBounds(30, 275, 250, 40);
        editTxtTotalAmount.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        editTxtTotalAmount.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        editTxtTotalAmount.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calculateBalance();
            }
        });
        editDialog.add(editTxtTotalAmount);
        
        // Amount Paid
        JLabel lblPaid = new JLabel("Amount Paid (₱)");
        lblPaid.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPaid.setForeground(new Color(51, 65, 85));
        lblPaid.setBounds(300, 250, 270, 20);
        editDialog.add(lblPaid);
        
        editTxtAmountPaid = new JTextField(amountPaid);
        editTxtAmountPaid.setBounds(300, 275, 270, 40);
        editTxtAmountPaid.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        editTxtAmountPaid.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        editTxtAmountPaid.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calculateBalance();
            }
        });
        editDialog.add(editTxtAmountPaid);
        
        // Balance (auto-calculated)
        JLabel lblBal = new JLabel("Balance (₱)");
        lblBal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblBal.setForeground(new Color(51, 65, 85));
        lblBal.setBounds(30, 335, 540, 20);
        editDialog.add(lblBal);
        
        editLblBalance = new JLabel("₱" + balance);
        editLblBalance.setFont(new Font("Segoe UI", Font.BOLD, 16));
        editLblBalance.setForeground(new Color(239, 68, 68));
        editLblBalance.setBounds(30, 360, 540, 40);
        editLblBalance.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        editLblBalance.setOpaque(true);
        editLblBalance.setBackground(new Color(254, 242, 242));
        editDialog.add(editLblBalance);
        
        // Due Date
        JLabel lblDue = new JLabel("Due Date");
        lblDue.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDue.setForeground(new Color(51, 65, 85));
        lblDue.setBounds(30, 420, 250, 20);
        editDialog.add(lblDue);
        
        editTxtDueDate = new JTextField(dueDate);
        editTxtDueDate.setBounds(30, 445, 250, 40);
        editTxtDueDate.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        editTxtDueDate.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        editDialog.add(editTxtDueDate);
        
        // Paid Date
        JLabel lblPaidDate = new JLabel("Paid Date");
        lblPaidDate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPaidDate.setForeground(new Color(51, 65, 85));
        lblPaidDate.setBounds(300, 420, 270, 20);
        editDialog.add(lblPaidDate);
        
        editTxtPaidDate = new JTextField(paidDate);
        editTxtPaidDate.setBounds(300, 445, 270, 40);
        editTxtPaidDate.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        editTxtPaidDate.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        editDialog.add(editTxtPaidDate);
        
        // Payment Status
        JLabel lblStatus = new JLabel("Payment Status");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStatus.setForeground(new Color(51, 65, 85));
        lblStatus.setBounds(30, 505, 540, 20);
        editDialog.add(lblStatus);
        
        editCmbStatus = new JComboBox<>(new String[]{"Fully Paid", "Pending", "Half Payment", "Deposit", "Partial Payment", "Overdue"});
        editCmbStatus.setSelectedItem(status);
        editCmbStatus.setBounds(30, 530, 540, 40);
        editCmbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        editCmbStatus.setBackground(Color.WHITE);
        editCmbStatus.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        editDialog.add(editCmbStatus);
        
        // Notes
        JLabel lblNotes = new JLabel("Notes");
        lblNotes.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNotes.setForeground(new Color(51, 65, 85));
        lblNotes.setBounds(30, 590, 540, 20);
        editDialog.add(lblNotes);
        
        editTxtNotes = new JTextField(notes);
        editTxtNotes.setBounds(30, 615, 540, 40);
        editTxtNotes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        editTxtNotes.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        editDialog.add(editTxtNotes);
        
        // Buttons
        JButton btnSave = new JButton("✓ Update Payment");
        btnSave.setBounds(30, 675, 270, 45);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setBackground(new Color(34, 197, 94));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setBorderPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> saveEditedPayment());
        editDialog.add(btnSave);
        
        JButton btnCancel = new JButton("Cancel");
        btnCancel.setBounds(315, 675, 255, 45);
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancel.setBackground(new Color(148, 163, 184));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.setBorderPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> editDialog.dispose());
        editDialog.add(btnCancel);
        
        editDialog.setVisible(true);
    }
    
    private void calculateBalance() {
        try {
            double total = Double.parseDouble(editTxtTotalAmount.getText().trim());
            double paid = Double.parseDouble(editTxtAmountPaid.getText().trim());
            double balance = total - paid;
            
            editLblBalance.setText("₱" + String.format("%.2f", balance));
            
            if (balance > 0) {
                editLblBalance.setForeground(new Color(239, 68, 68));
                editLblBalance.setBackground(new Color(254, 242, 242));
            } else {
                editLblBalance.setForeground(new Color(34, 197, 94));
                editLblBalance.setBackground(new Color(240, 253, 244));
            }
        } catch (NumberFormatException e) {
            editLblBalance.setText("₱0.00");
        }
    }
    
    private void saveEditedPayment() {
        if (editTxtTotalAmount.getText().trim().isEmpty() || editTxtAmountPaid.getText().trim().isEmpty()) {
            NotificationManager.showWarning(this, "Please fill all required fields!");
            return;
        }
        
        try {
            String month = (String) editCmbMonth.getSelectedItem();
            double totalAmount = Double.parseDouble(editTxtTotalAmount.getText().trim());
            double amountPaid = Double.parseDouble(editTxtAmountPaid.getText().trim());
            double balance = totalAmount - amountPaid;
            String dueDate = editTxtDueDate.getText().trim();
            String paidDateStr = editTxtPaidDate.getText().trim();
            String status = (String) editCmbStatus.getSelectedItem();
            String notes = editTxtNotes.getText().trim();
            String tenantName = editLblTenantName.getText();
            String roomNo = editLblRoomNo.getText();
            
            java.time.LocalDate paidDate = null;
            try {
                if (!paidDateStr.isEmpty() && !paidDateStr.equals("-")) {
                    paidDate = java.time.LocalDate.parse(paidDateStr);
                }
            } catch (Exception ex) {
                // ignore parse error, leave null
            }
            
            Payment payment = new Payment(
                selectedPaymentId,
                tenantName,
                roomNo,
                totalAmount,
                amountPaid,
                "Full Payment",
                balance,
                month,
                paidDate,
                status,
                notes
            );
            
            boolean updated = paymentDAO.updatePayment(payment);
            if (updated) {
                NotificationManager.showSuccess(this, "Payment record updated successfully!");
                editDialog.dispose();
                loadPaymentRecords();
                calculateStats();
            } else {
                NotificationManager.showError(this, "Failed to update payment in database!");
            }
            
        } catch (NumberFormatException e) {
            NotificationManager.showError(this, "Invalid amount! Please enter valid numbers.");
        }
    }
    
    public final void loadPaymentRecords() {
        List<Payment> payments = paymentDAO.getAllPayments();
        DefaultTableModel model = (DefaultTableModel) tablePayments.getModel();
        model.setRowCount(0);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        
        for (Payment p : payments) {
            String tenantName = p.getTenantName() != null ? p.getTenantName() : "N/A";
            String roomNo = p.getRoomNumber() != null ? p.getRoomNumber() : "";
            String month = p.getMonth() != null ? p.getMonth() : "N/A";
            double totalAmount = p.getTotalAmount();
            double amountPaid = p.getAmountPaid();
            double balance = p.getRemainingBalance();
            String dueDate = "2025-01-31";
            String paidDate = p.getPaymentDate() != null ? p.getPaymentDate().format(formatter) : "-";
            String status = p.getStatus() != null ? p.getStatus() : "Pending";
            String notes = p.getNotes() != null && !p.getNotes().isEmpty() ? p.getNotes() : "-";
            
            Object[] row = {
                p.getId(),
                tenantName,
                roomNo,
                month,
                "₱" + String.format("%.2f", totalAmount),
                "₱" + String.format("%.2f", amountPaid),
                "₱" + String.format("%.2f", balance),
                dueDate,
                paidDate,
                status,
                notes,
                "Edit"
            };
            model.addRow(row);
        }
        
        calculateStats();
    }
    
    private void calculateStats() {
        DefaultTableModel model = (DefaultTableModel) tablePayments.getModel();
        
        double totalCollected = 0.0;
        int totalPaid = 0;
        int pending = 0;
        int overdue = 0;
        
        for (int i = 0; i < model.getRowCount(); i++) {
            String amountPaidStr = model.getValueAt(i, 5).toString().replace("₱", "").replace(",", "");
            String status = model.getValueAt(i, 9).toString();
            
            try {
                double amountPaid = Double.parseDouble(amountPaidStr);
                totalCollected += amountPaid;
                
                if ("Fully Paid".equals(status)) {
                    totalPaid++;
                } else if ("Pending".equals(status)) {
                    pending++;
                } else if ("Overdue".equals(status)) {
                    overdue++;
                }
            } catch (NumberFormatException e) {
                // Skip invalid amounts
            }
        }
        
        lblTotalCollected.setText("₱" + String.format("%.2f", totalCollected));
        lblTotalPaid.setText(String.valueOf(totalPaid));
        lblPending.setText(String.valueOf(pending));
        lblOverdue.setText(String.valueOf(overdue));
    }
    
    private void filterPayments() {
        String searchText = txtSearch.getText().trim();
        if (searchText.equals("Search by name or room...")) {
            searchText = "";
        }
        
        String statusFilter = (String) cmbStatusFilter.getSelectedItem();
        String monthFilter = (String) cmbMonthFilter.getSelectedItem();
        
        List<Payment> allPayments = paymentDAO.getAllPayments();
        DefaultTableModel model = (DefaultTableModel) tablePayments.getModel();
        model.setRowCount(0);
        
        for (Payment p : allPayments) {
            String tenantName = p.getTenantName() != null ? p.getTenantName() : "";
            String roomNo = p.getRoomNumber() != null ? p.getRoomNumber() : "";
            String month = p.getMonth() != null ? p.getMonth() : "N/A";
            String status = p.getStatus() != null ? p.getStatus() : "Pending";
            
            boolean matchesSearch = searchText.isEmpty() || 
                tenantName.toLowerCase().contains(searchText.toLowerCase()) ||
                roomNo.contains(searchText);
            
            boolean matchesMonth = monthFilter.equals("All") || (month != null && month.equalsIgnoreCase(monthFilter));
            boolean matchesStatus = statusFilter.equals("All") || status.equalsIgnoreCase(statusFilter);
            
            if (matchesSearch && matchesMonth && matchesStatus) {
                double totalAmount = p.getTotalAmount();
                double amountPaid = p.getAmountPaid();
                double balance = p.getRemainingBalance();
                String dueDate = "2025-01-31";
                String paidDate = p.getPaymentDate() != null ? p.getPaymentDate().toString() : "-";
                String notes = p.getNotes() != null && !p.getNotes().isEmpty() ? p.getNotes() : "-";
                
                Object[] row = {
                    p.getId(),
                    tenantName,
                    roomNo,
                    month,
                    "₱" + String.format("%.2f", totalAmount),
                    "₱" + String.format("%.2f", amountPaid),
                    "₱" + String.format("%.2f", balance),
                    dueDate,
                    paidDate,
                    status,
                    notes,
                    "Edit"
                };
                model.addRow(row);
            }
        }
        
        calculateStats();
    }
    
    private void clearFilters() {
        txtSearch.setText("Search by name or room...");
        txtSearch.setForeground(Color.GRAY);
        cmbStatusFilter.setSelectedIndex(0);
        cmbMonthFilter.setSelectedIndex(0);
        loadPaymentRecords();
    }
    
    private void exportRecords() {
        DefaultTableModel model = (DefaultTableModel) tablePayments.getModel();
        int rowCount = model.getRowCount();
        
        if (rowCount == 0) {
            NotificationManager.showWarning(this, "No records to export!");
            return;
        }
        
        StringBuilder csvData = new StringBuilder();
        csvData.append("Tenant Name,Room No,Month,Total Amount,Amount Paid,Balance,Due Date,Paid Date,Status,Notes\n");
        
        for (int i = 0; i < rowCount; i++) {
            for (int j = 1; j < 11; j++) {
                Object value = model.getValueAt(i, j);
                csvData.append(value != null ? value.toString() : "");
                if (j < 10) csvData.append(",");
            }
            csvData.append("\n");
        }
        
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Payment Records");
            fileChooser.setSelectedFile(new java.io.File("payment_records_" + LocalDate.now() + ".csv"));
            
            int userSelection = fileChooser.showSaveDialog(this);
            
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToSave = fileChooser.getSelectedFile();
                java.nio.file.Files.write(fileToSave.toPath(), csvData.toString().getBytes());
                NotificationManager.showSuccess(this, "Records exported successfully to " + fileToSave.getName());
            }
        } catch (Exception e) {
            NotificationManager.showError(this, "Export failed: " + e.getMessage());
        }
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            ThemeManager.removeThemeChangeListener(this);
            this.dispose();
            new Login().setVisible(true);
        }
    }
}