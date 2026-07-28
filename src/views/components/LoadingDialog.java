package views.components;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import utils.ThemeManager;

public class LoadingDialog extends JDialog {

    private final JLabel messageLabel;
    private final ModernSpinner spinner;

    public LoadingDialog(Frame owner, String message) {
        super(owner, true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0)); 
        RoundedPanel panel = new RoundedPanel(20, ThemeManager.BORDER);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(35, 50, 35, 50));

        spinner = new ModernSpinner();
        spinner.setAlignmentX(Component.CENTER_ALIGNMENT);

        messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        messageLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(spinner);
        panel.add(Box.createVerticalStrut(20));
        panel.add(messageLabel);

        add(panel);
        pack();
        setLocationRelativeTo(owner);

        //stop timer if error
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                spinner.stop();
            }
        });
    }

    //reusable method to run a background task w loadin
    public static void runWithLoading(Frame owner, String message, Runnable backgroundTask, Runnable onSuccess) {
        LoadingDialog dialog = new LoadingDialog(owner, message);
        dialog.spinner.start();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                backgroundTask.run();
                return null;
            }

            @Override
            protected void done() {
                dialog.spinner.stop();
                dialog.dispose();
                if (onSuccess != null) {
                    onSuccess.run();
                }
            }
        };
        worker.execute();
        dialog.setVisible(true); // Blocks UI 
    }

    private static class ModernSpinner extends JPanel {

        private int angle = 0;
        private final Timer timer;

        public ModernSpinner() {
            setOpaque(false);
            setPreferredSize(new Dimension(50, 50));
            setMaximumSize(new Dimension(50, 50));

            timer = new Timer(16, e -> {
                angle = (angle + 8) % 360;
                repaint();
            });
        }

        public void start() {
            timer.start();
        }

        public void stop() {
            timer.stop();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int strokeWidth = 5;
            int size = Math.min(getWidth(), getHeight()) - strokeWidth;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            g2.setColor(ThemeManager.SURFACE);
            g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(x, y, size, size);

            g2.setColor(ThemeManager.PRIMARY);
            g2.drawArc(x, y, size, size, -angle, 120);

            g2.dispose();
        }
    }
}
