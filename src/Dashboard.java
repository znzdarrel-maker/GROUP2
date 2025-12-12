import dao.RoomDAO;
import dao.TenantDAO;
import model.Room;
import model.Tenant;
import model.User;
import util.NotificationPanel;
import util.ThemeManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.util.List;

public class Dashboard extends JFrame implements ThemeManager.ThemeChangeListener {
    
    private User currentUser;
    private RoomDAO roomDAO;
    private TenantDAO tenantDAO;
    
    private JLabel lblTotalTenants, lblOccupiedRooms, lblVacantRooms, lblOverdue;
    private JTable tableTenants;
    private JTextField txtSearch;
    private JComboBox<String> cmbStatus;
    private JPanel mainPanel;
    
    public Dashboard(User user) {
        this.currentUser = user;
        this.roomDAO = new RoomDAO();
        this.tenantDAO = new TenantDAO();
        
        ThemeManager.addThemeChangeListener(this);
        
        // ✅ Start automatic billing scheduler
        try {
            BillingScheduler scheduler = BillingScheduler.getInstance();
            scheduler.start();
            System.out.println("✅ Automatic billing system started");
        } catch (Exception e) {
            System.err.println("⚠️ Could not start billing scheduler: " + e.getMessage());
        }
        
        initComponents();
        loadStatistics();
        loadRecentTenants();
        
        NotificationPanel.addPaymentNotification("Kevin has paid rent for Room 1 - ₱5,000");
        NotificationPanel.addTenantNotification("Nefur has been added to Room 3");
        NotificationPanel.addRoomNotification("Room 5 maintenance scheduled for tomorrow");
        NotificationPanel.addErrorNotification("Reminder: 2 tenants have overdue payments");
        NotificationPanel.addSystemNotification("RentEase system has been updated to v2.1");
    }

    // ✅ Shutdown method to stop scheduler
    private void shutdown() {
        try {
            BillingScheduler.getInstance().stop();
            System.out.println("🛑 Billing scheduler stopped");
        } catch (Exception e) {
            System.err.println("Error stopping scheduler: " + e.getMessage());
        }
    }
    
    @Override
    public void onThemeChanged(boolean isDark) {
        applyTheme();
    }
    
    private void applyTheme() {
        mainPanel.setBackground(ThemeManager.getMainPanelColor());
        
        tableTenants.setBackground(ThemeManager.getTableBackground());
        tableTenants.setGridColor(ThemeManager.getTableGrid());
        tableTenants.setSelectionBackground(ThemeManager.getTableSelection());
        tableTenants.setForeground(ThemeManager.getTextPrimary());
        
        txtSearch.setBackground(ThemeManager.getInputBackground());
        txtSearch.setForeground(ThemeManager.getTextPrimary());
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 10)
        ));
        
        cmbStatus.setBackground(ThemeManager.getInputBackground());
        cmbStatus.setForeground(ThemeManager.getTextPrimary());
        cmbStatus.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1));
        
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
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private void initComponents() {
        setTitle("RentEase - Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);
        
        mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);
        
        getContentPane().setBackground(Color.WHITE);
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
            if (i == 0) btn.setBackground(ThemeManager.getSidebarHover());
            
            final int index = i;
            btn.addActionListener(e -> {
                switch(index) {
                    case 1: openTenantManagement(); break;
                    case 2: openRoomManagement(); break;
                    case 3: openPaymentRecords(); break;
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
                if (!btn.getText().contains("Dashboard")) {
                    btn.setBackground(ThemeManager.getSidebarHover());
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!btn.getText().contains("Dashboard")) {
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
        
        JLabel lblHeader = new JLabel("Dashboard Overview");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblHeader.setForeground(ThemeManager.getTextPrimary());
        lblHeader.setBounds(40, 30, 400, 45);
        mainPanel.add(lblHeader);
        
        JPanel bellPanel = NotificationPanel.createNotificationButton(this, 1230, 30);
        mainPanel.add(bellPanel);
        
        createStatCard(mainPanel, "13", "Total Tenants", new Color(59, 130, 246), 40, 110, lblTotalTenants = new JLabel("13"));
        createStatCard(mainPanel, "11", "Occupied Rooms", new Color(34, 197, 94), 305, 110, lblOccupiedRooms = new JLabel("11"));
        createStatCard(mainPanel, "4", "Vacant Rooms", new Color(251, 191, 36), 570, 110, lblVacantRooms = new JLabel("4"));
        createStatCard(mainPanel, "0", "Overdue Payments", new Color(239, 68, 68), 835, 110, lblOverdue = new JLabel("0"));
        
        JLabel lblRecentTenants = new JLabel("Recent Tenants");
        lblRecentTenants.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblRecentTenants.setForeground(ThemeManager.getTextPrimary());
        lblRecentTenants.setBounds(40, 270, 200, 30);
        mainPanel.add(lblRecentTenants);
        
        JLabel lblStatus = new JLabel("Status:");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblStatus.setForeground(ThemeManager.getTextSecondary());
        lblStatus.setBounds(750, 275, 60, 35);
        mainPanel.add(lblStatus);
        
        cmbStatus = new JComboBox<>(new String[]{"All", "Active", "Inactive"});
        cmbStatus.setBounds(815, 275, 100, 38);
        cmbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbStatus.setBackground(ThemeManager.getInputBackground());
        cmbStatus.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1));
        cmbStatus.addActionListener(e -> filterTenants());
        mainPanel.add(cmbStatus);
        
        txtSearch = new JTextField("Search tenants...");
        txtSearch.setBounds(930, 275, 250, 38);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setForeground(Color.GRAY);
        txtSearch.setBackground(ThemeManager.getInputBackground());
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 10)
        ));
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().equals("Search tenants...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(ThemeManager.getTextPrimary());
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText("Search tenants...");
                    txtSearch.setForeground(Color.GRAY);
                }
            }
        });
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterTenants();
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterTenants();
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterTenants();
            }
        });
        mainPanel.add(txtSearch);
        
        JButton btnSearch = new JButton();
        ImageIcon searchIcon = loadIcon("search.png", 20, 20);
        
        if (searchIcon != null) {
            btnSearch.setIcon(searchIcon);
        } else {
            btnSearch.setText("🔍");
            btnSearch.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        }
        
        btnSearch.setBounds(1190, 275, 50, 38);
        btnSearch.setBackground(new Color(59, 130, 246));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        btnSearch.setBorderPainted(false);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearch.addActionListener(e -> filterTenants());
        mainPanel.add(btnSearch);
        
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
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        
        card.setBounds(x, y, 250, 118);
        card.setBackground(color);
        card.setLayout(null);
        card.setOpaque(false);
        
        valueLabel.setText(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 56));
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
    
    private void createTableSection(JPanel parent) {
        tableTenants = new JTable();
        tableTenants.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tableTenants.setRowHeight(50);
        tableTenants.setShowGrid(true);
        tableTenants.setGridColor(ThemeManager.getTableGrid());
        tableTenants.setIntercellSpacing(new Dimension(1, 1));
        tableTenants.setBackground(ThemeManager.getTableBackground());
        tableTenants.setSelectionBackground(ThemeManager.getTableSelection());
        tableTenants.setSelectionForeground(ThemeManager.getTextPrimary());
        tableTenants.setForeground(ThemeManager.getTextPrimary());
        
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.setColumnIdentifiers(new String[]{"ID", "Tenant Name", "Contact Number", "Room Number", "Status"});
        tableTenants.setModel(model);
        
        JTableHeader header = tableTenants.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(59, 130, 246));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 50));
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
        header.setOpaque(true);
        
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value.toString());
                label.setFont(new Font("Segoe UI", Font.BOLD, 14));
                label.setBackground(new Color(59, 130, 246));
                label.setForeground(Color.WHITE);
                label.setOpaque(true);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return label;
            }
        };
        
        for (int i = 0; i < tableTenants.getColumnCount(); i++) {
            tableTenants.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
        
        tableTenants.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel panel = new JPanel();
                panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 12));
                panel.setOpaque(true);
                panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                
                JLabel label = new JLabel(value.toString());
                label.setOpaque(true);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                
                if ("Active".equals(value.toString())) {
                    label.setBackground(new Color(220, 252, 231));
                    label.setForeground(new Color(22, 163, 74));
                } else {
                    label.setBackground(new Color(254, 226, 226));
                    label.setForeground(new Color(220, 38, 38));
                }
                
                label.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
                panel.add(label);
                return panel;
            }
        });
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tableTenants.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableTenants.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        
        tableTenants.getColumnModel().getColumn(0).setPreferredWidth(60);
        tableTenants.getColumnModel().getColumn(1).setPreferredWidth(200);
        tableTenants.getColumnModel().getColumn(2).setPreferredWidth(150);
        tableTenants.getColumnModel().getColumn(3).setPreferredWidth(120);
        tableTenants.getColumnModel().getColumn(4).setPreferredWidth(120);
        
        JScrollPane scrollPane = new JScrollPane(tableTenants);
        scrollPane.setBounds(40, 330, 1200, 540);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1));
        scrollPane.getViewport().setBackground(ThemeManager.getTableBackground());
        parent.add(scrollPane);
    }
    
    private void loadStatistics() {
        List<Tenant> tenants = tenantDAO.getAllTenants();
        lblTotalTenants.setText(String.valueOf(tenants.size()));
        
        List<Room> rooms = roomDAO.getAllRooms();
        int occupiedCount = 0;
        int vacantCount = 0;
        
        for (Room room : rooms) {
            if ("Occupied".equalsIgnoreCase(room.getStatus())) {
                occupiedCount++;
            } else if ("Available".equalsIgnoreCase(room.getStatus())) {
                vacantCount++;
            }
        }
        
        lblOccupiedRooms.setText(String.valueOf(occupiedCount));
        lblVacantRooms.setText(String.valueOf(vacantCount));
        lblOverdue.setText("0");
    }
    
    private void loadRecentTenants() {
        List<Tenant> tenants = tenantDAO.getAllTenants();
        DefaultTableModel model = (DefaultTableModel) tableTenants.getModel();
        model.setRowCount(0);
        
        for (Tenant tenant : tenants) {
            Object[] row = {
                tenant.getTenantId(),
                tenant.getName(),
                tenant.getContact(),
                tenant.getRoomNumber(),
                "Active"
            };
            model.addRow(row);
        }
    }
    
    private void filterTenants() {
        String searchText = txtSearch.getText().trim();
        if (searchText.equals("Search tenants...")) {
            searchText = "";
        }
        
        String statusFilter = (String) cmbStatus.getSelectedItem();
        
        List<Tenant> allTenants = tenantDAO.getAllTenants();
        DefaultTableModel model = (DefaultTableModel) tableTenants.getModel();
        model.setRowCount(0);
        
        for (Tenant tenant : allTenants) {
            if (tenant == null) continue;
            
            String tenantName = tenant.getName() != null ? tenant.getName() : "";
            String tenantContact = tenant.getContact() != null ? tenant.getContact() : "";
            String roomNumber = String.valueOf(tenant.getRoomNumber());
            
            String lowerSearch = searchText.toLowerCase();
            
            boolean matchesSearch = searchText.isEmpty() || 
                tenantName.toLowerCase().contains(lowerSearch) ||
                tenantContact.toLowerCase().contains(lowerSearch) ||
                roomNumber.contains(searchText);
            
            String tenantStatus = "Active";
            boolean matchesStatus = statusFilter.equals("All") || 
                                   statusFilter.equals(tenantStatus);
            
            if (matchesSearch && matchesStatus) {
                Object[] row = {
                    tenant.getTenantId(),
                    tenantName,
                    tenantContact,
                    tenant.getRoomNumber(),
                    tenantStatus
                };
                model.addRow(row);
            }
        }
    }
    
    private void openTenantManagement() {
        this.dispose();
        HouseRent screen = new HouseRent(currentUser);
        screen.setExtendedState(JFrame.MAXIMIZED_BOTH);
        screen.setVisible(true);
    }
    
    private void openRoomManagement() {
        this.dispose();
        AddRoom screen = new AddRoom(currentUser);
        screen.setExtendedState(JFrame.MAXIMIZED_BOTH);
        screen.setVisible(true);
    }
    
    private void openPaymentRecords() {
        this.dispose();
        PaymentRecords screen = new PaymentRecords(currentUser);
        screen.setExtendedState(JFrame.MAXIMIZED_BOTH);
        screen.setVisible(true);
    }
    
    // ✅ SINGLE logout() method with shutdown call
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            ThemeManager.removeThemeChangeListener(this);
            shutdown(); // ✅ Stop scheduler before closing
            this.dispose();
            Login login = new Login();
            login.setExtendedState(JFrame.MAXIMIZED_BOTH);
            login.setVisible(true);
        }
    }
}