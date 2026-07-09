import controllers.LoginController;
import javax.swing.*;
import models.DatabaseConnection;
import utils.ThemeManager;
import views.LoginView;

public class Main {

    public static void main(String[] args) {
        ThemeManager.initialize();

        SwingUtilities.invokeLater(() -> {
            // Test db
            if (!DatabaseConnection.testConnection()) {
                int choice = JOptionPane.showConfirmDialog(null,
                    "Could not connect to the database.\n" +
                    "Would you like to continue anyway?",
                    "Database Connection Failed",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                if (choice != JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }

            // Create and show login view
            LoginView loginView = new LoginView();
            new LoginController(loginView);
        });
    }
}
