package views.components;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import utils.ThemeManager;


public class StatCardPanel extends RoundedPanel {

    private final JLabel valueLabel;

    public StatCardPanel(String title, String initialValue, String subtitle) {
        super(16, ThemeManager.BORDER); 
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(ThemeManager.SURFACE);
        setBorder(new EmptyBorder(24, 24, 24, 24)); 

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ThemeManager.FONT_SMALL);
        titleLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(titleLabel);

        add(Box.createVerticalStrut(8));

        valueLabel = new JLabel(initialValue);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(valueLabel);

        add(Box.createVerticalStrut(4));

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(ThemeManager.FONT_SMALL);
        subLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(subLabel);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }
}
