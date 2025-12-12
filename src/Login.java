import dao.UserDAO;
import model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class Login extends JFrame {
    
    private UserDAO userDAO;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JCheckBox chkRemember;
    
    public Login() {
        userDAO = new UserDAO();
        initComponents();
    }
    
    // ✅ SIMPLE method to load images from src/icons folder
    private ImageIcon loadIcon(String filename, int width, int height) {
        try {
            // Load from src/icons/ folder
            String path = "src/icons/" + filename;
            File file = new File(path);
            
            if (file.exists()) {
                ImageIcon icon = new ImageIcon(path);
                Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            } else {
                System.out.println("Icon not found: " + path);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error loading icon: " + filename);
            return null;
        }
    }
    
    private void initComponents() {
        setTitle("RentEase - Administrator Login");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        setLayout(new BorderLayout());
        
        JPanel mainContainer = new JPanel(new GridLayout(1, 2));
        
        JPanel leftPanel = createLeftPanel();
        mainContainer.add(leftPanel);
        
        JPanel rightPanel = createRightPanel();
        mainContainer.add(rightPanel);
        
        add(mainContainer, BorderLayout.CENTER);
    }
    
    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                Color color1 = new Color(37, 99, 235);
                Color color2 = new Color(29, 78, 216);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        leftPanel.setLayout(null);
        
        // ✅ Logo Icon - Try custom image first, fallback to emoji
        JLabel lblIcon = new JLabel();
        ImageIcon logoIcon = loadIcon("logo.png", 80, 80);
        if (logoIcon != null) {
            lblIcon.setIcon(logoIcon);
            lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        } else {
            lblIcon.setText("🏢");
            lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 72));
        }
        lblIcon.setBounds(80, 120, 100, 100);
        leftPanel.add(lblIcon);
        
        JLabel lblBrand = new JLabel("RentEase");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 48));
        lblBrand.setForeground(Color.WHITE);
        lblBrand.setBounds(80, 230, 400, 60);
        leftPanel.add(lblBrand);
        
        JLabel lblSubtitle = new JLabel("Management System");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        lblSubtitle.setForeground(new Color(219, 234, 254));
        lblSubtitle.setBounds(80, 295, 400, 30);
        leftPanel.add(lblSubtitle);
        
        JLabel lblTagline = new JLabel("Streamline Your");
        lblTagline.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblTagline.setForeground(Color.WHITE);
        lblTagline.setBounds(80, 380, 500, 50);
        leftPanel.add(lblTagline);
        
        JLabel lblTagline2 = new JLabel("Property Management");
        lblTagline2.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblTagline2.setForeground(Color.WHITE);
        lblTagline2.setBounds(80, 435, 500, 50);
        leftPanel.add(lblTagline2);
        
        JLabel lblDescription = new JLabel("Manage tenants, rooms, and payments all in one");
        lblDescription.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblDescription.setForeground(new Color(191, 219, 254));
        lblDescription.setBounds(80, 500, 500, 25);
        leftPanel.add(lblDescription);
        
        JLabel lblDescription2 = new JLabel("powerful platform designed for efficiency.");
        lblDescription2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblDescription2.setForeground(new Color(191, 219, 254));
        lblDescription2.setBounds(80, 530, 500, 25);
        leftPanel.add(lblDescription2);
        
        // ✅ Features with icons
        int featureY = 600;
        String[][] features = {
            {"security.png", "🔒", "Secure & Reliable", "Your data is protected with enterprise-grade security"},
            {"analytics.png", "📊", "Real-time Analytics", "Track payments and occupancy with live dashboards"},
            {"collaboration.png", "👥", "Easy Collaboration", "Manage multiple properties and teams effortlessly"}
        };
        
        for (String[] feature : features) {
            JLabel icon = new JLabel();
            ImageIcon featureIcon = loadIcon(feature[0], 32, 32);
            if (featureIcon != null) {
                icon.setIcon(featureIcon);
            } else {
                icon.setText(feature[1]);
                icon.setFont(new Font("Segoe UI", Font.PLAIN, 28));
            }
            icon.setBounds(80, featureY, 40, 40);
            leftPanel.add(icon);
            
            JLabel title = new JLabel(feature[2]);
            title.setFont(new Font("Segoe UI", Font.BOLD, 16));
            title.setForeground(Color.WHITE);
            title.setBounds(130, featureY, 400, 22);
            leftPanel.add(title);
            
            JLabel desc = new JLabel(feature[3]);
            desc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            desc.setForeground(new Color(191, 219, 254));
            desc.setBounds(130, featureY + 25, 450, 18);
            leftPanel.add(desc);
            
            featureY += 75;
        }
        
        JLabel lblFooter = new JLabel("© 2025 RentEase. All rights reserved.");
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFooter.setForeground(new Color(191, 219, 254));
        lblFooter.setBounds(80, 880, 400, 20);
        leftPanel.add(lblFooter);
        
        return leftPanel;
    }
    
    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(new Color(249, 250, 251));
        rightPanel.setLayout(null);
        
        int centerX = 150;
        int startY = 180;
        
        JLabel lblWelcome = new JLabel("Welcome back");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 38));
        lblWelcome.setForeground(new Color(17, 24, 39));
        lblWelcome.setBounds(centerX, startY, 400, 50);
        rightPanel.add(lblWelcome);
        
        JLabel lblSubtitle = new JLabel("Please enter your credentials to continue");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblSubtitle.setForeground(new Color(107, 114, 128));
        lblSubtitle.setBounds(centerX, startY + 55, 450, 25);
        rightPanel.add(lblSubtitle);
        
        JPanel adminBadge = new JPanel();
        adminBadge.setBackground(new Color(239, 246, 255));
        adminBadge.setBounds(centerX, startY + 110, 450, 55);
        adminBadge.setLayout(null);
        adminBadge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(59, 130, 246), 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel lblAdminIcon = new JLabel();
        ImageIcon adminIcon = loadIcon("admin.png", 24, 24);
        if (adminIcon != null) {
            lblAdminIcon.setIcon(adminIcon);
        } else {
            lblAdminIcon.setText("👤");
            lblAdminIcon.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        }
        lblAdminIcon.setBounds(15, 12, 30, 30);
        adminBadge.add(lblAdminIcon);
        
        JLabel lblAdmin = new JLabel("Administrator");
        lblAdmin.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblAdmin.setForeground(new Color(59, 130, 246));
        lblAdmin.setBounds(50, 15, 200, 25);
        adminBadge.add(lblAdmin);
        
        rightPanel.add(adminBadge);
        
        JLabel lblUsername = new JLabel("Username");
        lblUsername.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUsername.setForeground(new Color(55, 65, 81));
        lblUsername.setBounds(centerX, startY + 195, 450, 20);
        rightPanel.add(lblUsername);
        
        JPanel userPanel = new JPanel();
        userPanel.setBackground(Color.WHITE);
        userPanel.setBounds(centerX, startY + 220, 450, 50);
        userPanel.setLayout(new BorderLayout());
        userPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        
        JLabel userIcon = new JLabel();
        ImageIcon userIconImg = loadIcon("user.png", 20, 20);
        if (userIconImg != null) {
            userIcon.setIcon(userIconImg);
        } else {
            userIcon.setText("👤");
            userIcon.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        }
        userPanel.add(userIcon, BorderLayout.WEST);
        
        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtUsername.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
        txtUsername.setBackground(Color.WHITE);
        userPanel.add(txtUsername, BorderLayout.CENTER);
        
        rightPanel.add(userPanel);
        
        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPassword.setForeground(new Color(55, 65, 81));
        lblPassword.setBounds(centerX, startY + 290, 450, 20);
        rightPanel.add(lblPassword);
        
        JPanel passPanel = new JPanel();
        passPanel.setBackground(Color.WHITE);
        passPanel.setBounds(centerX, startY + 315, 450, 50);
        passPanel.setLayout(new BorderLayout());
        passPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        
        JLabel passIcon = new JLabel();
        ImageIcon passIconImg = loadIcon("lock.png", 20, 20);
        if (passIconImg != null) {
            passIcon.setIcon(passIconImg);
        } else {
            passIcon.setText("🔒");
            passIcon.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        }
        passPanel.add(passIcon, BorderLayout.WEST);
        
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtPassword.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
        txtPassword.setBackground(Color.WHITE);
        passPanel.add(txtPassword, BorderLayout.CENTER);
        
        rightPanel.add(passPanel);
        
        chkRemember = new JCheckBox("Keep me signed in");
        chkRemember.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chkRemember.setForeground(new Color(55, 65, 81));
        chkRemember.setBackground(new Color(249, 250, 251));
        chkRemember.setBounds(centerX, startY + 385, 200, 25);
        chkRemember.setFocusPainted(false);
        rightPanel.add(chkRemember);
        
        btnLogin = new JButton("Sign In   ➜");
        btnLogin.setBounds(centerX, startY + 440, 450, 52);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnLogin.setBackground(new Color(59, 130, 246));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> handleLogin());
        
        btnLogin.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnLogin.setBackground(new Color(37, 99, 235));
            }
            public void mouseExited(MouseEvent e) {
                btnLogin.setBackground(new Color(59, 130, 246));
            }
        });
        
        rightPanel.add(btnLogin);
        
        JPanel demoPanel = new JPanel();
        demoPanel.setBackground(new Color(239, 246, 255));
        demoPanel.setBounds(centerX, startY + 520, 450, 90);
        demoPanel.setLayout(null);
        demoPanel.setBorder(BorderFactory.createLineBorder(new Color(191, 219, 254), 1));
        
        JLabel lblDemoIcon = new JLabel("ℹ️");
        lblDemoIcon.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblDemoIcon.setBounds(15, 10, 30, 25);
        demoPanel.add(lblDemoIcon);
        
        JLabel lblDemoTitle = new JLabel("Demo Credentials");
        lblDemoTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDemoTitle.setForeground(new Color(30, 64, 175));
        lblDemoTitle.setBounds(50, 10, 200, 25);
        demoPanel.add(lblDemoTitle);
        
        JLabel lblDemoAdmin = new JLabel("Admin: admin / admin123");
        lblDemoAdmin.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDemoAdmin.setForeground(new Color(37, 99, 235));
        lblDemoAdmin.setBounds(50, 40, 350, 20);
        demoPanel.add(lblDemoAdmin);
        
        rightPanel.add(demoPanel);
        
        JLabel lblHelp = new JLabel("Need help? Contact Support");
        lblHelp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblHelp.setForeground(new Color(107, 114, 128));
        lblHelp.setBounds(centerX + 120, startY + 640, 250, 20);
        rightPanel.add(lblHelp);
        
        txtPassword.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        });
        
        return rightPanel;
    }
    
    // ✅ FIXED: Removed the annoying popup notification!
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields!");
            return;
        }
        
        User user = userDAO.authenticateUser(username, password);
        
        if (user != null) {
            if (!"admin".equalsIgnoreCase(user.getRole()) && !"administrator".equalsIgnoreCase(user.getRole())) {
                showError("Access denied! Only administrators can login.");
                return;
            }
            
            // ✅ NO MORE POPUP! Just close login and open dashboard
            this.dispose();
            new Dashboard(user).setVisible(true);
            
        } else {
            showError("Invalid username or password!");
            txtPassword.setText("");
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, 
            message,
            "Login Error", 
            JOptionPane.ERROR_MESSAGE);
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new Login().setVisible(true);
        });
    }
}