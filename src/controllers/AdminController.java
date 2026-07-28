package controllers;

import java.util.List;
import javax.swing.*;
import models.Booking;
import models.Movie;
import models.User;
import views.AdminView;
import views.LoginView;

public class AdminController {

    private final AdminView view;
    private final User currentUser;

    public AdminController(AdminView view, User currentUser) {
        this.view = view;
        this.currentUser = currentUser;

        initializeNavListeners();
        initializeActionListeners();
        initializeStaffListeners();
        new AdminMovieController(view); // Delegate movie logic to dedicated controller

        loadDashboardStats();
        loadActiveShows();
        loadStaffData();
    }

    private void initializeNavListeners() {
        view.addNavListener(0, e -> {
            view.setActiveNav(0);
            view.showCard("dashboard");
            loadDashboardStats(); // Refresh stats when returning to dashboard
        });

        view.addNavListener(1, e -> {
            view.setActiveNav(1);
            view.showCard("movies");
        });

        view.addNavListener(2, e -> {
            view.setActiveNav(2);
            loadStaffData(); // Refresh table when opening tab
            view.showCard("staff");
        });

        view.addNavListener(3, e -> {
            view.setActiveNav(3);
            view.showCard("reports");
        });

        view.addLogoutListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(view.getFrame(),
                    "Are you sure you want to sign out?",
                    "Confirm Sign Out", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                view.dispose();
                SwingUtilities.invokeLater(() -> {
                    LoginView loginView = new LoginView();
                    new LoginController(loginView);
                });
            }
        });
    }

    private void initializeActionListeners() {
        view.addAddMovieListener(e -> {
            int choice = view.showAddMovieOptions();
            if (choice == 0) {
                // TMDB fetch — navigate to movies tab and focus the search field
                view.setActiveNav(1);
                view.showCard("movies");
                SwingUtilities.invokeLater(() ->
                    view.getMovieManagementPanel().getSearchField().requestFocusInWindow()
                );
            } else if (choice == 1) {
                // Manual entry — navigate to movies tab
                view.setActiveNav(1);
                view.showCard("movies");
            }
        });
    }

    // ==========================================
    // Staff Management Logic
    // ==========================================
    private void initializeStaffListeners() {

        // Add Staff
        view.getStaffManagementPanel().getAddButton().addActionListener(e -> {
            User newUser = view.getStaffManagementPanel().showStaffFormDialog(view.getFrame(), null);
            if (newUser != null) {
                if (User.insert(newUser)) {
                    JOptionPane.showMessageDialog(view.getFrame(), "Staff member added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadStaffData();
                } else {
                    JOptionPane.showMessageDialog(view.getFrame(), "Failed to add staff member.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Edit Staff
        view.getStaffManagementPanel().getEditButton().addActionListener(e -> {
            int selectedRow = view.getStaffManagementPanel().getStaffTable().getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(view.getFrame(), "Please select a staff member to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int userId = (int) view.getStaffManagementPanel().getTableModel().getValueAt(selectedRow, 0);
            User userToEdit = User.getById(userId);

            if (userToEdit != null) {
                User updatedUser = view.getStaffManagementPanel().showStaffFormDialog(view.getFrame(), userToEdit);
                if (updatedUser != null) {
                    if (User.update(updatedUser)) {
                        JOptionPane.showMessageDialog(view.getFrame(), "Staff member updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadStaffData();
                    } else {
                        JOptionPane.showMessageDialog(view.getFrame(), "Failed to update staff member.", "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // Deactivate Staff
        view.getStaffManagementPanel().getDeleteButton().addActionListener(e -> {
            int selectedRow = view.getStaffManagementPanel().getStaffTable().getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(view.getFrame(), "Please select a staff member to deactivate.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int userId = (int) view.getStaffManagementPanel().getTableModel().getValueAt(selectedRow, 0);

            // Prevent self-deactivation
            if (userId == currentUser.getId()) {
                JOptionPane.showMessageDialog(view.getFrame(), "You cannot deactivate your own account.", "Action Prohibited", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(view.getFrame(),
                    "Are you sure you want to deactivate this staff account?",
                    "Confirm Deactivation", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (User.delete(userId)) {
                    JOptionPane.showMessageDialog(view.getFrame(), "Account deactivated.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadStaffData();
                } else {
                    JOptionPane.showMessageDialog(view.getFrame(), "Failed to deactivate account.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void loadStaffData() {
        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<User> doInBackground() {
                return User.getAllUsers();
            }

            @Override
            protected void done() {
                try {
                    List<User> users = get();
                    view.getStaffManagementPanel().getTableModel().setRowCount(0); // Clear table
                    for (User u : users) {
                        view.getStaffManagementPanel().getTableModel().addRow(new Object[]{
                            u.getId(),
                            u.getFullName(),
                            u.getUsername(),
                            u.getRole(),
                            u.getStatus()
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    // ==========================================
    // Dashboard Stats Logic
    // ==========================================
    private void loadDashboardStats() {
        SwingWorker<DashboardStats, Void> worker = new SwingWorker<>() {
            @Override
            protected DashboardStats doInBackground() {
                try {
                    int movieCount = Movie.getActiveCount();
                    int staffCount = User.getStaffCount(); // Now uses proper DB logic
                    int bookingCount = Booking.getTodaysBookingCount();
                    double revenue = Booking.getTotalRevenue();

                    return new DashboardStats(movieCount, staffCount, bookingCount, revenue);
                } catch (Exception e) {
                    e.printStackTrace();
                    return new DashboardStats(0, 0, 0, 0.0);
                }
            }

            @Override
            protected void done() {
                try {
                    DashboardStats stats = get();
                    view.updateStats(stats.movieCount, stats.staffCount, stats.bookingCount, stats.revenue);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void loadActiveShows() {
        view.clearShowsTable();
        Object[][] dummyData = {
            {"SH-101", "Dune: Part Two", "Hall 1 (IMAX)", "14:30 PM", "110/120", "Running"},
            {"SH-102", "Kung Fu Panda 4", "Hall 2", "15:00 PM", "78/80", "Almost Full"},
            {"SH-103", "Godzilla x Kong", "Hall 3", "16:45 PM", "15/100", "Running"},
            {"SH-104", "Ghostbusters", "Hall 1 (IMAX)", "18:00 PM", "120/120", "Sold Out"},
            {"SH-105", "Civil War", "Hall 2", "19:30 PM", "0/80", "Available"}
        };
        for (Object[] row : dummyData) {
            view.addShowToTable(row);
        }
    }

    private record DashboardStats(int movieCount, int staffCount, int bookingCount, double revenue) {

    }
}
