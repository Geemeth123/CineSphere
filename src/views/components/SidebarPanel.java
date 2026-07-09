package views.components;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import utils.ThemeManager;

public class SidebarPanel extends JPanel {

    private final List<NavButton> navButtons;
    private final JButton logoutButton;
    private int activeNavIndex = 0;

    private final Color BG_COLOR = new Color(14, 14, 14);
    private final Color BORDER_COLOR = new Color(30, 30, 30);
    private final Color TEXT_MUTED = new Color(130, 130, 130);
    private final Color HOVER_COLOR = new Color(22, 22, 22);

    public SidebarPanel(String subtitleText, String userName, String role, String[] navLabels) {
        navButtons = new ArrayList<>();

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(250, 0));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(BG_COLOR);
        topPanel.setBorder(new EmptyBorder(40, 30, 40, 30));

        JLabel logo = new JLabel("CineSphere.");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel(subtitleText);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(2, 0, 0, 0));

        topPanel.add(logo);
        topPanel.add(subtitle);
        
        add(topPanel, BorderLayout.NORTH);

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(BG_COLOR);
        navPanel.setBorder(new EmptyBorder(0, 0, 0, 0)); 

        for (int i = 0; i < navLabels.length; i++) {
            NavButton btn = new NavButton(navLabels[i]);
            if (i == 0) btn.setActive(true);
            
            navButtons.add(btn);
            navPanel.add(btn);
        }
        
        add(navPanel, BorderLayout.CENTER);

        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));
        bottomContainer.setBackground(BG_COLOR);
        bottomContainer.setBorder(new EmptyBorder(20, 30, 40, 30));

        JLabel nameLabel = new JLabel(userName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel roleLabel = new JLabel(role);
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(TEXT_MUTED);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        roleLabel.setBorder(new EmptyBorder(2, 0, 15, 0)); 

        logoutButton = new JButton("Sign out");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutButton.setForeground(TEXT_MUTED);
        logoutButton.setContentAreaFilled(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutButton.setMargin(new Insets(0, 0, 0, 0)); 
        
        logoutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { logoutButton.setForeground(ThemeManager.ERROR); }
            @Override
            public void mouseExited(MouseEvent e) { logoutButton.setForeground(TEXT_MUTED); }
        });

        bottomContainer.add(nameLabel);
        bottomContainer.add(roleLabel);
        bottomContainer.add(logoutButton);
        
        add(bottomContainer, BorderLayout.SOUTH);
    }

    public void setActiveNav(int index) {
        this.activeNavIndex = index;
        for (int i = 0; i < navButtons.size(); i++) {
            navButtons.get(i).setActive(i == index);
        }
    }

    public void addNavListener(int index, ActionListener listener) {
        if (index >= 0 && index < navButtons.size()) {
            navButtons.get(index).addActionListener(listener);
        }
    }

    public void addLogoutListener(ActionListener listener) {
        logoutButton.addActionListener(listener);
    }

    private class NavButton extends JButton {
        private boolean isHovered = false;
        private boolean isActive = false;

        public NavButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(TEXT_MUTED);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new EmptyBorder(14, 30, 14, 30)); 
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            setPreferredSize(new Dimension(250, 48));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        public void setActive(boolean active) {
            this.isActive = active;
            setForeground(active ? Color.WHITE : TEXT_MUTED);
            setFont(active ? new Font("Segoe UI", Font.BOLD, 14) : new Font("Segoe UI", Font.PLAIN, 14));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isActive) {
                g2.setColor(new Color(15, 98, 254, 25)); 
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                g2.setColor(ThemeManager.PRIMARY);
                g2.fillRect(0, 0, 4, getHeight());
            } else if (isHovered) {
                g2.setColor(HOVER_COLOR);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}