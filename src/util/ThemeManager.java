package util;

import java.awt.Color;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.JFrame;

/**
 * Enhanced ThemeManager - Global Theme System for RentEase
 * Manages dark/light mode across all panels with live refresh
 */
public class ThemeManager {
    private static final String CONFIG_FILE = "theme.properties";
    private static boolean isDarkMode = false;
    private static final List<ThemeChangeListener> listeners = new ArrayList<>();
    
    // Interface for components that need to respond to theme changes
    public interface ThemeChangeListener {
        void onThemeChanged(boolean isDark);
    }
    
    // Dark Mode Colors
    public static class Dark {
        public static final Color BACKGROUND = new Color(15, 23, 42);
        public static final Color SIDEBAR = new Color(30, 41, 59);
        public static final Color SIDEBAR_HOVER = new Color(51, 65, 85);
        public static final Color MAIN_PANEL = new Color(15, 23, 42);
        public static final Color CARD_BG = new Color(30, 41, 59);
        public static final Color TEXT_PRIMARY = new Color(241, 245, 249);
        public static final Color TEXT_SECONDARY = new Color(148, 163, 184);
        public static final Color INPUT_BG = new Color(30, 41, 59);
        public static final Color INPUT_BORDER = new Color(71, 85, 105);
        public static final Color TABLE_BG = new Color(30, 41, 59);
        public static final Color TABLE_GRID = new Color(51, 65, 85);
        public static final Color TABLE_SELECTION = new Color(51, 65, 85);
        public static final Color SEPARATOR = new Color(71, 85, 105);
    }
    
    // Light Mode Colors
    public static class Light {
        public static final Color BACKGROUND = new Color(241, 245, 249);
        public static final Color SIDEBAR = new Color(30, 41, 59); // Keep sidebar dark in both modes
        public static final Color SIDEBAR_HOVER = new Color(51, 65, 85);
        public static final Color MAIN_PANEL = new Color(241, 245, 249);
        public static final Color CARD_BG = Color.WHITE;
        public static final Color TEXT_PRIMARY = new Color(15, 23, 42);
        public static final Color TEXT_SECONDARY = new Color(100, 116, 139);
        public static final Color INPUT_BG = Color.WHITE;
        public static final Color INPUT_BORDER = new Color(226, 232, 240);
        public static final Color TABLE_BG = Color.WHITE;
        public static final Color TABLE_GRID = new Color(226, 232, 240);
        public static final Color TABLE_SELECTION = new Color(239, 246, 255);
        public static final Color SEPARATOR = new Color(226, 232, 240);
    }
    
    // Load theme from file on startup
    static {
        loadTheme();
    }
    
    public static void loadTheme() {
        try {
            File file = new File(CONFIG_FILE);
            if (file.exists()) {
                Properties props = new Properties();
                props.load(new FileInputStream(file));
                isDarkMode = "dark".equals(props.getProperty("theme", "light"));
            }
        } catch (IOException e) {
            System.err.println("Could not load theme: " + e.getMessage());
        }
    }
    
    public static void saveTheme(boolean dark) {
        try {
            Properties props = new Properties();
            props.setProperty("theme", dark ? "dark" : "light");
            props.store(new FileOutputStream(CONFIG_FILE), "RentEase Theme Settings");
            isDarkMode = dark;
        } catch (IOException e) {
            System.err.println("Could not save theme: " + e.getMessage());
        }
    }
    
    public static void setTheme(boolean dark) {
        if (isDarkMode != dark) {
            isDarkMode = dark;
            saveTheme(dark);
            notifyListeners();
        }
    }
    
    public static boolean isDarkMode() {
        return isDarkMode;
    }
    
    public static void addThemeChangeListener(ThemeChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public static void removeThemeChangeListener(ThemeChangeListener listener) {
        listeners.remove(listener);
    }
    
    private static void notifyListeners() {
        for (ThemeChangeListener listener : new ArrayList<>(listeners)) {
            try {
                listener.onThemeChanged(isDarkMode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    // Helper methods to get colors based on current theme
    public static Color getBackground() {
        return isDarkMode ? Dark.BACKGROUND : Light.BACKGROUND;
    }
    
    public static Color getSidebarColor() {
        return isDarkMode ? Dark.SIDEBAR : Light.SIDEBAR;
    }
    
    public static Color getSidebarHover() {
        return isDarkMode ? Dark.SIDEBAR_HOVER : Light.SIDEBAR_HOVER;
    }
    
    public static Color getMainPanelColor() {
        return isDarkMode ? Dark.MAIN_PANEL : Light.MAIN_PANEL;
    }
    
    public static Color getCardBackground() {
        return isDarkMode ? Dark.CARD_BG : Light.CARD_BG;
    }
    
    public static Color getTextPrimary() {
        return isDarkMode ? Dark.TEXT_PRIMARY : Light.TEXT_PRIMARY;
    }
    
    public static Color getTextSecondary() {
        return isDarkMode ? Dark.TEXT_SECONDARY : Light.TEXT_SECONDARY;
    }
    
    public static Color getInputBackground() {
        return isDarkMode ? Dark.INPUT_BG : Light.INPUT_BG;
    }
    
    public static Color getInputBorder() {
        return isDarkMode ? Dark.INPUT_BORDER : Light.INPUT_BORDER;
    }
    
    public static Color getTableBackground() {
        return isDarkMode ? Dark.TABLE_BG : Light.TABLE_BG;
    }
    
    public static Color getTableGrid() {
        return isDarkMode ? Dark.TABLE_GRID : Light.TABLE_GRID;
    }
    
    public static Color getTableSelection() {
        return isDarkMode ? Dark.TABLE_SELECTION : Light.TABLE_SELECTION;
    }
    
    public static Color getSeparatorColor() {
        return isDarkMode ? Dark.SEPARATOR : Light.SEPARATOR;
    }
}