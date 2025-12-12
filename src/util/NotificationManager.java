package util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Complete Notification System for RentEase
 * Shows notifications for all actions with auto-dismiss and no overlapping
 */
public class NotificationManager {
    
    private static final List<JDialog> activeNotifications = new ArrayList<>();
    private static final int NOTIFICATION_WIDTH = 350;
    private static final int NOTIFICATION_HEIGHT = 80;
    private static final int NOTIFICATION_SPACING = 10;
    private static final int AUTO_DISMISS_DELAY = 4000; // 4 seconds
    
    /**
     * Show SUCCESS notification
     */
    public static void showSuccess(JFrame parent, String message) {
        show(parent, message, new Color(34, 197, 94), "✓");
    }
    
    /**
     * Show ERROR notification
     */
    public static void showError(JFrame parent, String message) {
        show(parent, message, new Color(239, 68, 68), "✕");
    }
    
    /**
     * Show WARNING notification
     */
    public static void showWarning(JFrame parent, String message) {
        show(parent, message, new Color(251, 191, 36), "⚠");
    }
    
    /**
     * Show INFO notification
     */
    public static void showInfo(JFrame parent, String message) {
        show(parent, message, new Color(59, 130, 246), "ℹ");
    }
    
    /**
     * Show a notification with auto-dismiss
     */
    private static void show(JFrame parent, String message, Color color, String icon) {
        SwingUtilities.invokeLater(() -> {
            JDialog notification = createNotification(parent, message, color, icon);
            positionNotification(notification);
            
            // Add to active notifications
            activeNotifications.add(notification);
            
            // Show notification
            notification.setVisible(true);
            
            // Auto-dismiss after delay
            Timer dismissTimer = new Timer(AUTO_DISMISS_DELAY, e -> dismissNotification(notification));
            dismissTimer.setRepeats(false);
            dismissTimer.start();
        });
    }
    
    /**
     * Create notification dialog
     */
    private static JDialog createNotification(JFrame parent, String message, Color typeColor, String icon) {
        JDialog notification = new JDialog(parent);
        notification.setUndecorated(true);
        notification.setSize(NOTIFICATION_WIDTH, NOTIFICATION_HEIGHT);
        notification.setAlwaysOnTop(true);
        notification.setFocusableWindowState(false);
        
        // Main panel with rounded border effect
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw shadow
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 12, 12);
                
                // Draw background
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 12, 12);
                
                // Draw colored left border
                g2d.setColor(typeColor);
                g2d.fillRoundRect(0, 0, 6, getHeight() - 4, 12, 12);
            }
        };
        
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new BorderLayout(10, 0));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Icon panel
        JPanel iconPanel = new JPanel();
        iconPanel.setOpaque(false);
        iconPanel.setLayout(new BorderLayout());
        iconPanel.setPreferredSize(new Dimension(40, 40));
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        iconLabel.setForeground(typeColor);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconPanel.add(iconLabel, BorderLayout.CENTER);
        
        // Message panel
        JPanel messagePanel = new JPanel();
        messagePanel.setOpaque(false);
        messagePanel.setLayout(new BorderLayout());
        
        JLabel messageLabel = new JLabel("<html><body style='width: 250px'>" + message + "</body></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        messageLabel.setForeground(new Color(51, 65, 85));
        messagePanel.add(messageLabel, BorderLayout.CENTER);
        
        // Close button
        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        closeButton.setForeground(new Color(148, 163, 184));
        closeButton.setBackground(Color.WHITE);
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.setPreferredSize(new Dimension(30, 30));
        closeButton.addActionListener(e -> dismissNotification(notification));
        
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeButton.setForeground(new Color(51, 65, 85));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeButton.setForeground(new Color(148, 163, 184));
            }
        });
        
        // Add components
        mainPanel.add(iconPanel, BorderLayout.WEST);
        mainPanel.add(messagePanel, BorderLayout.CENTER);
        mainPanel.add(closeButton, BorderLayout.EAST);
        
        notification.add(mainPanel);
        
        // Fade-in animation
        notification.setOpacity(0.0f);
        Timer fadeIn = new Timer(20, new ActionListener() {
            private float opacity = 0.0f;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                opacity += 0.1f;
                if (opacity >= 1.0f) {
                    opacity = 1.0f;
                    ((Timer) e.getSource()).stop();
                }
                notification.setOpacity(opacity);
            }
        });
        fadeIn.start();
        
        return notification;
    }
    
    /**
     * Position notification on screen (stack from top-right)
     */
    private static void positionNotification(JDialog notification) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(notification.getGraphicsConfiguration());
        
        int x = screenSize.width - NOTIFICATION_WIDTH - screenInsets.right - 20;
        int y = screenInsets.top + 20;
        
        // Stack notifications vertically
        for (JDialog existing : activeNotifications) {
            if (existing.isVisible()) {
                y += NOTIFICATION_HEIGHT + NOTIFICATION_SPACING;
            }
        }
        
        notification.setLocation(x, y);
    }
    
    /**
     * Dismiss notification with fade-out animation
     */
    private static void dismissNotification(JDialog notification) {
        Timer fadeOut = new Timer(20, new ActionListener() {
            private float opacity = 1.0f;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                opacity -= 0.1f;
                if (opacity <= 0.0f) {
                    opacity = 0.0f;
                    ((Timer) e.getSource()).stop();
                    notification.dispose();
                    activeNotifications.remove(notification);
                    repositionNotifications();
                }
                notification.setOpacity(opacity);
            }
        });
        fadeOut.start();
    }
    
    /**
     * Reposition all active notifications after one is dismissed
     */
    private static void repositionNotifications() {
        if (activeNotifications.isEmpty()) return;
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(
            activeNotifications.get(0).getGraphicsConfiguration()
        );
        
        int x = screenSize.width - NOTIFICATION_WIDTH - screenInsets.right - 20;
        int y = screenInsets.top + 20;
        
        for (JDialog notification : activeNotifications) {
            if (notification.isVisible()) {
                notification.setLocation(x, y);
                y += NOTIFICATION_HEIGHT + NOTIFICATION_SPACING;
            }
        }
    }
    
    /**
     * Clear all active notifications
     */
    public static void clearAll() {
        List<JDialog> toRemove = new ArrayList<>(activeNotifications);
        for (JDialog notification : toRemove) {
            dismissNotification(notification);
        }
    }
}