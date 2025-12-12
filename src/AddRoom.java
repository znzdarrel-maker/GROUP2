import model.Room;
import model.User;
import dao.RoomDAO;
import util.NotificationManager;
import util.ThemeManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Map;

public class AddRoom extends JFrame implements ThemeManager.ThemeChangeListener {
    
    private RoomDAO roomDAO;
    private User currentUser;
    
    private JTable tableRooms;
    private JTextField txtRoomNumber, txtCapacity, txtPrice;
    private JComboBox<String> cmbStatus, cmbRoomType;
    private JLabel lblTotalRooms, lblAvailableRooms;
    
    // ✅ NEW: Search field for room list
    private JTextField txtSearch;
    
    private int selectedRoomId = -1;
    private boolean isEditMode = false;
    private JButton btnAddUpdate, btnCancelEdit, btnDelete;
    
    // Status filter buttons
    private JButton btnFilterAll, btnFilterAvailable, btnFilterOccupied, btnFilterMaintenance, btnFilterUnderRepair;
    private String currentFilter = "All";
    
    private JPanel mainPanel;
    private JScrollPane scrollPane;
    
    public AddRoom(User user) {
        this.currentUser = user;
        this.roomDAO = new RoomDAO();
        
        ThemeManager.addThemeChangeListener(this);
        
        initComponents();
        loadRooms();
        setupTableListener();
    }
    
    @Override
    public void onThemeChanged(boolean isDark) {
        applyTheme();
    }
    
    private void applyTheme() {
        mainPanel.setBackground(ThemeManager.getMainPanelColor());
        
        tableRooms.setBackground(ThemeManager.getTableBackground());
        tableRooms.setGridColor(ThemeManager.getTableGrid());
        tableRooms.setSelectionBackground(ThemeManager.getTableSelection());
        tableRooms.setForeground(ThemeManager.getTextPrimary());
        
        updateTextFieldTheme(txtRoomNumber);
        updateTextFieldTheme(txtCapacity);
        updateTextFieldTheme(txtPrice);
        
        // ✅ NEW: Update search field theme
        updateTextFieldTheme(txtSearch);
        
        updateComboBoxTheme(cmbStatus);
        updateComboBoxTheme(cmbRoomType);
        
        if (scrollPane != null) {
            scrollPane.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1));
            scrollPane.getViewport().setBackground(ThemeManager.getTableBackground());
        }
        
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    private void updateTextFieldTheme(JTextField field) {
        if (field == null) return;
        field.setBackground(ThemeManager.getInputBackground());
        field.setForeground(ThemeManager.getTextPrimary());
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }
    
    private void updateComboBoxTheme(JComboBox<?> combo) {
        if (combo == null) return;
        combo.setBackground(ThemeManager.getInputBackground());
        combo.setForeground(ThemeManager.getTextPrimary());
        combo.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1));
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
        setTitle("RentEase - Room Management");
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
            if (i == 2) btn.setBackground(ThemeManager.getSidebarHover());
            
            final int index = i;
            btn.addActionListener(e -> {
                switch(index) {
                    case 0: this.dispose(); new Dashboard(currentUser).setVisible(true); break;
                    case 1: this.dispose(); new HouseRent(currentUser).setVisible(true); break;
                    case 3: this.dispose(); new PaymentRecords(currentUser).setVisible(true); break;
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
                if (!btn.getText().contains("Room Management")) {
                    btn.setBackground(ThemeManager.getSidebarHover());
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!btn.getText().contains("Room Management")) {
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
        
        JLabel lblTitle = new JLabel("Room Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(ThemeManager.getTextPrimary());
        lblTitle.setBounds(40, 30, 400, 45);
        mainPanel.add(lblTitle);
        
        createStatCard(mainPanel, "17", "Total Rooms", new Color(59, 130, 246), 40, 110, lblTotalRooms = new JLabel("17"));
        createStatCard(mainPanel, "4", "Available", new Color(34, 197, 94), 305, 110, lblAvailableRooms = new JLabel("4"));
        
        createFormPanel(mainPanel);
        createTablePanel(mainPanel);
        
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
    
    private void createFormPanel(JPanel parent) {
        JPanel formCard = new JPanel();
        formCard.setBackground(ThemeManager.getCardBackground());
        formCard.setBounds(40, 265, 245, 625);
        formCard.setLayout(null);
        formCard.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1));
        
        JLabel lblFormTitle = new JLabel("Room Details");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblFormTitle.setForeground(ThemeManager.getTextPrimary());
        lblFormTitle.setBounds(20, 20, 205, 25);
        formCard.add(lblFormTitle);
        
        createFormField(formCard, "Room Number", 65, txtRoomNumber = new JTextField());
        
        JLabel lblRoomType = new JLabel("Room Type");
        lblRoomType.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRoomType.setForeground(ThemeManager.getTextSecondary());
        lblRoomType.setBounds(20, 140, 205, 20);
        formCard.add(lblRoomType);
        
        cmbRoomType = new JComboBox<>(new String[]{"single", "double", "deluxe", "custom"});
        cmbRoomType.setBounds(20, 165, 205, 38);
        cmbRoomType.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbRoomType.setBackground(ThemeManager.getInputBackground());
        cmbRoomType.setForeground(ThemeManager.getTextPrimary());
        cmbRoomType.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1));
        cmbRoomType.addActionListener(e -> autoFillRoomDetails());
        formCard.add(cmbRoomType);
        
        createFormField(formCard, "Capacity", 215, txtCapacity = new JTextField());
        createFormField(formCard, "Price (₱)", 290, txtPrice = new JTextField());
        
        JLabel lblStatus = new JLabel("Status");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStatus.setForeground(ThemeManager.getTextSecondary());
        lblStatus.setBounds(20, 365, 205, 20);
        formCard.add(lblStatus);
        
        cmbStatus = new JComboBox<>(new String[]{"Available", "Occupied", "Maintenance", "Under Repair"});
        cmbStatus.setBounds(20, 390, 205, 38);
        cmbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbStatus.setBackground(ThemeManager.getInputBackground());
        cmbStatus.setForeground(ThemeManager.getTextPrimary());
        cmbStatus.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1));
        formCard.add(cmbStatus);
        
        btnAddUpdate = createActionButton("Add Room", new Color(34, 197, 94), 20, 445);
        btnAddUpdate.addActionListener(e -> {
            if (isEditMode) {
                updateRoom();
            } else {
                addRoom();
            }
        });
        formCard.add(btnAddUpdate);
        
        btnCancelEdit = createActionButton("Cancel Edit", new Color(251, 191, 36), 20, 490);
        btnCancelEdit.addActionListener(e -> cancelEdit());
        btnCancelEdit.setVisible(false);
        formCard.add(btnCancelEdit);
        
        btnDelete = createActionButton("Delete", new Color(239, 68, 68), 20, 535);
        btnDelete.addActionListener(e -> deleteRoom());
        btnDelete.setVisible(false);
        formCard.add(btnDelete);
        
        parent.add(formCard);
    }
    
    private void createFormField(JPanel parent, String label, int y, JTextField field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(ThemeManager.getTextSecondary());
        lbl.setBounds(20, y, 205, 20);
        parent.add(lbl);
        
        field.setBounds(20, y + 25, 205, 38);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBackground(ThemeManager.getInputBackground());
        field.setForeground(ThemeManager.getTextPrimary());
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        parent.add(field);
    }
    
    private JButton createActionButton(String text, Color color, int x, int y) {
        JButton btn = new JButton(text);
        btn.setBounds(x, y, 205, 38);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ✅ NEW: Modified table panel with search bar
    private void createTablePanel(JPanel parent) {
        JLabel lblTitle = new JLabel("Room List");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(ThemeManager.getTextPrimary());
        lblTitle.setBounds(305, 265, 150, 30);
        parent.add(lblTitle);
        
        // ✅ NEW: Search bar section
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSearch.setForeground(ThemeManager.getTextSecondary());
        lblSearch.setBounds(800, 270, 70, 35);
        parent.add(lblSearch);
        
        txtSearch = new JTextField("Search rooms...");
        txtSearch.setBounds(870, 270, 250, 38);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setForeground(Color.GRAY);
        txtSearch.setBackground(ThemeManager.getInputBackground());
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 10)
        ));
        
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().equals("Search rooms...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(ThemeManager.getTextPrimary());
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText("Search rooms...");
                    txtSearch.setForeground(Color.GRAY);
                }
            }
        });
        
        // ✅ NEW: Real-time search as user types
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchRooms();
            }
        });
        
        parent.add(txtSearch);
        
        JButton btnSearch = new JButton();
        ImageIcon searchIcon = loadIcon("search.png", 20, 20);
        if (searchIcon != null) {
            btnSearch.setIcon(searchIcon);
        } else {
            btnSearch.setText("🔍");
            btnSearch.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        }
        btnSearch.setBounds(1130, 270, 50, 38);
        btnSearch.setBackground(new Color(59, 130, 246));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        btnSearch.setBorderPainted(false);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearch.addActionListener(e -> searchRooms());
        parent.add(btnSearch);
        
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBounds(1195, 270, 100, 38);
        btnRefresh.setBackground(new Color(148, 163, 184));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> {
            clearFields();
            txtSearch.setText("Search rooms...");
            txtSearch.setForeground(Color.GRAY);
            loadRooms();
        });
        parent.add(btnRefresh);
        
        // Filter bar
        JPanel filterBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            }
        };
        filterBar.setBackground(ThemeManager.getCardBackground());
        filterBar.setLayout(null);
        filterBar.setBounds(305, 320, 990, 52);
        filterBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1,1,1,1, ThemeManager.getInputBorder()),
            BorderFactory.createEmptyBorder(8,12,8,12)
        ));
        
        JLabel lblFilter = new JLabel("Filter by Status:");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblFilter.setForeground(ThemeManager.getTextSecondary());
        lblFilter.setBounds(12, 12, 130, 28);
        filterBar.add(lblFilter);
        
        btnFilterAll = createPillFilter("All", "all.png", 150, 10, new Color(59,130,246), true);
        btnFilterAvailable = createPillFilter("Available", "avail.png", 260, 10, new Color(220,252,231), false);
        btnFilterOccupied = createPillFilter("Occupied", "occu.png", 390, 10, new Color(254,226,226), false);
        btnFilterMaintenance = createPillFilter("Maintenance", "mainte.png", 520, 10, new Color(254,243,199), false);
        btnFilterUnderRepair = createPillFilter("Under Repair", "under.png", 680, 10, new Color(255,249,230), false);
        
        filterBar.add(btnFilterAll);
        filterBar.add(btnFilterAvailable);
        filterBar.add(btnFilterOccupied);
        filterBar.add(btnFilterMaintenance);
        filterBar.add(btnFilterUnderRepair);
        
        parent.add(filterBar);
        
        tableRooms = new JTable();
        tableRooms.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tableRooms.setRowHeight(50);
        tableRooms.setShowGrid(true);
        tableRooms.setGridColor(ThemeManager.getTableGrid());
        tableRooms.setIntercellSpacing(new Dimension(1, 1));
        tableRooms.setBackground(ThemeManager.getTableBackground());
        tableRooms.setSelectionBackground(ThemeManager.getTableSelection());
        tableRooms.setSelectionForeground(ThemeManager.getTextPrimary());
        tableRooms.setForeground(ThemeManager.getTextPrimary());
        
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.setColumnIdentifiers(new String[]{"ID", "Room No", "Type", "Capacity", "Price", "Status"});
        tableRooms.setModel(model);
        
        // Hide ID column
        tableRooms.getColumnModel().getColumn(0).setMinWidth(0);
        tableRooms.getColumnModel().getColumn(0).setMaxWidth(0);
        tableRooms.getColumnModel().getColumn(0).setWidth(0);
        
        JTableHeader header = tableRooms.getTableHeader();
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
                JLabel label = new JLabel(value == null ? "" : value.toString());
                label.setFont(new Font("Segoe UI", Font.BOLD, 14));
                label.setBackground(new Color(59, 130, 246));
                label.setForeground(Color.WHITE);
                label.setOpaque(true);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return label;
            }
        };
        
        for (int i = 0; i < tableRooms.getColumnCount(); i++) {
            tableRooms.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
        
        // Status column renderer
        tableRooms.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel panel = new JPanel();
                panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 12));
                panel.setOpaque(true);
                panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                
                JLabel label = new JLabel(value == null ? "" : value.toString());
                label.setOpaque(true);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                
                String status = value == null ? "" : value.toString();
                if ("Available".equalsIgnoreCase(status)) {
                    label.setBackground(new Color(220, 252, 231));
                    label.setForeground(new Color(22, 163, 74));
                } else if ("Occupied".equalsIgnoreCase(status)) {
                    label.setBackground(new Color(254, 226, 226));
                    label.setForeground(new Color(220, 38, 38));
                } else if ("Maintenance".equalsIgnoreCase(status)) {
                    label.setBackground(new Color(254, 243, 199));
                    label.setForeground(new Color(180, 83, 9));
                } else if ("Under Repair".equalsIgnoreCase(status)) {
                    label.setBackground(new Color(255, 249, 230));
                    label.setForeground(new Color(217, 119, 6));
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(new Color(51, 65, 85));
                }
                
                label.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
                panel.add(label);
                return panel;
            }
        });
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tableRooms.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tableRooms.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tableRooms.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        
        tableRooms.getColumnModel().getColumn(1).setPreferredWidth(80);
        tableRooms.getColumnModel().getColumn(2).setPreferredWidth(120);
        tableRooms.getColumnModel().getColumn(3).setPreferredWidth(80);
        tableRooms.getColumnModel().getColumn(4).setPreferredWidth(100);
        tableRooms.getColumnModel().getColumn(5).setPreferredWidth(120);
        
        scrollPane = new JScrollPane(tableRooms);
        scrollPane.setBounds(305, 380, 990, 510);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1));
        scrollPane.getViewport().setBackground(ThemeManager.getTableBackground());
        parent.add(scrollPane);
    }
    
    private JButton createPillFilter(String label, String iconFile, int x, int y, Color bgExample, boolean isActive) {
        JButton btn = new JButton(label);
        btn.setBounds(x, y, 120, 32);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 12));
        
        ImageIcon ic = loadIcon(iconFile, 16, 16);
        if (ic != null) {
            btn.setIcon(ic);
            btn.setIconTextGap(8);
        }
        
        if (isActive) {
            btn.setBackground(new Color(59,130,246));
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(new Color(247,249,250));
            btn.setForeground(new Color(51,65,85));
        }
        
        btn.addActionListener(e -> {
            currentFilter = label;
            updateFilterButtons(label);
            loadRooms();
        });
        
        return btn;
    }
    
    private void updateFilterButtons(String activeFilter) {
        Map<String, Integer> counts = roomDAO.getStatusCounts();
        int all = counts.getOrDefault("All", 0);
        int avail = counts.getOrDefault("Available", 0);
        int occ = counts.getOrDefault("Occupied", 0);
        int maint = counts.getOrDefault("Maintenance", 0);
        int under = counts.getOrDefault("Under Repair", 0);
        
        btnFilterAll.setText("All (" + all + ")");
        btnFilterAvailable.setText("Available (" + avail + ")");
        btnFilterOccupied.setText("Occupied (" + occ + ")");
        btnFilterMaintenance.setText("Maintenance (" + maint + ")");
        btnFilterUnderRepair.setText("Under Repair (" + under + ")");
        
        btnFilterAll.setBackground(activeFilter.equals("All") ? new Color(59,130,246) : new Color(247,249,250));
        btnFilterAll.setForeground(activeFilter.equals("All") ? Color.WHITE : new Color(51,65,85));
        
        btnFilterAvailable.setBackground(activeFilter.equals("Available") ? new Color(34,197,94) : new Color(247,249,250));
        btnFilterAvailable.setForeground(activeFilter.equals("Available") ? Color.WHITE : new Color(51,65,85));
        
        btnFilterOccupied.setBackground(activeFilter.equals("Occupied") ? new Color(239,68,68) : new Color(247,249,250));
        btnFilterOccupied.setForeground(activeFilter.equals("Occupied") ? Color.WHITE : new Color(51,65,85));
        
        btnFilterMaintenance.setBackground(activeFilter.equals("Maintenance") ? new Color(251,191,36) : new Color(247,249,250));
        btnFilterMaintenance.setForeground(activeFilter.equals("Maintenance") ? Color.WHITE : new Color(51,65,85));
        
        btnFilterUnderRepair.setBackground(activeFilter.equals("Under Repair") ? new Color(250,204,21) : new Color(247,249,250));
        btnFilterUnderRepair.setForeground(activeFilter.equals("Under Repair") ? Color.WHITE : new Color(51,65,85));
    }
    
    // ✅ NEW: Search functionality for rooms
    private void searchRooms() {
        String searchText = txtSearch.getText().trim();
        if (searchText.isEmpty() || searchText.equals("Search rooms...")) {
            loadRooms();
            return;
        }
        
        List<Room> allRooms = roomDAO.getAllRooms();
        DefaultTableModel model = (DefaultTableModel) tableRooms.getModel();
        model.setRowCount(0);
        
        int totalCount = 0;
        int availableCount = 0;
        
        for (Room room : allRooms) {
            String roomNumber = room.getRoomNumber() != null ? room.getRoomNumber() : "";
            String roomType = room.getRoomType() != null ? room.getRoomType() : "";
            String status = room.getStatus() != null ? room.getStatus() : "";
            
            String lowerSearch = searchText.toLowerCase();
            
            // Search by room number, room type, or status
            boolean matchesSearch = roomNumber.toLowerCase().contains(lowerSearch) ||
                                   roomType.toLowerCase().contains(lowerSearch) ||
                                   status.toLowerCase().contains(lowerSearch);
            
            // Apply current filter
            boolean matchesFilter = "All".equalsIgnoreCase(currentFilter) ||
                                   status.equalsIgnoreCase(currentFilter);
            
            if (matchesSearch && matchesFilter) {
                Object[] row = {
                    room.getId(),
                    room.getRoomNumber(),
                    room.getRoomType(),
                    room.getCapacity(),
                    String.format("₱%.2f", room.getPrice()),
                    room.getStatus()
                };
                model.addRow(row);
                totalCount++;
                
                if ("Available".equalsIgnoreCase(room.getStatus())) {
                    availableCount++;
                }
            }
        }
        
        lblTotalRooms.setText(String.valueOf(totalCount));
        lblAvailableRooms.setText(String.valueOf(availableCount));
    }
    
    private void autoFillRoomDetails() {
        String roomType = (String) cmbRoomType.getSelectedItem();
        if (roomType == null || isEditMode) return;
        
        switch (roomType) {
            case "single":
                txtCapacity.setText("2");
                txtPrice.setText("1500");
                break;
            case "double":
                txtCapacity.setText("4");
                txtPrice.setText("2500");
                break;
            case "deluxe":
                txtCapacity.setText("6");
                txtPrice.setText("4000");
                break;
            case "custom":
                break;
        }
    }
    
    private void setupTableListener() {
        tableRooms.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tableRooms.getSelectedRow();
                if (row != -1) {
                    loadRoomForEdit(row);
                }
            }
        });
    }

    private void loadRoomForEdit(int row) {
        DefaultTableModel model = (DefaultTableModel) tableRooms.getModel();
        
        selectedRoomId = Integer.parseInt(model.getValueAt(row, 0).toString());
        txtRoomNumber.setText(model.getValueAt(row, 1).toString());
        
        String roomType = model.getValueAt(row, 2).toString();
        cmbRoomType.setSelectedItem(roomType);
        
        txtCapacity.setText(model.getValueAt(row, 3).toString());
        txtPrice.setText(model.getValueAt(row, 4).toString().replace("₱", "").replace(",", ""));
        cmbStatus.setSelectedItem(model.getValueAt(row, 5).toString());
        
        enterEditMode();
    }
    
    private void enterEditMode() {
        isEditMode = true;
        
        btnAddUpdate.setText("Update");
        btnAddUpdate.setBackground(new Color(251, 191, 36));
        btnAddUpdate.setVisible(true);
        
        btnCancelEdit.setVisible(true);
        btnDelete.setVisible(true);
        
        txtRoomNumber.setEditable(true);
        txtRoomNumber.setBackground(ThemeManager.getInputBackground());
    }
    
    private void cancelEdit() {
        exitEditMode();
        clearFields();
        tableRooms.clearSelection();
        NotificationManager.showInfo(this, "Edit cancelled");
    }
    
    private void exitEditMode() {
        isEditMode = false;
        selectedRoomId = -1;
        
        btnAddUpdate.setText("Add Room");
        btnAddUpdate.setBackground(new Color(34, 197, 94));
        btnCancelEdit.setVisible(false);
        btnDelete.setVisible(false);
        
        txtRoomNumber.setEditable(true);
        txtRoomNumber.setBackground(ThemeManager.getInputBackground());
    }

    private void loadRooms() {
        List<Room> rooms = roomDAO.getAllRooms();
        DefaultTableModel model = (DefaultTableModel) tableRooms.getModel();
        model.setRowCount(0);
        
        int totalCount = rooms.size();
        int availableCount = 0;
        
        for (Room room : rooms) {
            if (!"All".equalsIgnoreCase(currentFilter)) {
                if (!room.getStatus().equalsIgnoreCase(currentFilter)) {
                    continue;
                }
            }
            
            Object[] row = {
                room.getId(),
                room.getRoomNumber(),
                room.getRoomType(),
                room.getCapacity(),
                String.format("₱%.2f", room.getPrice()),
                room.getStatus()
            };
            model.addRow(row);
            
            if ("Available".equalsIgnoreCase(room.getStatus())) {
                availableCount++;
            }
        }
        
        lblTotalRooms.setText(String.valueOf(totalCount));
        lblAvailableRooms.setText(String.valueOf(availableCount));
        
        updateFilterButtons(currentFilter);
    }
    
    private boolean validateFields() {
        if (txtRoomNumber.getText().trim().isEmpty()) {
            NotificationManager.showWarning(this, "Room number is required!");
            txtRoomNumber.requestFocus();
            return false;
        }
        
        if (txtCapacity.getText().trim().isEmpty()) {
            NotificationManager.showWarning(this, "Capacity is required!");
            txtCapacity.requestFocus();
            return false;
        }
        
        if (txtPrice.getText().trim().isEmpty()) {
            NotificationManager.showWarning(this, "Price is required!");
            txtPrice.requestFocus();
            return false;
        }
        
        try {
            int capacity = Integer.parseInt(txtCapacity.getText().trim());
            if (capacity <= 0) {
                NotificationManager.showWarning(this, "Capacity must be greater than 0!");
                txtCapacity.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            NotificationManager.showWarning(this, "Capacity must be a valid number!");
            txtCapacity.requestFocus();
            return false;
        }
        
        try {
            double price = Double.parseDouble(txtPrice.getText().trim());
            if (price <= 0) {
                NotificationManager.showWarning(this, "Price must be greater than 0!");
                txtPrice.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            NotificationManager.showWarning(this, "Price must be a valid number!");
            txtPrice.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private boolean isDuplicateRoomNumber(String roomNumber) {
        List<Room> rooms = roomDAO.getAllRooms();
        for (Room room : rooms) {
            if (isEditMode && room.getId() == selectedRoomId) {
                continue;
            }
            if (room.getRoomNumber().equalsIgnoreCase(roomNumber)) {
                return true;
            }
        }
        return false;
    }
    
    private void addRoom() {
        if (!validateFields()) {
            return;
        }
        
        try {
            String roomNumber = txtRoomNumber.getText().trim();
            
            if (isDuplicateRoomNumber(roomNumber)) {
                NotificationManager.showError(this, "Room number '" + roomNumber + "' already exists!");
                return;
            }
            
            String roomType = (String) cmbRoomType.getSelectedItem();
            int capacity = Integer.parseInt(txtCapacity.getText().trim());
            double price = Double.parseDouble(txtPrice.getText().trim());
            String status = (String) cmbStatus.getSelectedItem();
            String description = "";
            
            Room room = new Room(roomNumber, roomType, capacity, price, status, description);
            boolean added = roomDAO.addRoom(room);
            
            if (added) {
                NotificationManager.showSuccess(this, "Room '" + roomNumber + "' added successfully!");
                clearFields();
                loadRooms();
            } else {
                NotificationManager.showError(this, "Failed to add room!");
            }
        } catch (NumberFormatException ex) {
            NotificationManager.showError(this, "Invalid capacity or price!");
        }
    }
    
    private void updateRoom() {
        if (selectedRoomId == -1) {
            NotificationManager.showWarning(this, "Please select a room to update!");
            return;
        }
        
        if (!validateFields()) {
            return;
        }
        
        try {
            String roomNumber = txtRoomNumber.getText().trim();
            
            if (isDuplicateRoomNumber(roomNumber)) {
                NotificationManager.showError(this, 
                    "Room number '" + roomNumber + "' already exists! Please use a different number.");
                txtRoomNumber.requestFocus();
                return;
            }
            
            String roomType = (String) cmbRoomType.getSelectedItem();
            int capacity = Integer.parseInt(txtCapacity.getText().trim());
            double price = Double.parseDouble(txtPrice.getText().trim());
            String status = (String) cmbStatus.getSelectedItem();
            String description = "";
            
            Room room = new Room(selectedRoomId, roomNumber, roomType, capacity, price, status, description);
            boolean updated = roomDAO.updateRoom(room);
            
            if (updated) {
                NotificationManager.showSuccess(this, "Room '" + roomNumber + "' updated successfully!");
                exitEditMode();
                clearFields();
                loadRooms();
                tableRooms.clearSelection();
            } else {
                NotificationManager.showError(this, "Failed to update room!");
            }
        } catch (NumberFormatException ex) {
            NotificationManager.showError(this, "Invalid capacity or price!");
        }
    }
    
    private void deleteRoom() {
        if (selectedRoomId == -1) {
            NotificationManager.showWarning(this, "Please select a room to delete!");
            return;
        }
        
        String roomNumber = txtRoomNumber.getText().trim();
        String status = (String) cmbStatus.getSelectedItem();
        
        if ("Occupied".equalsIgnoreCase(status)) {
            NotificationManager.showError(this, "Cannot delete occupied room! Remove tenant first.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete Room " + roomNumber + "?\n\nThis action cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        boolean deleted = roomDAO.deleteRoom(selectedRoomId);
        
        if (deleted) {
            NotificationManager.showSuccess(this, "Room '" + roomNumber + "' deleted successfully!");
            exitEditMode();
            clearFields();
            loadRooms();
            tableRooms.clearSelection();
        } else {
            NotificationManager.showError(this, "Failed to delete room!");
        }
    }
    
    private void clearFields() {
        txtRoomNumber.setText("");
        cmbRoomType.setSelectedIndex(0);
        txtCapacity.setText("");
        txtPrice.setText("");
        cmbStatus.setSelectedIndex(0);
        txtRoomNumber.requestFocus();
        
        if (isEditMode) {
            exitEditMode();
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