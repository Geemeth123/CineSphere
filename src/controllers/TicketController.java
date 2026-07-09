package controllers;

import models.Booking;
import models.Movie;
import models.User;
import views.LoginView;
import views.TicketView;
import views.components.LoadingDialog;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TicketController {

    private final TicketView view;
    private final User currentUser;

    public TicketController(TicketView view, User currentUser) {
        this.view = view;
        this.currentUser = currentUser;

        initializeNavListeners();
        initializeTableListeners();
        initializeBookingListeners();

        loadDashboardStats();
        loadTodayShows();
    }

    private void initializeNavListeners() {
        // 0. Dashboard
        view.addNavListener(0, e -> {
            view.setActiveNav(0);
            view.showCard("dashboard");
        });

        // 1. Now Showing
        view.addNavListener(1, e -> {
            view.setActiveNav(1);
            view.showCard("now_showing");
        });

        // 2. Book Tickets
        view.addNavListener(2, e -> {
            view.setActiveNav(2);
            view.showCard("show_selection");
        });

        // 3. Booking History
        view.addNavListener(3, e -> {
            view.setActiveNav(3);
            view.showCard("booking_history");
        });

        // 4. Cancellation Desk
        view.addNavListener(4, e -> {
            view.setActiveNav(4);
            view.getTicketCancellationPanel().reset();
            view.showCard("cancellation_desk");
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

        view.addBookTicketListener(e -> {
            view.setActiveNav(2);
            view.showCard("show_selection");
        });

        view.addSearchListener(e -> {
            JOptionPane.showMessageDialog(view.getFrame(),
                    "Search functionality will query the internal Show database.",
                    "Search Requested", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void initializeTableListeners() {
        JTable table = view.getShowsTable();
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());

                if (row >= 0) {
                    String movieTitle = (String) table.getValueAt(row, 2);

                    if (col == 0 && e.getClickCount() == 1) {
                        showMovieInfo(movieTitle);
                    }
                }
            }
        });
    }

    private void showMovieInfo(String title) {
        String info = "<html><body style='width: 250px; font-family: Segoe UI;'>"
                + "<h2 style='color:#111111;'>" + title + "</h2>"
                + "<p style='color:#6f6f6f;'><b>Genre:</b> Action / Sci-Fi<br>"
                + "<b>Duration:</b> 148 mins<br>"
                + "<b>Rating:</b> 8.5/10<br><br>"
                + "<i>Details dynamically retrieved from the database.</i></p>"
                + "</body></html>";

        JOptionPane.showMessageDialog(view.getFrame(), info, "Movie Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void initializeBookingListeners() {

        // --- Now Showing -> Movie Details Routing ---
        view.getNowShowingPanel().setMovieClickListener(movieData -> {
            String title = movieData[0];
            String genre = movieData[1];
            String duration = movieData[2];
            String rating = movieData[3];
            String desc = movieData[4];

            view.getMovieDetailsPanel().loadMovieDetails(title, genre, duration, rating, desc);
            view.showCard("movie_details");
        });

        view.getMovieDetailsPanel().getBackButton().addActionListener(e -> {
            view.showCard("now_showing");
        });

        view.getMovieDetailsPanel().getBookButton().addActionListener(e -> {
            String targetMovie = view.getMovieDetailsPanel().getCurrentMovieTitle();
            view.getShowSelectionPanel().preselectMovieByTitle(targetMovie);
            view.setActiveNav(2);
            view.showCard("show_selection");
        });

        // --- Show Selection -> Seat Map ---
        view.getShowSelectionPanel().getProceedButton().addActionListener(e -> {
            String movie = view.getShowSelectionPanel().getSelectedMovieTitle();
            String timeHall = view.getShowSelectionPanel().getSelectedTimeSlot();

            if (movie == null || timeHall == null) {
                JOptionPane.showMessageDialog(view.getFrame(),
                        "Please select both a movie and a time slot to continue.",
                        "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            view.getSeatMapPanel().reset(movie, timeHall);
            view.showCard("seatmap");
        });

        view.getSeatMapPanel().getBackButton().addActionListener(e -> {
            view.showCard("show_selection");
        });

        // --- Seat Map Checkout -> Receipt Panel ---
        view.getSeatMapPanel().getCheckoutButton().addActionListener(e -> {
            String seats = String.join(", ", view.getSeatMapPanel().getSelectedSeats());
            int adults = view.getSeatMapPanel().getAdultCount();
            int kids = view.getSeatMapPanel().getKidsCount();
            double total = view.getSeatMapPanel().getTotalAmount();

            String summary = String.format(
                    "Confirm Booking?\n\nSeats: %s\nAdults: %d\nKids: %d\nTotal Amount: $%.2f",
                    seats, adults, kids, total
            );

            int confirm = JOptionPane.showConfirmDialog(
                    view.getFrame(), summary, "Confirm Checkout", JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                LoadingDialog.runWithLoading(view.getFrame(), "Generating ticket & QR code...", () -> {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException ex) {
                    }
                }, () -> {
                    // Populate Receipt Panel Data
                    String generatedId = "BK-" + (1000 + (int) (Math.random() * 9000));
                    String movie = view.getShowSelectionPanel().getSelectedMovieTitle();
                    String timeHall = view.getShowSelectionPanel().getSelectedTimeSlot();
                    String totalStr = String.format("$%.2f", total);

                    view.getReceiptPanel().updateReceipt(generatedId, movie, timeHall, seats, totalStr);
                    view.showCard("receipt");
                });
            }
        });

        // --- Receipt Panel Actions ---
        view.getReceiptPanel().getDownloadButton().addActionListener(e -> {
            JOptionPane.showMessageDialog(view.getFrame(),
                    "Downloading digital receipt...\nSaved to Documents folder as PDF.",
                    "Download Complete", JOptionPane.INFORMATION_MESSAGE);
        });

        view.getReceiptPanel().getHomeButton().addActionListener(e -> {
            view.setActiveNav(0);
            view.showCard("dashboard");
        });

        view.getReceiptPanel().getCancellationDeskButton().addActionListener(e -> {
            view.setActiveNav(4);
            view.getTicketCancellationPanel().reset();
            view.showCard("cancellation_desk");
        });

        // --- History Panel Actions ---
        view.getBookingHistoryPanel().getSearchButton().addActionListener(e -> {
            String query = view.getBookingHistoryPanel().getSearchField().getText().trim();
            if (query.isEmpty()) {
                JOptionPane.showMessageDialog(view.getFrame(), "Please enter a Booking ID or Movie Title.");
            } else {
                JOptionPane.showMessageDialog(view.getFrame(), "Searching history for: " + query);
            }
        });

        view.getBookingHistoryPanel().getDownloadReceiptButton().addActionListener(e -> {
            int selectedRow = view.getBookingHistoryPanel().getHistoryTable().getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(view.getFrame(),
                        "Please select a booking from the table first.",
                        "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String bookingId = (String) view.getBookingHistoryPanel().getHistoryTable().getValueAt(selectedRow, 0);
            JOptionPane.showMessageDialog(view.getFrame(),
                    "Downloading receipt for " + bookingId + "...\nReceipt saved to Documents folder as PDF.",
                    "Download Complete", JOptionPane.INFORMATION_MESSAGE);
        });

        // --- Cancellation Desk Actions ---
        view.getTicketCancellationPanel().getSearchButton().addActionListener(e -> {
            String input = view.getTicketCancellationPanel().getSearchField().getText().trim();
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(view.getFrame(), "Please enter a Booking ID.");
                return;
            }
            view.getTicketCancellationPanel().showTicketDetails(
                    input.toUpperCase(), "Dune: Part Two", "2 Adult, 1 Child (Seats A1, A2, A3)", "$40.00", "Pending"
            );
        });

        view.getTicketCancellationPanel().getRefundButton().addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(view.getFrame(),
                    "Are you sure you want to cancel this ticket and process a refund?",
                    "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                LoadingDialog.runWithLoading(view.getFrame(), "Processing cancellation & updating QR code...", () -> {
                    try {
                        Thread.sleep(1200);
                    } catch (InterruptedException ex) {
                    }
                }, () -> {
                    JOptionPane.showMessageDialog(view.getFrame(),
                            "Ticket cancelled successfully. Refund initialized and QR code updated.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                    view.getTicketCancellationPanel().logActivity("Refunded");
                    view.getTicketCancellationPanel().showTicketDetails(
                            view.getTicketCancellationPanel().getSearchField().getText().toUpperCase(),
                            "Dune: Part Two", "2 Adult, 1 Child", "$40.00", "Cancelled"
                    );
                });
            }
        });

        view.getTicketCancellationPanel().getCheckInButton().addActionListener(e -> {
            LoadingDialog.runWithLoading(view.getFrame(), "Validating ticket & updating QR code...", () -> {
                try {
                    Thread.sleep(1200);
                } catch (InterruptedException ex) {
                }
            }, () -> {
                JOptionPane.showMessageDialog(view.getFrame(),
                        "Ticket Validated. Customer checked in. QR code updated.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

                view.getTicketCancellationPanel().logActivity("Checked-In");
                view.getTicketCancellationPanel().showTicketDetails(
                        view.getTicketCancellationPanel().getSearchField().getText().toUpperCase(),
                        "Dune: Part Two", "2 Adult, 1 Child", "$40.00", "Checked-In"
                );
            });
        });
    }

    private void loadDashboardStats() {
        SwingWorker<int[], Void> worker = new SwingWorker<>() {
            @Override
            protected int[] doInBackground() {
                try {
                    int movieCount = Movie.getActiveCount();
                    int showCount = 8;
                    int bookingCount = Booking.getTodaysBookingCount();
                    int ticketsSold = bookingCount * 2;
                    return new int[]{movieCount, showCount, bookingCount, ticketsSold};
                } catch (Exception e) {
                    e.printStackTrace();
                    return new int[]{0, 0, 0, 0};
                }
            }

            @Override
            protected void done() {
                try {
                    int[] stats = get();
                    view.updateStats(stats[0], stats[1], stats[2], stats[3]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void loadTodayShows() {
        view.clearShowsTable();

        Object[][] dummyData = {
            {"\u2139", "SH-101", "Dune: Part Two", "Hall 1", "14:30 PM", "45 / 120", "75"},
            {"\u2139", "SH-102", "Kung Fu Panda 4", "Hall 2", "15:00 PM", "120 / 120", "0"},
            {"\u2139", "SH-103", "Godzilla x Kong", "Hall 3", "16:45 PM", "10 / 80", "70"},
            {"\u2139", "SH-105", "Civil War", "Hall 2", "19:30 PM", "200 / 200", "0"}
        };

        for (Object[] row : dummyData) {
            view.addShowToTable(row);
        }
    }
}
