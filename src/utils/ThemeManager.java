package utils;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;

public class ThemeManager {

    public static final Color PRIMARY = new Color(15, 98, 254);      // #0F62FE Blue
    public static final Color PRIMARY_HOVER = new Color(3, 83, 233); // #0353E9
    public static final Color BACKGROUND = Color.WHITE;               // #FFFFFF Pure White
    public static final Color SURFACE = new Color(248, 249, 250);    // #F8F9FA Light Gray
    public static final Color TEXT_PRIMARY = new Color(17, 17, 17);  // #111111 Black/Dark Gray
    public static final Color TEXT_SECONDARY = new Color(111, 111, 111); // #6F6F6F Muted
    public static final Color BORDER = new Color(224, 224, 224);     // #E0E0E0
    public static final Color ERROR = new Color(218, 30, 40);        // #DA1E28
    public static final Color DELETE = new Color(180, 25, 33);  // #ffb2b6
    public static final Color SUCCESS = new Color(25, 128, 56);      // #198038
    public static final Color SIDEBAR_BG = new Color(17, 17, 17);    // #111111 Dark Gray (Highlighted Box)
    public static final Color SIDEBAR_TEXT = new Color(255, 255, 255); // White
    public static final Color SIDEBAR_HOVER = new Color(50, 50, 50); // #323232

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBHEADING = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);

    public static void initialize() {
        try {
            FlatLightLaf.setup();

            // Global UI 
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Component.innerFocusWidth", 0);
            UIManager.put("Component.borderColor", BORDER);
            UIManager.put("Component.focusColor", PRIMARY);

            // Button 
            UIManager.put("Button.background", BACKGROUND);
            UIManager.put("Button.foreground", TEXT_PRIMARY);
            UIManager.put("Button.font", FONT_BUTTON);

            // Text field 
            UIManager.put("TextField.background", BACKGROUND);
            UIManager.put("TextField.foreground", TEXT_PRIMARY);
            UIManager.put("TextField.placeholderForeground", TEXT_SECONDARY);
            UIManager.put("PasswordField.background", BACKGROUND);
            UIManager.put("PasswordField.foreground", TEXT_PRIMARY);
            UIManager.put("PasswordField.placeholderForeground", TEXT_SECONDARY);

            // Panel 
            UIManager.put("Panel.background", BACKGROUND);

            // ScrollBar
            UIManager.put("ScrollBar.width", 8);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.trackArc", 999);

        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf: " + e.getMessage());
        }
    }

    //Blue button
    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFont(FONT_BUTTON);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty("JButton.buttonType", "roundRect");
        return button;
    }

    //Gray button
    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(BACKGROUND);
        button.setForeground(TEXT_PRIMARY);
        button.setFont(FONT_BUTTON);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty("JButton.buttonType", "roundRect");
        return button;
    }

    //Red button
    public static JButton createDeletionButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(DELETE);
        button.setForeground(Color.WHITE);
        button.setFont(FONT_BUTTON);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty("JButton.buttonType", "roundRect");
        return button;
    }
}
