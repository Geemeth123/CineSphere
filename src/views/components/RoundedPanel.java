package views.components;

import java.awt.*;
import javax.swing.*;
import utils.ThemeManager;

public class RoundedPanel extends JPanel {

    private final int cornerRadius;
    private Color borderCol;

    public RoundedPanel(int radius) {
        this.cornerRadius = radius;
        this.borderCol = ThemeManager.BORDER;
        setOpaque(false);
    }

    public RoundedPanel(int radius, Color borderColor) {
        this.cornerRadius = radius;
        this.borderCol = borderColor;
        setOpaque(false);
    }

    public void setBorderColor(Color color) {
        this.borderCol = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //bg
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        if (borderCol != null) {
            g2.setColor(borderCol);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
