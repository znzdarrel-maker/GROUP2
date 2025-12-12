package util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;

/**
 * Notification Panel System - Figma Style
 * Bell icon dropdown with notifications (for Dashboard)
 */
public class NotificationPanel {
    
    private static final List<Notification> notifications = new ArrayList<>();
    private static JDialog notificationDialog;
    private static JLabel bellLabel;
    private static JLabel badgeLabel;
    private static JFrame parentFrame;
    
    // Notification Types with Colors
    public static class NotificationType {
        public static final Color PAYMENT = new Color(34, 197, 94);     // Green
        public static final Color TENANT = new Color(59, 130, 246);     // Blue
        public static final Color ROOM = new Color(251, 191, 36);       // Yellow
        public static final Color SYSTEM = new Color(147, 51, 234);     // Purple
        public static final Color ERROR = new Color(239, 68, 68);       // Red
        
        public static String getIcon(Color type) {
            if (type.equals(PAYMENT)) return "💵";
            if (type.equals(TENANT)) return "👤";
            if (type.equals(ROOM)) return "🏠";
            if (type.equals(SYSTEM)) return "📱";
            if (type.equals(ERROR)) return "⚠";
            return "🔔";
        }
    }
    
    // Notification Data Class
    public static class Notification {
        String title;
        String message;
        Color type;
        Date timestamp;
        boolean isRead;
        
        public Notification(String title, String message, Color type) {
            this.title = title;
            this.message = message;
            this.type = type;
            this.timestamp = new Date();
            this.isRead = false;
        }
    }
    
    /**
     * Load icon from src/icons folder with fallback
     */
    private static ImageIcon loadIcon(String filename, int width, int height) {
        try {
            String path = "src/icons/" + filename;
            File file = new File(path);
            
            if (file.exists()) {
                ImageIcon icon = new ImageIcon(path);
                Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            } else {
                System.out.println("Icon not found: " + path + " (using emoji fallback)");
            }
        } catch (Exception e) {
            System.err.println("Error loading icon: " + filename);
        }
        return null;
    }
    
    /**
     * Initialize notification system with bell icon button
     */
    public static JPanel createNotificationButton(JFrame parent, int x, int y) {
        parentFrame = parent;
        
        JPanel bellPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (isOpaque()) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(getBackground());
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
            }
        };
        
        bellPanel.setLayout(null);
        bellPanel.setOpaque(false);
        bellPanel.setBounds(x, y, 60, 60);
        bellPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Try to load bell.png icon from src/icons folder
        ImageIcon bellIcon = loadIcon("bell.png", 32, 32);
        
        bellLabel = new JLabel();
        bellLabel.setBounds(10, 10, 40, 40);
        bellLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        if (bellIcon != null) {
            // Use loaded PNG icon
            bellLabel.setIcon(bellIcon);
        } else {
            // Fallback to emoji bell
            bellLabel.setText("🔔");
            bellLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        }
        
        bellPanel.add(bellLabel);
        
        // Red notification badge (count)
        badgeLabel = new JLabel("0") {
            @Override
            protected void paintComponent(Graphics g) {
                if (isVisible()) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Draw circular background
                    g2d.setColor(getBackground());
                    g2d.fillOval(0, 0, getWidth(), getHeight());
                    
                    // Draw white border
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawOval(1, 1, getWidth() - 2, getHeight() - 2);
                }
                super.paintComponent(g);
            }
        };
        
        badgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badgeLabel.setForeground(Color.WHITE);
        badgeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        badgeLabel.setVerticalAlignment(SwingConstants.CENTER);
        badgeLabel.setBounds(35, 5, 20, 20);
        badgeLabel.setOpaque(false);
        badgeLabel.setBackground(new Color(239, 68, 68));
        badgeLabel.setVisible(false);
        
        bellPanel.add(badgeLabel);
        
        // Hover effect
        bellPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                toggleNotificationPanel();
            }
            
            public void mouseEntered(MouseEvent e) {
                bellPanel.setOpaque(true);
                bellPanel.setBackground(new Color(241, 245, 249));
                bellPanel.repaint();
            }
            
            public void mouseExited(MouseEvent e) {
                bellPanel.setOpaque(false);
                bellPanel.repaint();
            }
        });
        
        updateBadge();
        return bellPanel;
    }
    
    /**
     * Add notification methods
     */
    public static void addNotification(String title, String message, Color type) {
        notifications.add(0, new Notification(title, message, type));
        updateBadge();
        
        if (notificationDialog != null && notificationDialog.isVisible()) {
            toggleNotificationPanel();
            toggleNotificationPanel();
        }
    }
    
    public static void addPaymentNotification(String message) {
        addNotification("New Payment Received", message, NotificationType.PAYMENT);
    }
    
    public static void addTenantNotification(String message) {
        addNotification("New Tenant Added", message, NotificationType.TENANT);
    }
    
    public static void addRoomNotification(String message) {
        addNotification("Room Maintenance", message, NotificationType.ROOM);
    }
    
    public static void addSystemNotification(String message) {
        addNotification("System Update", message, NotificationType.SYSTEM);
    }
    
    public static void addErrorNotification(String message) {
        addNotification("Payment Overdue", message, NotificationType.ERROR);
    }
    
    /**
     * Update notification badge count
     */
    private static void updateBadge() {
        if (badgeLabel == null) return;
        
        int unreadCount = 0;
        for (Notification n : notifications) {
            if (!n.isRead) unreadCount++;
        }
        
        if (unreadCount > 0) {
            badgeLabel.setText(String.valueOf(Math.min(unreadCount, 99))); // Max 99
            badgeLabel.setVisible(true);
        } else {
            badgeLabel.setVisible(false);
        }
    }
    
    /**
     * Toggle notification panel visibility
     */
    private static void toggleNotificationPanel() {
        if (notificationDialog != null && notificationDialog.isVisible()) {
            notificationDialog.dispose();
            notificationDialog = null;
            return;
        }
        
        showNotificationPanel();
    }
    
    /**
     * Show notification panel dropdown - FIGMA DESIGN
     */
    private static void showNotificationPanel() {
        notificationDialog = new JDialog(parentFrame);
        notificationDialog.setUndecorated(true);
        notificationDialog.setSize(400, 500);  // ✅ REDUCED HEIGHT
        notificationDialog.setAlwaysOnTop(true);
        
        // Position below bell icon
        Point bellLocation = bellLabel.getLocationOnScreen();
        notificationDialog.setLocation(bellLocation.x - 330, bellLocation.y + 55);
        
        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 2));
        
        // Header - Blue background (Figma style)
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(59, 130, 246));
        headerPanel.setLayout(null);
        headerPanel.setPreferredSize(new Dimension(400, 70));
        
        JLabel lblTitle = new JLabel("Notifications");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBounds(20, 15, 200, 25);
        headerPanel.add(lblTitle);
        
        int unreadCount = 0;
        for (Notification n : notifications) {
            if (!n.isRead) unreadCount++;
        }
        
        JLabel lblCount = new JLabel(unreadCount + " unread notifications");
        lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblCount.setForeground(new Color(219, 234, 254));
        lblCount.setBounds(20, 40, 200, 20);
        headerPanel.add(lblCount);
        
        JButton btnMarkAllRead = new JButton("✓ Mark all read");
        btnMarkAllRead.setBounds(235, 20, 145, 32);
        btnMarkAllRead.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnMarkAllRead.setForeground(new Color(59, 130, 246));
        btnMarkAllRead.setBackground(Color.WHITE);
        btnMarkAllRead.setFocusPainted(false);
        btnMarkAllRead.setBorderPainted(false);
        btnMarkAllRead.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMarkAllRead.addActionListener(e -> {
            markAllAsRead();
            notificationDialog.dispose();
            notificationDialog = null;
            showNotificationPanel();
        });
        headerPanel.add(btnMarkAllRead);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Notification list panel
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        
        if (notifications.isEmpty()) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setLayout(new BorderLayout());
            emptyPanel.setBackground(Color.WHITE);
            emptyPanel.setPreferredSize(new Dimension(396, 300));  // ✅ CHANGED from 380 to 396
            
            JLabel noNotif = new JLabel("No notifications yet");
            noNotif.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            noNotif.setForeground(new Color(148, 163, 184));
            noNotif.setHorizontalAlignment(SwingConstants.CENTER);
            
            emptyPanel.add(noNotif, BorderLayout.CENTER);
            listPanel.add(emptyPanel);
        } else {
            for (int i = 0; i < notifications.size(); i++) {
                Notification notif = notifications.get(i);
                listPanel.add(createNotificationItem(notif, i));
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Footer (Figma style)
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(248, 250, 252));
        footerPanel.setPreferredSize(new Dimension(400, 50));  // ✅ REDUCED HEIGHT
        footerPanel.setLayout(new BorderLayout());
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));
        
        JButton btnViewAll = new JButton("View All Notifications");
        btnViewAll.setFont(new Font("Segoe UI", Font.BOLD, 13));  // ✅ SLIGHTLY SMALLER FONT
        btnViewAll.setForeground(new Color(59, 130, 246));
        btnViewAll.setBackground(new Color(248, 250, 252));
        btnViewAll.setFocusPainted(false);
        btnViewAll.setBorderPainted(false);
        btnViewAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnViewAll.addActionListener(e -> {
            JOptionPane.showMessageDialog(parentFrame, "View All Notifications page coming soon!");
        });
        footerPanel.add(btnViewAll, BorderLayout.CENTER);
        
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        notificationDialog.add(mainPanel);
        notificationDialog.setVisible(true);
        
        // Close when clicking outside
        notificationDialog.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                if (notificationDialog != null) {
                    notificationDialog.dispose();
                    notificationDialog = null;
                }
            }
        });
    }
    
    /**
     * Create individual notification item (Figma design)
     */
    private static JPanel createNotificationItem(Notification notif, int index) {
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(null);
        itemPanel.setPreferredSize(new Dimension(396, 95));  // ✅ INCREASED WIDTH from 380 to 396
        itemPanel.setMaximumSize(new Dimension(396, 95));    // ✅ INCREASED WIDTH from 400 to 396
        itemPanel.setMinimumSize(new Dimension(396, 95));    // ✅ ADDED MIN WIDTH
        itemPanel.setBackground(notif.isRead ? Color.WHITE : new Color(239, 246, 255));
        itemPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
        itemPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Color-coded circular icon background
        JPanel iconContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
        };
        iconContainer.setBounds(15, 15, 45, 45);
        iconContainer.setLayout(new BorderLayout());
        iconContainer.setOpaque(true);
        
        Color iconBg = new Color(notif.type.getRed(), notif.type.getGreen(), notif.type.getBlue(), 30);
        iconContainer.setBackground(iconBg);
        
        JLabel iconLabel = new JLabel(NotificationType.getIcon(notif.type));
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconContainer.add(iconLabel, BorderLayout.CENTER);
        
        itemPanel.add(iconContainer);
        
        // Unread blue dot indicator
        if (!notif.isRead) {
            JLabel unreadDot = new JLabel("●");
            unreadDot.setFont(new Font("Segoe UI", Font.BOLD, 16));
            unreadDot.setForeground(new Color(59, 130, 246));
            unreadDot.setBounds(370, 12, 20, 20);  // ✅ ADJUSTED X from 355 to 370
            itemPanel.add(unreadDot);
        }
        
        // Title
        JLabel titleLabel = new JLabel(notif.title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(15, 23, 42));
        titleLabel.setBounds(70, 15, 290, 20);  // ✅ INCREASED WIDTH from 270 to 290
        itemPanel.add(titleLabel);
        
        // Message
        JLabel messageLabel = new JLabel("<html>" + notif.message + "</html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setForeground(new Color(100, 116, 139));
        messageLabel.setBounds(70, 38, 290, 20);  // ✅ INCREASED WIDTH from 270 to 290
        itemPanel.add(messageLabel);
        
        // Time ago
        JLabel timeLabel = new JLabel(getTimeAgo(notif.timestamp));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setForeground(new Color(148, 163, 184));
        timeLabel.setBounds(70, 62, 150, 15);
        itemPanel.add(timeLabel);
        
        // Mark read button
        JButton btnMarkRead = new JButton("✓ Mark read");
        btnMarkRead.setBounds(200, 60, 85, 20);  // ✅ ADJUSTED X from 190 to 200
        btnMarkRead.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnMarkRead.setForeground(new Color(59, 130, 246));
        btnMarkRead.setBackground(Color.WHITE);
        btnMarkRead.setFocusPainted(false);
        btnMarkRead.setBorderPainted(false);
        btnMarkRead.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMarkRead.setVisible(!notif.isRead);
        btnMarkRead.addActionListener(e -> {
            notif.isRead = true;
            updateBadge();
            notificationDialog.dispose();
            notificationDialog = null;
            showNotificationPanel();
        });
        itemPanel.add(btnMarkRead);
        
        // Delete button
        JButton btnDelete = new JButton("✕ Delete");
        btnDelete.setBounds(300, 60, 75, 20);  // ✅ ADJUSTED X from 285 to 300
        btnDelete.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnDelete.setForeground(new Color(239, 68, 68));
        btnDelete.setBackground(Color.WHITE);
        btnDelete.setFocusPainted(false);
        btnDelete.setBorderPainted(false);
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.addActionListener(e -> {
            notifications.remove(notif);
            updateBadge();
            notificationDialog.dispose();
            notificationDialog = null;
            showNotificationPanel();
        });
        itemPanel.add(btnDelete);
        
        return itemPanel;
    }
    
    /**
     * Mark all as read
     */
    private static void markAllAsRead() {
        for (Notification n : notifications) {
            n.isRead = true;
        }
        updateBadge();
    }
    
    /**
     * Get relative time string
     */
    private static String getTimeAgo(Date date) {
        long diff = new Date().getTime() - date.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (seconds < 60) return "Just now";
        if (minutes < 60) return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        if (hours < 24) return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        if (days < 7) return days + " day" + (days > 1 ? "s" : "") + " ago";
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        return sdf.format(date);
    }
    
    /**
     * Clear all notifications
     */
    public static void clearAll() {
        notifications.clear();
        updateBadge();
    }
}