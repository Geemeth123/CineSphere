package views;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import utils.ThemeManager;

public class LoginView {

    private final JFrame frame;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JLabel errorLabel;
    private final JButton loginButton;

    public LoginView() {
        frame = new JFrame("CineSphere - Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 650);
        frame.setLocationRelativeTo(null);
        frame.setBackground(ThemeManager.BACKGROUND);

        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(ThemeManager.BACKGROUND);

        // login card
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ThemeManager.BACKGROUND);
        card.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel titleLabel = new JLabel("CineSphere");
        titleLabel.setFont(ThemeManager.FONT_TITLE);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Cinema Theater Management");
        subtitleLabel.setFont(ThemeManager.FONT_SMALL);
        subtitleLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitleLabel);

        card.add(Box.createRigidArea(new Dimension(0, 40)));

        // sub panel (forms)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(ThemeManager.BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 8, 0);
        gbc.weightx = 1.0;

        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(ThemeManager.FONT_SMALL);
        usernameLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        formPanel.add(usernameLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0);

        usernameField = new JTextField();
        usernameField.setFont(ThemeManager.FONT_BODY);
        usernameField.putClientProperty("JTextField.placeholderText", "Enter your username");
        usernameField.setPreferredSize(new Dimension(320, 40));
        formPanel.add(usernameField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 8, 0);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(ThemeManager.FONT_SMALL);
        passwordLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        formPanel.add(passwordLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 12, 0);

        passwordField = new JPasswordField();
        passwordField.setFont(ThemeManager.FONT_BODY);
        passwordField.putClientProperty("JTextField.placeholderText", "Enter your password");
        passwordField.setPreferredSize(new Dimension(320, 40));
        formPanel.add(passwordField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 24, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        errorLabel = new JLabel(" ");
        errorLabel.setFont(ThemeManager.FONT_SMALL);
        errorLabel.setForeground(ThemeManager.ERROR);
        errorLabel.setVisible(false);
        formPanel.add(errorLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        //button
        loginButton = ThemeManager.createPrimaryButton("Sign In");
        loginButton.setPreferredSize(new Dimension(320, 44));
        formPanel.add(loginButton, gbc);

        card.add(formPanel);

        JLabel footerLabel = new JLabel("\u00A9 2026 CineSphere");
        footerLabel.setFont(ThemeManager.FONT_SMALL);
        footerLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(footerLabel);

        outerPanel.add(card);
        frame.setContentPane(outerPanel);
        frame.setVisible(true);
    }

    public String getUsername() {
        return usernameField.getText().trim();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        errorLabel.setText(" ");
        errorLabel.setVisible(false);
    }

//listener
    public void addLoginListener(ActionListener listener) {
        loginButton.addActionListener(listener);
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    listener.actionPerformed(
                            new java.awt.event.ActionEvent(passwordField, java.awt.event.ActionEvent.ACTION_PERFORMED, "enter")
                    );
                }
            }
        });
    }

    public JFrame getFrame() {
        return frame;
    }

    public void dispose() {
        frame.dispose();
    }
}
