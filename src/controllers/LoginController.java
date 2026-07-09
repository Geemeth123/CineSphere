package controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import models.User;
import views.AdminView;
import views.LoginView;
import views.TicketView;

public class LoginController {

    private final LoginView view;

    public LoginController(LoginView view) {
        this.view = view;
        this.view.addLoginListener(new LoginListener());
    }

    private class LoginListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String username = view.getUsername().trim();
            String password = view.getPassword();

            if (username.isEmpty() || password.isEmpty()) {
                view.showError("Please enter both username and password.");
                return;
            }

            try {
                User user = User.authenticate(username, password);
                if (user != null) {
                    view.dispose();
                    openDashboard(user);
                } else {
                    view.showError("Invalid username or password.");
                }
            } catch (Exception ex) {
                view.showError("Database connection failed. Please try again.");
                ex.printStackTrace();
            }
        }
    }

    private void openDashboard(User user) {
        SwingUtilities.invokeLater(() -> {
            switch (user.getRole()) {
                case "ADMIN" -> {
                    AdminView adminView = new AdminView(user);
                    new AdminController(adminView, user);
                }
                case "TICKET_STAFF" -> {
                    TicketView ticketView = new TicketView(user);
                    new TicketController(ticketView, user);
                }
                default -> {
                    JOptionPane.showMessageDialog(null,
                            "Unknown role: " + user.getRole(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
