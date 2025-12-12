import dao.TenantDAO;
import dao.RoomDAO;
import model.Tenant;
import model.Room;
import model.User;
import util.NotificationManager;
import util.ThemeManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.util.List;

public class HouseRent extends JFrame implements ThemeManager.ThemeChangeListener {

    private TenantDAO tenantDAO;
    private RoomDAO roomDAO;
    private User currentUser;
    private JTable tableTenants;
    private JTextField txtName, txtContact, txtSearch;
    private JComboBox<String> cmbMonth, cmbGender, cmbRoomNo;
    private JLabel lblTotalTenants, lblOccupiedRooms;
    private int selectedTenantId = -1;
    private JPanel mainPanel;
    private JScrollPane scrollPane;

    private boolean isNavigating = false;
    
    public HouseRent(User user) {
        this.currentUser = user;
        this.tenantDAO = new TenantDAO();
        this.roomDAO = new RoomDAO();
        
        ThemeManager.addThemeChangeListener(this);
        
        initComponents();
        loadTenants();
        setupTableListener();
        loadAvailableRooms();
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
        
        updateTextFieldTheme(txtName);
        updateTextFieldTheme(txtContact);
        updateTextFieldTheme(txtSearch);
        
        updateComboBoxTheme(cmbMonth);
        updateComboBoxTheme(cmbGender);
        updateComboBoxTheme(cmbRoomNo);
        
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
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void initComponents() {
        setTitle("RentEase - Tenant Management");
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
            if (i == 1) btn.setBackground(ThemeManager.getSidebarHover());
            final int index = i;
            btn.addActionListener(e -> {
                switch (index) {
                    case 0: this.dispose(); new Dashboard(currentUser).setVisible(true); break;
                    case 2: this.dispose(); new AddRoom(currentUser).setVisible(true); break;
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
                if (!btn.getText().contains("Tenant Management")) {
                    btn.setBackground(ThemeManager.getSidebarHover());
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!btn.getText().contains("Tenant Management")) {
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
        
        JLabel lblTitle = new JLabel("Tenant Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(ThemeManager.getTextPrimary());
        lblTitle.setBounds(40, 30, 400, 45);
        mainPanel.add(lblTitle);
        
        createStatCard(mainPanel, "14", "Total Tenants", new Color(59, 130, 246), 40, 110, lblTotalTenants = new JLabel("14"));
        createStatCard(mainPanel, "14", "Occupied Rooms", new Color(34, 197, 94), 305, 110, lblOccupiedRooms = new JLabel("14"));
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
        
        JLabel lblFormTitle = new JLabel("Tenant Details");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblFormTitle.setForeground(ThemeManager.getTextPrimary());
        lblFormTitle.setBounds(20, 20, 205, 25);
        formCard.add(lblFormTitle);
        
        createFormField(formCard, "Tenant Name", 65, txtName = new JTextField());
        createFormField(formCard, "Contact Number", 140, txtContact = new JTextField());
        
        JLabel lblRoomNo = new JLabel("Room Number");
        lblRoomNo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRoomNo.setForeground(ThemeManager.getTextSecondary());
        lblRoomNo.setBounds(20, 215, 205, 20);
        formCard.add(lblRoomNo);
        
        cmbRoomNo = new JComboBox<>();
        cmbRoomNo.setBounds(20, 240, 205, 38);
        cmbRoomNo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbRoomNo.setBackground(ThemeManager.getInputBackground());
        cmbRoomNo.setForeground(ThemeManager.getTextPrimary());
        cmbRoomNo.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1));
        formCard.add(cmbRoomNo);
        
        JLabel lblGender = new JLabel("Gender");
        lblGender.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGender.setForeground(ThemeManager.getTextSecondary());
        lblGender.setBounds(20, 290, 205, 20);
        formCard.add(lblGender);
        cmbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        cmbGender.setBounds(20, 315, 205, 38);
        cmbGender.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbGender.setBackground(ThemeManager.getInputBackground());
        cmbGender.setForeground(ThemeManager.getTextPrimary());
        cmbGender.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1));
        formCard.add(cmbGender);
        
        JLabel lblMonth = new JLabel("Month");
        lblMonth.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMonth.setForeground(ThemeManager.getTextSecondary());
        lblMonth.setBounds(20, 365, 205, 20);
        formCard.add(lblMonth);
        cmbMonth = new JComboBox<>(new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"});
        cmbMonth.setBounds(20, 390, 205, 38);
        cmbMonth.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbMonth.setBackground(ThemeManager.getInputBackground());
        cmbMonth.setForeground(ThemeManager.getTextPrimary());
        cmbMonth.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1));
        formCard.add(cmbMonth);
        
        JButton btnAdd = createActionButton("Add Tenant", new Color(34, 197, 94), 20, 445);
        btnAdd.addActionListener(e -> addTenant());
        formCard.add(btnAdd);
        JButton btnUpdate = createActionButton("Update", new Color(251, 191, 36), 20, 490);
        btnUpdate.addActionListener(e -> updateTenant());
        formCard.add(btnUpdate);
        JButton btnDelete = createActionButton("Delete", new Color(239, 68, 68), 20, 535);
        btnDelete.addActionListener(e -> deleteTenant());
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

    // CONTINUED FROM PART 1 - Add these methods to HouseRent.java

    private void createTablePanel(JPanel parent) {
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSearch.setForeground(ThemeManager.getTextSecondary());
        lblSearch.setBounds(375, 270, 70, 35);
        parent.add(lblSearch);
        
        txtSearch = new JTextField("Search tenants...");
        txtSearch.setBounds(440, 270, 300, 38);
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
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchTenants();
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
        btnSearch.setBounds(750, 270, 50, 38);
        btnSearch.setBackground(new Color(59, 130, 246));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        btnSearch.setBorderPainted(false);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearch.addActionListener(e -> searchTenants());
        parent.add(btnSearch);
        
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBounds(815, 270, 95, 38);
        btnRefresh.setBackground(new Color(148, 163, 184));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> {
            clearFields();
            loadTenants();
            loadAvailableRooms();
        });
        parent.add(btnRefresh);
        
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
        tableTenants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableTenants.setRowSelectionAllowed(true);
        tableTenants.setColumnSelectionAllowed(false);
        
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.setColumnIdentifiers(new String[]{"ID", "Name", "Contact", "Room No", "Gender", "Month", "Payment"});
        tableTenants.setModel(model);
        
        javax.swing.table.JTableHeader header = tableTenants.getTableHeader();
        header.setPreferredSize(new Dimension(0, 50));
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
        header.setOpaque(true);
        
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = new JLabel(value == null ? "" : value.toString());
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lbl.setOpaque(true);
                lbl.setBackground(new Color(59, 130, 246));
                lbl.setForeground(Color.WHITE);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return lbl;
            }
        };
        for (int i = 0; i < tableTenants.getColumnCount(); i++) {
            tableTenants.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tableTenants.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableTenants.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tableTenants.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableTenants.getColumnModel().getColumn(1).setPreferredWidth(150);
        tableTenants.getColumnModel().getColumn(2).setPreferredWidth(120);
        tableTenants.getColumnModel().getColumn(3).setPreferredWidth(80);
        tableTenants.getColumnModel().getColumn(4).setPreferredWidth(80);
        tableTenants.getColumnModel().getColumn(5).setPreferredWidth(100);
        tableTenants.getColumnModel().getColumn(6).setPreferredWidth(100);
        
        scrollPane = new JScrollPane(tableTenants);
        scrollPane.setBounds(305, 330, 990, 560);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1));
        scrollPane.getViewport().setBackground(ThemeManager.getTableBackground());
        parent.add(scrollPane);
    }

    private void setupTableListener() {
        tableTenants.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int viewRow = tableTenants.getSelectedRow();
                if (viewRow != -1) {
                    int modelRow = tableTenants.convertRowIndexToModel(viewRow);
                    DefaultTableModel model = (DefaultTableModel) tableTenants.getModel();
                    Object idObj = model.getValueAt(modelRow, 0);
                    if (idObj != null) {
                        try {
                            selectedTenantId = Integer.parseInt(idObj.toString());
                        } catch (NumberFormatException ex) {
                            selectedTenantId = -1;
                        }
                    } else {
                        selectedTenantId = -1;
                    }
                    txtName.setText(model.getValueAt(modelRow, 1) != null ? model.getValueAt(modelRow, 1).toString() : "");
                    txtContact.setText(model.getValueAt(modelRow, 2) != null ? model.getValueAt(modelRow, 2).toString() : "");
                    
                    String roomNo = model.getValueAt(modelRow, 3) != null ? model.getValueAt(modelRow, 3).toString() : "";
                    
                    for (int i = 0; i < cmbRoomNo.getItemCount(); i++) {
                        String item = cmbRoomNo.getItemAt(i);
                        if (item.startsWith(roomNo + " ") || item.equals(roomNo)) {
                            cmbRoomNo.setSelectedIndex(i);
                            break;
                        }
                    }
                    
                    cmbGender.setSelectedItem(model.getValueAt(modelRow, 4) != null ? model.getValueAt(modelRow, 4).toString() : cmbGender.getItemAt(0));
                    cmbMonth.setSelectedItem(model.getValueAt(modelRow, 5) != null ? model.getValueAt(modelRow, 5).toString() : cmbMonth.getItemAt(0));
                }
            }
        });
    }

    // ✅ UPDATED: Load rooms with tenant count display (Recommendation #2)
    private void loadAvailableRooms() {
        List<Room> rooms = roomDAO.getAllRooms();
        cmbRoomNo.removeAllItems();
        cmbRoomNo.addItem("-- Select Room --");
        
        for (Room room : rooms) {
            int capacity = room.getCapacity();
            int roomNumber = Integer.parseInt(room.getRoomNumber());
            int currentTenants = tenantDAO.countTenantsInRoom(roomNumber);
            
            // ✅ Show rooms that have space (not at full capacity)
            if (currentTenants < capacity) {
                String displayText = room.getRoomNumber() + 
                    " (₱" + String.format("%.2f", room.getPrice()) + 
                    ") [" + currentTenants + "/" + capacity + " tenants]";
                cmbRoomNo.addItem(displayText);
            }
        }
    }
    
    private void loadTenants() {
        List<Tenant> tenants = tenantDAO.getAllTenants();
        DefaultTableModel model = (DefaultTableModel) tableTenants.getModel();
        model.setRowCount(0);
        for (Tenant tenant : tenants) {
            String gender = tenant.getGender();
            if (gender == null || gender.trim().isEmpty()) {
                gender = "Not Set";
            }
            
            Object[] row = {
                tenant.getTenantId(), 
                tenant.getName(), 
                tenant.getContact(), 
                tenant.getRoomNumber(), 
                gender,
                tenant.getMonth(), 
                String.format("₱%.2f", tenant.getPayment())
            };
            model.addRow(row);
        }
        lblTotalTenants.setText(String.valueOf(tenants.size()));
        
        // ✅ Count unique occupied rooms
        int occupiedRooms = (int) tenants.stream()
            .map(Tenant::getRoomNumber)
            .distinct()
            .count();
        lblOccupiedRooms.setText(String.valueOf(occupiedRooms));
    }

    // ✅ UPDATED: Add tenant with capacity checking (Recommendation #2)
    private void addTenant() {
        if (txtName.getText().trim().isEmpty() || cmbRoomNo.getSelectedItem() == null || 
            cmbRoomNo.getSelectedItem().toString().equals("-- Select Room --")) {
            NotificationManager.showWarning(this, "Please fill all required fields!");
            return;
        }
        
        try {
            String name = txtName.getText().trim();
            String contact = txtContact.getText().trim();
            
            String roomSelection = cmbRoomNo.getSelectedItem().toString();
            String roomNoStr = roomSelection.split(" ")[0];
            int roomNo = Integer.parseInt(roomNoStr);
            
            // ✅ NEW: Check if room can accommodate more tenants
            if (!tenantDAO.canAddTenantToRoom(roomNo)) {
                int capacity = tenantDAO.getRoomCapacity(roomNo);
                int current = tenantDAO.countTenantsInRoom(roomNo);
                NotificationManager.showError(this, 
                    "Room " + roomNo + " is at full capacity!\n\n" +
                    "Current tenants: " + current + "\n" +
                    "Max capacity: " + capacity + "\n\n" +
                    "Cannot add more tenants to this room.");
                return;
            }
            
            String month = (String) cmbMonth.getSelectedItem();
            String gender = (String) cmbGender.getSelectedItem();
            
            double payment = tenantDAO.getRoomPrice(roomNo);
            if (payment == 0.0) {
                NotificationManager.showError(this, "Room not found or has no price set!");
                return;
            }
            
            Tenant tenant = new Tenant(name, contact, roomNo, payment, month, gender);
            boolean added = tenantDAO.addTenant(tenant);
            
            if (added) {
                // ✅ Get updated tenant count
                int tenantCount = tenantDAO.countTenantsInRoom(roomNo);
                int capacity = tenantDAO.getRoomCapacity(roomNo);
                
                // ✅ Show success with tenant count info
                NotificationManager.showSuccess(this, 
                    "Tenant '" + name + "' added successfully to Room " + roomNo + "!\n\n" +
                    "Room occupancy: " + tenantCount + "/" + capacity + " tenants");
                
                clearFields();
                loadTenants();
                loadAvailableRooms();
            } else {
                NotificationManager.showError(this, "Failed to add tenant!");
            }
        } catch (NumberFormatException ex) {
            NotificationManager.showWarning(this, "Invalid room number! Please select a valid room.");
        } catch (Exception ex) {
            NotificationManager.showError(this, "An error occurred: " + ex.getMessage());
        }
    }

    private void updateTenant() {
        if (selectedTenantId == -1) {
            NotificationManager.showWarning(this, "Please select a tenant from the table to update!");
            return;
        }
        if (txtName.getText().trim().isEmpty() || cmbRoomNo.getSelectedItem() == null || 
            cmbRoomNo.getSelectedItem().toString().equals("-- Select Room --")) {
            NotificationManager.showWarning(this, "Please fill all required fields!");
            return;
        }
        try {
            String name = txtName.getText().trim();
            String contact = txtContact.getText().trim();
            
            String roomSelection = cmbRoomNo.getSelectedItem().toString();
            String roomNoStr = roomSelection.contains("(") ? roomSelection.split(" ")[0] : roomSelection;
            int roomNo = Integer.parseInt(roomNoStr);
            
            String month = (String) cmbMonth.getSelectedItem();
            String gender = (String) cmbGender.getSelectedItem();
            
            double payment = tenantDAO.getRoomPrice(roomNo);
            
            Tenant tenant = new Tenant(selectedTenantId, name, contact, roomNo, payment, month, gender);
            boolean updated = tenantDAO.updateTenant(tenant);
            
            if (updated) {
                NotificationManager.showSuccess(this, "Tenant '" + name + "' updated successfully!");
                clearFields();
                loadTenants();
                loadAvailableRooms();
                selectedTenantId = -1;
            } else {
                NotificationManager.showError(this, "Failed to update tenant!");
            }
        } catch (NumberFormatException ex) {
            NotificationManager.showWarning(this, "Invalid room number! Please select a valid room.");
        } catch (Exception ex) {
            NotificationManager.showError(this, "An error occurred: " + ex.getMessage());
        }
    }

    private void deleteTenant() {
        if (selectedTenantId == -1) {
            NotificationManager.showWarning(this, "Please select a tenant from the table to delete!");
            return;
        }
        
        String tenantName = txtName.getText().trim();
        String roomSelection = cmbRoomNo.getSelectedItem() != null ? cmbRoomNo.getSelectedItem().toString() : "";
        String roomNoDisplay = roomSelection.contains("(") ? roomSelection.split(" ")[0] : roomSelection;
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this tenant?\n\nTenant: " + tenantName + "\nRoom: " + roomNoDisplay, 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            boolean deleted = tenantDAO.deleteTenantById(selectedTenantId);
            if (deleted) {
                NotificationManager.showSuccess(this, "Tenant '" + tenantName + "' deleted successfully!");
                clearFields();
                loadTenants();
                loadAvailableRooms();
                selectedTenantId = -1;
            } else {
                NotificationManager.showError(this, "Failed to delete tenant. Please try again.");
            }
        } catch (Exception ex) {
            NotificationManager.showError(this, "An error occurred: " + ex.getMessage());
        }
    }

    private void searchTenants() {
        String searchValue = txtSearch.getText().trim();
        if (searchValue.isEmpty() || searchValue.equals("Search tenants...")) {
            loadTenants();
            return;
        }
        List<Tenant> results = tenantDAO.searchTenants(searchValue);
        DefaultTableModel model = (DefaultTableModel) tableTenants.getModel();
        model.setRowCount(0);
        for (Tenant tenant : results) {
            String gender = tenant.getGender();
            if (gender == null || gender.trim().isEmpty()) {
                gender = "Not Set";
            }
            
            Object[] row = {
                tenant.getTenantId(), 
                tenant.getName(), 
                tenant.getContact(), 
                tenant.getRoomNumber(), 
                gender,
                tenant.getMonth(), 
                String.format("₱%.2f", tenant.getPayment())
            };
            model.addRow(row);
        }
    }

    private void clearFields() {
        txtName.setText("");
        txtContact.setText("");
        cmbRoomNo.setSelectedIndex(0);
        txtSearch.setText("Search tenants...");
        txtSearch.setForeground(Color.GRAY);
        cmbGender.setSelectedIndex(0);
        cmbMonth.setSelectedIndex(0);
        selectedTenantId = -1;
        tableTenants.clearSelection();
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            ThemeManager.removeThemeChangeListener(this);
            this.dispose();
            new Login().setVisible(true);
        }
    }
}