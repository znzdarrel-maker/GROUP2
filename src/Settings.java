import util.ThemeManager;
import model.User;
import util.NotificationManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.util.prefs.Preferences;

public class Settings extends JFrame implements ThemeManager.ThemeChangeListener {
    
    private User currentUser;
    private Preferences prefs;
    
    // Primary Colors
    private Color primaryBlue = new Color(59, 130, 246);
    private Color primaryGreen = new Color(34, 197, 94);
    private Color primaryYellow = new Color(251, 191, 36);
    private Color primaryRed = new Color(239, 68, 68);
    private Color primaryPurple = new Color(147, 51, 234);
    
    // Form fields
    private JTextField txtFullName, txtEmail, txtPhone;
    private JTextField txtPropertyName, txtAddress, txtUnits;
    private JComboBox<String> cmbCurrency, cmbTimezone;
    private JPasswordField txtCurrentPassword, txtNewPassword, txtConfirmPassword;
    private JPanel toggle2FA;
    private boolean is2FAEnabled = false;
    
    // UI References
    private JPanel contentArea;
    private JPanel mainPanel;
    private JButton[] tabButtons;
    private JLabel lblTitle, lblSubtitle;
    
    // Theme toggle buttons
    private JButton btnLight, btnDark;
    private JLabel lblLightText, lblDarkText;
    
    public Settings(User user) {
        this.currentUser = user;
        this.prefs = Preferences.userNodeForPackage(Settings.class);
        
        ThemeManager.addThemeChangeListener(this);
        
        initComponents();
        loadUserData();
        applyTheme();
    }
    
    @Override
    public void onThemeChanged(boolean isDark) {
        applyTheme();
    }
    
    private void applyTheme() {
        mainPanel.setBackground(ThemeManager.getMainPanelColor());
        
        // Update title colors
        lblTitle.setForeground(ThemeManager.getTextPrimary());
        lblSubtitle.setForeground(ThemeManager.getTextSecondary());
        
        // Update all text fields
        updateTextFieldTheme(txtFullName);
        updateTextFieldTheme(txtEmail);
        updateTextFieldTheme(txtPhone);
        updateTextFieldTheme(txtPropertyName);
        updateTextFieldTheme(txtAddress);
        updateTextFieldTheme(txtUnits);
        updateTextFieldTheme(txtCurrentPassword);
        updateTextFieldTheme(txtNewPassword);
        updateTextFieldTheme(txtConfirmPassword);
        
        // Update combo boxes
        updateComboBoxTheme(cmbCurrency);
        updateComboBoxTheme(cmbTimezone);
        
        // Update theme buttons appearance
        if (btnLight != null && btnDark != null) {
            btnLight.repaint();
            btnDark.repaint();
            lblLightText.setForeground(ThemeManager.isDarkMode() ? ThemeManager.getTextPrimary() : Color.WHITE);
            lblDarkText.setForeground(ThemeManager.isDarkMode() ? Color.WHITE : ThemeManager.getTextPrimary());
        }
        
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    private void updateTextFieldTheme(JComponent field) {
        if (field == null) return;
        field.setBackground(ThemeManager.getInputBackground());
        field.setForeground(ThemeManager.getTextPrimary());
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
    }
    
    private void updateComboBoxTheme(JComboBox<?> combo) {
        if (combo == null) return;
        combo.setBackground(ThemeManager.getInputBackground());
        combo.setForeground(ThemeManager.getTextPrimary());
        combo.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1));
    }
    
    private void loadUserData() {
        txtFullName.setText(currentUser.getFullName());
        txtEmail.setText(prefs.get("userEmail_" + currentUser.getUserId(), ""));
        txtPhone.setText(prefs.get("userPhone_" + currentUser.getUserId(), ""));
        txtPropertyName.setText(prefs.get("propertyName", ""));
        txtAddress.setText(prefs.get("propertyAddress", ""));
        txtUnits.setText(prefs.get("totalUnits", ""));
        
        String savedCurrency = prefs.get("currency", "USD - US Dollar");
        cmbCurrency.setSelectedItem(savedCurrency);
        
        String savedTimezone = prefs.get("timezone", "Eastern Time (ET)");
        cmbTimezone.setSelectedItem(savedTimezone);
        
        is2FAEnabled = prefs.getBoolean("2faEnabled", false);
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
        setTitle("RentEase - Settings");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);
        
        mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(ThemeManager.getMainPanelColor());
        add(mainPanel, BorderLayout.CENTER);
        
        createSettingsContent();
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(235, 850));
        sidebar.setLayout(null);
        sidebar.setBackground(ThemeManager.getSidebarColor());
        
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
        userPanel.setBounds(18, 110, 199, 65);
        userPanel.setLayout(null);
        userPanel.setBackground(ThemeManager.getSidebarHover());
        userPanel.setBorder(BorderFactory.createLineBorder(new Color(71, 85, 105), 1));
        
        String displayName = currentUser.getFullName();
        if (displayName.length() > 18) {
            displayName = displayName.substring(0, 15) + "...";
        }
        
        JLabel lblWelcome = new JLabel("Welcome, " + displayName);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblWelcome.setForeground(Color.WHITE);
        lblWelcome.setBounds(10, 10, 180, 20);
        userPanel.add(lblWelcome);
        
        JLabel lblActive = new JLabel("VIEW ACTIVE");
        lblActive.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblActive.setForeground(new Color(74, 222, 128));
        lblActive.setBounds(10, 35, 180, 15);
        userPanel.add(lblActive);
        
        sidebar.add(userPanel);
        
        int yPos = 210;
        String[] menuItems = {"Dashboard", "Tenant Management", "Room Management", "Payment Records", "Settings"};
        String[] iconFiles = {"dashboard.png", "tenant.png", "room.png", "peso.png", "user.png"};
        
        for (int i = 0; i < menuItems.length; i++) {
            JButton btn = createMenuButton(menuItems[i], iconFiles[i], yPos, i);
            if (i == 4) btn.setBackground(ThemeManager.getSidebarHover());
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
        btnLogout.setBackground(primaryRed);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> logout());
        sidebar.add(btnLogout);
        
        return sidebar;
    }
    
    private JButton createMenuButton(String text, String iconFile, int yPos, int index) {
        JButton btn = new JButton();
        btn.setBounds(18, yPos, 199, 45);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(ThemeManager.getSidebarColor());
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
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
        
        final int buttonIndex = index;
        btn.addActionListener(e -> {
            switch(buttonIndex) {
                case 0: this.dispose(); new Dashboard(currentUser).setVisible(true); break;
                case 1: this.dispose(); new HouseRent(currentUser).setVisible(true); break;
                case 2: this.dispose(); new AddRoom(currentUser).setVisible(true); break;
                case 3: this.dispose(); new PaymentRecords(currentUser).setVisible(true); break;
            }
        });
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!btn.getText().contains("Settings")) {
                    btn.setBackground(ThemeManager.getSidebarHover());
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!btn.getText().contains("Settings")) {
                    btn.setBackground(ThemeManager.getSidebarColor());
                }
            }
        });
        
        return btn;
    }
    
// CONTINUED FROM PART 1
    
    private void createSettingsContent() {
        lblTitle = new JLabel("Settings");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(ThemeManager.getTextPrimary());
        lblTitle.setBounds(40, 30, 400, 45);
        mainPanel.add(lblTitle);
        
        lblSubtitle = new JLabel("Manage your account, property, and preferences");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(ThemeManager.getTextSecondary());
        lblSubtitle.setBounds(40, 75, 500, 20);
        mainPanel.add(lblSubtitle);
        
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(null);
        leftPanel.setBackground(ThemeManager.getCardBackground());
        leftPanel.setBounds(40, 120, 200, 650);
        leftPanel.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1));
        
        String[] tabs = {"Profile", "Property", "Security", "Appearance", "Data"};
        String[] icons = {"user.png", "logo.png", "lock.png", "color.png", "data.png"};
        tabButtons = new JButton[tabs.length];
        
        int tabY = 20;
        for (int i = 0; i < tabs.length; i++) {
            JButton tabBtn = createVerticalTabButton(tabs[i], icons[i], tabY, i);
            leftPanel.add(tabBtn);
            tabButtons[i] = tabBtn;
            tabY += 70;
        }
        
        mainPanel.add(leftPanel);
        
        contentArea = new JPanel();
        contentArea.setLayout(new CardLayout());
        contentArea.setBounds(260, 120, 1010, 650);
        contentArea.setOpaque(false);
        
        contentArea.add(createProfilePanel(), "Profile");
        contentArea.add(createPropertyPanel(), "Property");
        contentArea.add(createSecurityPanel(), "Security");
        contentArea.add(createAppearancePanel(), "Appearance");
        contentArea.add(createDataPanel(), "Data");
        
        mainPanel.add(contentArea);
        
        switchTab(0, "Profile");
    }
    
    private JButton createVerticalTabButton(String text, String iconFile, int y, int tabIndex) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Boolean isSelected = (Boolean) getClientProperty("selected");
                if (isSelected != null && isSelected) {
                    g2d.setColor(new Color(239, 246, 255));
                    g2d.fillRoundRect(8, 5, getWidth() - 16, getHeight() - 10, 10, 10);
                }
                
                super.paintComponent(g);
            }
        };
        
        btn.setBounds(10, y, 180, 60);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(ThemeManager.getCardBackground());
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.putClientProperty("selected", tabIndex == 0);
        btn.setForeground(tabIndex == 0 ? primaryBlue : ThemeManager.getTextSecondary());
        
        ImageIcon icon = loadIcon(iconFile, 20, 20);
        if (icon != null) {
            btn.setIcon(icon);
            btn.setText("  " + text);
            btn.setIconTextGap(12);
        } else {
            btn.setText(text);
        }
        
        final int index = tabIndex;
        btn.addActionListener(e -> switchTab(index, text));
        
        return btn;
    }
    
    private void switchTab(int tabIndex, String tabName) {
        for (int i = 0; i < tabButtons.length; i++) {
            boolean isSelected = (i == tabIndex);
            tabButtons[i].putClientProperty("selected", isSelected);
            tabButtons[i].setForeground(isSelected ? primaryBlue : ThemeManager.getTextSecondary());
            tabButtons[i].repaint();
        }
        
        CardLayout cl = (CardLayout) contentArea.getLayout();
        cl.show(contentArea, tabName);
    }
    
    private JPanel createSettingsCard(String title) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        
        card.setLayout(null);
        card.setOpaque(false);
        card.setBackground(ThemeManager.getCardBackground());
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(ThemeManager.getTextPrimary());
        lblTitle.setBounds(32, 28, 900, 30);
        card.add(lblTitle);
        
        JSeparator sep = new JSeparator();
        sep.setBounds(32, 68, 946, 1);
        sep.setForeground(ThemeManager.getSeparatorColor());
        card.add(sep);
        
        return card;
    }
    
    private void createLabel(JPanel parent, String text, int y) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(ThemeManager.getTextSecondary());
        label.setBounds(32, y, 946, 18);
        parent.add(label);
    }
    
    private JTextField createTextField(JPanel parent, int y) {
        JTextField textField = new JTextField();
        textField.setBounds(32, y, 946, 40);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBackground(ThemeManager.getInputBackground());
        textField.setForeground(ThemeManager.getTextPrimary());
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        parent.add(textField);
        return textField;
    }
    
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setOpaque(false);
        
        JPanel card = createSettingsCard("Profile Settings");
        card.setBounds(0, 0, 1010, 440);
        
        createLabel(card, "Full Name", 100);
        txtFullName = createTextField(card, 125);
        
        createLabel(card, "Email Address", 180);
        txtEmail = createTextField(card, 205);
        
        createLabel(card, "Phone Number", 260);
        txtPhone = createTextField(card, 285);
        
        createLabel(card, "Role", 340);
        JLabel lblRole = new JLabel(currentUser.getRole());
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblRole.setForeground(ThemeManager.getTextSecondary());
        lblRole.setBounds(32, 365, 946, 40);
        lblRole.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        lblRole.setOpaque(true);
        lblRole.setBackground(ThemeManager.isDarkMode() ? new Color(51, 65, 85) : new Color(249, 250, 251));
        card.add(lblRole);
        
        panel.add(card);
        
        JButton btnSave = new JButton("💾 Save Changes");
        btnSave.setBounds(32, 460, 180, 45);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setBackground(primaryBlue);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setBorderPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> saveProfile());
        panel.add(btnSave);
        
        return panel;
    }
    
    private JPanel createPropertyPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setOpaque(false);
        
        JPanel card = createSettingsCard("Property Settings");
        card.setBounds(0, 0, 1010, 520);
        
        createLabel(card, "Property Name", 100);
        txtPropertyName = createTextField(card, 125);
        
        createLabel(card, "Address", 180);
        txtAddress = createTextField(card, 205);
        
        createLabel(card, "Total Units", 260);
        txtUnits = createTextField(card, 285);
        
        createLabel(card, "Currency", 340);
        cmbCurrency = new JComboBox<>(new String[]{
            "USD - US Dollar", "PHP - Philippine Peso", "EUR - Euro", "GBP - British Pound"
        });
        cmbCurrency.setBounds(32, 365, 946, 40);
        cmbCurrency.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbCurrency.setBackground(ThemeManager.getInputBackground());
        cmbCurrency.setForeground(ThemeManager.getTextPrimary());
        cmbCurrency.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1));
        card.add(cmbCurrency);
        
        createLabel(card, "Timezone", 420);
        cmbTimezone = new JComboBox<>(new String[]{
            "Eastern Time (ET)", "Pacific Time (PT)", "Philippine Time (PHT)", "UTC"
        });
        cmbTimezone.setBounds(32, 445, 946, 40);
        cmbTimezone.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbTimezone.setBackground(ThemeManager.getInputBackground());
        cmbTimezone.setForeground(ThemeManager.getTextPrimary());
        cmbTimezone.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1));
        card.add(cmbTimezone);
        
        panel.add(card);
        
        JButton btnSave = new JButton("💾 Save Changes");
        btnSave.setBounds(32, 540, 180, 45);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setBackground(primaryGreen);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setBorderPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> saveProperty());
        panel.add(btnSave);
        
        return panel;
    }
    
    private JPanel createSecurityPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setOpaque(false);
        
        JPanel card = createSettingsCard("Security Settings");
        card.setBounds(0, 0, 1010, 500);
        
        createLabel(card, "Current Password", 100);
        txtCurrentPassword = new JPasswordField();
        txtCurrentPassword.setBounds(32, 125, 946, 40);
        txtCurrentPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtCurrentPassword.setBackground(ThemeManager.getInputBackground());
        txtCurrentPassword.setForeground(ThemeManager.getTextPrimary());
        txtCurrentPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        card.add(txtCurrentPassword);
        
        createLabel(card, "New Password", 180);
        txtNewPassword = new JPasswordField();
        txtNewPassword.setBounds(32, 205, 946, 40);
        txtNewPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNewPassword.setBackground(ThemeManager.getInputBackground());
        txtNewPassword.setForeground(ThemeManager.getTextPrimary());
        txtNewPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        card.add(txtNewPassword);
        
        createLabel(card, "Confirm New Password", 260);
        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.setBounds(32, 285, 946, 40);
        txtConfirmPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtConfirmPassword.setBackground(ThemeManager.getInputBackground());
        txtConfirmPassword.setForeground(ThemeManager.getTextPrimary());
        txtConfirmPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        card.add(txtConfirmPassword);
        
        // 2FA Section
        JPanel twoFAPanel = new JPanel();
        twoFAPanel.setLayout(null);
        twoFAPanel.setBounds(32, 345, 946, 115);
        twoFAPanel.setBackground(ThemeManager.isDarkMode() ? new Color(51, 65, 85) : new Color(249, 250, 251));
        twoFAPanel.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1));
        
        JLabel lbl2FA = new JLabel("Two-Factor Authentication");
        lbl2FA.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl2FA.setForeground(ThemeManager.getTextPrimary());
        lbl2FA.setBounds(20, 18, 700, 25);
        twoFAPanel.add(lbl2FA);
        
        JLabel lblDesc = new JLabel("Add an extra layer of security to your account");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(ThemeManager.getTextSecondary());
        lblDesc.setBounds(20, 48, 700, 20);
        twoFAPanel.add(lblDesc);
        
        toggle2FA = create2FAToggle();
        toggle2FA.setBounds(840, 35, 90, 40);
        twoFAPanel.add(toggle2FA);
        
        card.add(twoFAPanel);
        panel.add(card);
        
        JButton btnSave = new JButton("🔒 Update Password");
        btnSave.setBounds(32, 520, 180, 45);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setBackground(primaryYellow);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setBorderPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> updatePassword());
        panel.add(btnSave);
        
        return panel;
    }
// CONTINUED FROM PART 2 - Appearance Panel with GLOBAL theme toggle
    
    private JPanel createAppearancePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setOpaque(false);
        
        JPanel card = createSettingsCard("Appearance Settings");
        card.setBounds(0, 0, 1010, 280);
        
        JLabel lblTheme = new JLabel("Theme");
        lblTheme.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTheme.setForeground(ThemeManager.getTextPrimary());
        lblTheme.setBounds(32, 95, 400, 25);
        card.add(lblTheme);
        
        // Light Mode Button
        btnLight = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!ThemeManager.isDarkMode()) {
                    g2d.setColor(primaryBlue);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                } else {
                    g2d.setColor(ThemeManager.getCardBackground());
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2d.setColor(ThemeManager.getInputBorder());
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
                }
                super.paintComponent(g);
            }
        };
        
        btnLight.setBounds(32, 140, 450, 90);
        btnLight.setLayout(null);
        btnLight.setFocusPainted(false);
        btnLight.setContentAreaFilled(false);
        btnLight.setBorderPainted(false);
        btnLight.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel lblLightIcon = new JLabel("☀️");
        lblLightIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        lblLightIcon.setBounds(180, 8, 90, 50);
        btnLight.add(lblLightIcon);
        
        lblLightText = new JLabel("Light Mode");
        lblLightText.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblLightText.setForeground(!ThemeManager.isDarkMode() ? Color.WHITE : ThemeManager.getTextPrimary());
        lblLightText.setBounds(0, 62, 450, 20);
        lblLightText.setHorizontalAlignment(SwingConstants.CENTER);
        btnLight.add(lblLightText);
        
        // Dark Mode Button
        btnDark = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (ThemeManager.isDarkMode()) {
                    g2d.setColor(primaryBlue);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                } else {
                    g2d.setColor(ThemeManager.getCardBackground());
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2d.setColor(ThemeManager.getInputBorder());
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
                }
                super.paintComponent(g);
            }
        };
        
        btnDark.setBounds(528, 140, 450, 90);
        btnDark.setLayout(null);
        btnDark.setFocusPainted(false);
        btnDark.setContentAreaFilled(false);
        btnDark.setBorderPainted(false);
        btnDark.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel lblDarkIcon = new JLabel("🌙");
        lblDarkIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        lblDarkIcon.setBounds(180, 8, 90, 50);
        btnDark.add(lblDarkIcon);
        
        lblDarkText = new JLabel("Dark Mode");
        lblDarkText.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblDarkText.setForeground(ThemeManager.isDarkMode() ? Color.WHITE : ThemeManager.getTextPrimary());
        lblDarkText.setBounds(0, 62, 450, 20);
        lblDarkText.setHorizontalAlignment(SwingConstants.CENTER);
        btnDark.add(lblDarkText);
        
        // ✅ GLOBAL THEME TOGGLE - Changes theme everywhere!
        btnLight.addActionListener(e -> {
            ThemeManager.setTheme(false); // Light mode
            NotificationManager.showSuccess(this, "Theme changed to Light mode across all panels!");
        });
        
        btnDark.addActionListener(e -> {
            ThemeManager.setTheme(true); // Dark mode
            NotificationManager.showSuccess(this, "Theme changed to Dark mode across all panels!");
        });
        
        card.add(btnLight);
        card.add(btnDark);
        panel.add(card);
        
        JButton btnSave = new JButton("💾 Save Changes");
        btnSave.setBounds(32, 300, 180, 45);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setBackground(primaryPurple);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setBorderPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> NotificationManager.showSuccess(this, "Theme preferences saved!"));
        panel.add(btnSave);
        
        return panel;
    }
    
    private JPanel createDataPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setOpaque(false);
        
        JPanel card = createSettingsCard("Data Management");
        card.setBounds(0, 0, 1010, 550);
        
        JLabel lblWarning = new JLabel("⚠️  Manage and reset your application data. These actions are permanent and cannot be undone.");
        lblWarning.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblWarning.setForeground(new Color(120, 53, 15));
        lblWarning.setBounds(32, 95, 946, 25);
        card.add(lblWarning);
        
        // Reset Payments Panel
        JPanel paymentPanel = new JPanel();
        paymentPanel.setLayout(null);
        paymentPanel.setBounds(32, 140, 430, 110);
        paymentPanel.setBackground(new Color(254, 243, 199));
        paymentPanel.setBorder(BorderFactory.createLineBorder(new Color(251, 191, 36), 2));
        
        JLabel lblPayIcon = new JLabel("💳");
        lblPayIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        lblPayIcon.setBounds(20, 18, 40, 40);
        paymentPanel.add(lblPayIcon);
        
        JLabel lblPaymentTitle = new JLabel("Reset Payments");
        lblPaymentTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblPaymentTitle.setForeground(new Color(120, 53, 15));
        lblPaymentTitle.setBounds(65, 20, 300, 22);
        paymentPanel.add(lblPaymentTitle);
        
        JLabel lblPaymentDesc = new JLabel("Clear payment records only");
        lblPaymentDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPaymentDesc.setForeground(new Color(120, 53, 15));
        lblPaymentDesc.setBounds(65, 50, 300, 18);
        paymentPanel.add(lblPaymentDesc);
        
        JButton btnResetPayments = new JButton("Reset");
        btnResetPayments.setBounds(340, 32, 75, 40);
        btnResetPayments.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnResetPayments.setBackground(primaryYellow);
        btnResetPayments.setForeground(Color.WHITE);
        btnResetPayments.setFocusPainted(false);
        btnResetPayments.setBorderPainted(false);
        btnResetPayments.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnResetPayments.addActionListener(e -> confirmResetPayments());
        paymentPanel.add(btnResetPayments);
        
        card.add(paymentPanel);
        
        // Reset All Data Panel
        JPanel allDataPanel = new JPanel();
        allDataPanel.setLayout(null);
        allDataPanel.setBounds(478, 140, 430, 110);
        allDataPanel.setBackground(new Color(254, 226, 226));
        allDataPanel.setBorder(BorderFactory.createLineBorder(primaryRed, 2));
        
        JLabel lblAllIcon = new JLabel("🗑️");
        lblAllIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        lblAllIcon.setBounds(20, 18, 40, 40);
        allDataPanel.add(lblAllIcon);
        
        JLabel lblAllTitle = new JLabel("Reset All Data");
        lblAllTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblAllTitle.setForeground(new Color(127, 29, 29));
        lblAllTitle.setBounds(65, 20, 300, 22);
        allDataPanel.add(lblAllTitle);
        
        JLabel lblAllDesc = new JLabel("Delete everything permanently");
        lblAllDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAllDesc.setForeground(new Color(127, 29, 29));
        lblAllDesc.setBounds(65, 50, 300, 18);
        allDataPanel.add(lblAllDesc);
        
        JButton btnResetAll = new JButton("Reset");
        btnResetAll.setBounds(340, 32, 75, 40);
        btnResetAll.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnResetAll.setBackground(primaryRed);
        btnResetAll.setForeground(Color.WHITE);
        btnResetAll.setFocusPainted(false);
        btnResetAll.setBorderPainted(false);
        btnResetAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnResetAll.addActionListener(e -> confirmResetAll());
        allDataPanel.add(btnResetAll);
        
        card.add(allDataPanel);
        panel.add(card);
        
        return panel;
    }
    
    // Helper methods and database operations (keep the same as original)
    private JPanel create2FAToggle() {
        JPanel toggle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(is2FAEnabled ? new Color(34, 197, 94) : new Color(203, 213, 225));
                g2d.fillRoundRect(0, 0, 80, 40, 40, 40);
                
                g2d.setColor(Color.WHITE);
                int thumbX = is2FAEnabled ? 44 : 4;
                g2d.fillOval(thumbX, 4, 32, 32);
            }
        };
        
        toggle.setPreferredSize(new Dimension(80, 40));
        toggle.setOpaque(false);
        toggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        toggle.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                is2FAEnabled = !is2FAEnabled;
                prefs.putBoolean("2faEnabled", is2FAEnabled);
                toggle.repaint();
                NotificationManager.showSuccess(Settings.this, 
                    "Two-Factor Authentication " + (is2FAEnabled ? "enabled" : "disabled"));
            }
        });
        
        return toggle;
    }
    
    // Keep all remaining methods (saveProfile, saveProperty, updatePassword, etc.) same as original
    // Add these methods from original file...
    
    private void saveProfile() {
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        
        if (fullName.isEmpty()) {
            NotificationManager.showError(this, "Full Name is required!");
            return;
        }
        
        try {
            String url = "jdbc:mysql://localhost/houserent";
            String user = "root";
            String pass = "";
            
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                String sql = "UPDATE users SET full_name = ? WHERE user_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, fullName);
                    pstmt.setInt(2, currentUser.getUserId());
                    pstmt.executeUpdate();
                    
                    currentUser.setFullName(fullName);
                    prefs.put("userEmail_" + currentUser.getUserId(), email);
                    prefs.put("userPhone_" + currentUser.getUserId(), phone);
                    
                    NotificationManager.showSuccess(this, "Profile updated successfully!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            NotificationManager.showError(this, "Failed to update profile: " + e.getMessage());
        }
    }
    
    private void saveProperty() {
        String propertyName = txtPropertyName.getText().trim();
        String address = txtAddress.getText().trim();
        String units = txtUnits.getText().trim();
        String currency = (String) cmbCurrency.getSelectedItem();
        String timezone = (String) cmbTimezone.getSelectedItem();
        
        prefs.put("propertyName", propertyName);
        prefs.put("propertyAddress", address);
        prefs.put("totalUnits", units);
        prefs.put("currency", currency);
        prefs.put("timezone", timezone);
        
        NotificationManager.showSuccess(this, "Property settings saved successfully!");
    }
    
    private void updatePassword() {
        String currentPass = new String(txtCurrentPassword.getPassword());
        String newPass = new String(txtNewPassword.getPassword());
        String confirmPass = new String(txtConfirmPassword.getPassword());
        
        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            NotificationManager.showError(this, "All password fields are required!");
            return;
        }
        
        if (!newPass.equals(confirmPass)) {
            NotificationManager.showError(this, "New passwords do not match!");
            return;
        }
        
        if (newPass.length() < 4) {
            NotificationManager.showError(this, "Password must be at least 4 characters!");
            return;
        }
        
        try {
            String url = "jdbc:mysql://localhost/houserent";
            String user = "root";
            String pass = "";
            
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                String checkSql = "SELECT password FROM users WHERE user_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                    pstmt.setInt(1, currentUser.getUserId());
                    ResultSet rs = pstmt.executeQuery();
                    
                    if (rs.next()) {
                        String dbPassword = rs.getString("password");
                        if (!dbPassword.equals(currentPass)) {
                            NotificationManager.showError(this, "Current password is incorrect!");
                            return;
                        }
                    }
                }
                
                String updateSql = "UPDATE users SET password = ? WHERE user_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    pstmt.setString(1, newPass);
                    pstmt.setInt(2, currentUser.getUserId());
                    pstmt.executeUpdate();
                    
                    txtCurrentPassword.setText("");
                    txtNewPassword.setText("");
                    txtConfirmPassword.setText("");
                    
                    NotificationManager.showSuccess(this, "Password updated successfully!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            NotificationManager.showError(this, "Failed to update password: " + e.getMessage());
        }
    }
    
    private void confirmResetPayments() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete ALL payment records?",
            "Confirm Reset Payments",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = resetPaymentsOnly();
            if (success) {
                NotificationManager.showSuccess(this, "Payment records reset successfully!");
            } else {
                NotificationManager.showError(this, "Failed to reset payment records.");
            }
        }
    }
    
    private void confirmResetAll() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "⚠️ Are you ABSOLUTELY SURE you want to delete ALL system data?",
            "Confirm Reset All Data",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String input = JOptionPane.showInputDialog(this,
                "Type DELETE to confirm:",
                "Final Confirmation",
                JOptionPane.WARNING_MESSAGE);
            
            if ("DELETE".equals(input)) {
                boolean success = resetAllData();
                if (success) {
                    NotificationManager.showSuccess(this, "All system data reset successfully!");
                } else {
                    NotificationManager.showError(this, "Failed to reset system data.");
                }
            }
        }
    }
    
    private boolean resetPaymentsOnly() {
        String url = "jdbc:mysql://localhost/houserent";
        String user = "root";
        String pass = "";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String sql = "DELETE FROM payments";
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean resetAllData() {
        String url = "jdbc:mysql://localhost/houserent";
        String user = "root";
        String pass = "";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            conn.setAutoCommit(false);
            
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM payments");
                stmt.executeUpdate("DELETE FROM records");
                stmt.executeUpdate("UPDATE rooms SET status = 'Available'");
                
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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
    }}
