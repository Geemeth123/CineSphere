package controllers.ticket;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeatSelectionController {

    @FXML private GridPane seatGrid;
    @FXML private Label movieTitleLabel;
    @FXML private Label showtimeLabel;
    @FXML private Label selectedSeatsLabel;
    @FXML private Label adultCountLabel;
    @FXML private Label childCountLabel;
    @FXML private Label totalAmountLabel;
    @FXML private Button proceedBtn;

    private int adultCount = 0;
    private int childCount = 0;
    private double adultPrice = 350.0;
    private double childPrice = 200.0;
    private double discountPercentage = 0.0;
    
    private List<String> selectedSeats = new ArrayList<>();
    
    private String showId;
    private String movieTitle;
    private String showtimeDetails;

    // Initialization method called by BookingTicketController to pass data
    public void setBookingData(String showId, String title, String details) {
        this.showId = showId;
        this.movieTitle = title;
        this.showtimeDetails = details;
        
        movieTitleLabel.setText(title);
        showtimeLabel.setText(details);
        
        try (java.sql.Connection conn = utils.DBUtils.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(
                 "SELECT m.adult_price, m.kids_price FROM shows s JOIN movies m ON s.movie_id = m.id WHERE s.id = ?")) {
            int sId = Integer.parseInt(showId.replace("SH-", ""));
            stmt.setInt(1, sId);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double ap = rs.getDouble("adult_price");
                    double kp = rs.getDouble("kids_price");
                    if (ap > 0) this.adultPrice = ap;
                    if (kp > 0) this.childPrice = kp;
                }
            }
        } catch (Exception e) {}

        // Query active show/movie discounts
        try (java.sql.Connection conn = utils.DBUtils.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(
                 "SELECT MAX(discount_percentage) as discount FROM discounts WHERE status = 'ACTIVE' AND ((target_type = 'SHOW' AND target_id = ?) OR (target_type = 'MOVIE' AND target_id = (SELECT movie_id FROM shows WHERE id = ?)))")) {
            int sId = Integer.parseInt(showId.replace("SH-", ""));
            stmt.setInt(1, sId);
            stmt.setInt(2, sId);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    this.discountPercentage = rs.getDouble("discount");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        generateSeatGrid();
        updateSummary();
    }

    private void generateSeatGrid() {
        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<Void>() {
            @Override
            protected Void call() throws Exception {
                models.BookingDAO dao = new models.BookingDAO();
                int[] dims = dao.getHallDimensions(showId);
                int rows = dims[0];
                int cols = dims[1];
                
                List<String> mockBooked = dao.getBookedSeats(showId);
                
                int hallId = dao.getHallId(showId);
                models.HallDAO hallDAO = new models.HallDAO();
                List<String> maintenanceSeats = hallDAO.getMaintenanceSeats(hallId);
                
                javafx.application.Platform.runLater(() -> {
                    buildSeatGrid(rows, cols, mockBooked, maintenanceSeats);
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    // Reusable method for rendering a seat grid given specific data
    public void buildSeatGrid(int rows, int cols, List<String> bookedSeats, List<String> maintenanceSeats) {
        seatGrid.getChildren().clear();
        
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                String seatId = (char)('A' + r) + String.valueOf(c + 1);
                Button seatBtn = new Button(seatId); // Show text inside seat
                seatBtn.setPrefSize(45, 45); // Make it slightly bigger
                seatBtn.setStyle("-fx-font-size: 10px;"); // Ensure it fits
                
                // Add Tooltip for seat number hover
                Tooltip tooltip = new Tooltip(seatId);
                seatBtn.setTooltip(tooltip);
                
                if (maintenanceSeats != null && maintenanceSeats.contains(seatId)) {
                    seatBtn.getStyleClass().add("seat-btn-maintenance");
                    seatBtn.setDisable(true);
                } else if (bookedSeats != null && bookedSeats.contains(seatId)) {
                    seatBtn.getStyleClass().add("seat-btn-booked");
                    seatBtn.setDisable(true);
                } else {
                    seatBtn.getStyleClass().add("seat-btn");
                    seatBtn.setOnAction(e -> handleSeatToggle(seatBtn, seatId));
                }
                
                seatGrid.add(seatBtn, c, r);
            }
        }
    }

    private void handleSeatToggle(Button seatBtn, String seatId) {
        if (selectedSeats.contains(seatId)) {
            selectedSeats.remove(seatId);
            seatBtn.getStyleClass().remove("seat-btn-selected");
            if (!seatBtn.getStyleClass().contains("seat-btn")) {
                seatBtn.getStyleClass().add("seat-btn");
            }
        } else {
            selectedSeats.add(seatId);
            seatBtn.getStyleClass().remove("seat-btn");
            if (!seatBtn.getStyleClass().contains("seat-btn-selected")) {
                seatBtn.getStyleClass().add("seat-btn-selected");
            }
        }
        
        // Auto-increment ticket counter logic
        if (getTotalTickets() < selectedSeats.size()) {
            adultCount++; // Default assign to adult
        } else if (getTotalTickets() > selectedSeats.size()) {
            // Need to decrement
            if (adultCount > 0) adultCount--;
            else if (childCount > 0) childCount--;
        }
        
        updateSummary();
    }

    @FXML
    public void handleClearSeats() {
        selectedSeats.clear();
        adultCount = 0;
        childCount = 0;
        
        for (javafx.scene.Node node : seatGrid.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                if (btn.getStyleClass().contains("seat-btn-selected")) {
                    btn.getStyleClass().remove("seat-btn-selected");
                    if (!btn.getStyleClass().contains("seat-btn")) {
                        btn.getStyleClass().add("seat-btn");
                    }
                }
            }
        }
        updateSummary();
    }

    @FXML
    public void incrementAdult() {
        if (getTotalTickets() < selectedSeats.size()) {
            adultCount++;
            updateSummary();
        }
    }

    @FXML
    public void decrementAdult() {
        if (adultCount > 0) {
            adultCount--;
            updateSummary();
        }
    }

    @FXML
    public void incrementChild() {
        if (getTotalTickets() < selectedSeats.size()) {
            childCount++;
            updateSummary();
        }
    }

    @FXML
    public void decrementChild() {
        if (childCount > 0) {
            childCount--;
            updateSummary();
        }
    }

    private int getTotalTickets() {
        return adultCount + childCount;
    }

    private void updateSummary() {
        if (selectedSeats.isEmpty()) {
            selectedSeatsLabel.setText("-");
        } else {
            selectedSeatsLabel.setText(String.join(", ", selectedSeats));
        }

        adultCountLabel.setText(String.valueOf(adultCount));
        childCountLabel.setText(String.valueOf(childCount));

        double subtotal = (adultCount * adultPrice) + (childCount * childPrice);
        if (discountPercentage > 0) {
            double discountAmt = subtotal * (discountPercentage / 100.0);
            double finalTotal = subtotal - discountAmt;
            totalAmountLabel.setText(String.format("$%.2f (%.0f%% Off)", finalTotal, discountPercentage));
        } else {
            totalAmountLabel.setText(String.format("$%.2f", subtotal));
        }

        // Enable proceed if at least 1 seat selected AND ticket count matches seat count
        if (!selectedSeats.isEmpty() && getTotalTickets() == selectedSeats.size()) {
            proceedBtn.setDisable(false);
            proceedBtn.getStyleClass().remove("primary-action-btn-disabled");
            if (!proceedBtn.getStyleClass().contains("primary-action-btn")) {
                proceedBtn.getStyleClass().add("primary-action-btn");
            }
        } else {
            proceedBtn.setDisable(true);
            proceedBtn.getStyleClass().remove("primary-action-btn");
            if (!proceedBtn.getStyleClass().contains("primary-action-btn-disabled")) {
                proceedBtn.getStyleClass().add("primary-action-btn-disabled");
            }
        }
    }

    @FXML
    public void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ticket/BookingTicket.fxml"));
            Parent root = loader.load();
            StackPane contentArea = (StackPane) proceedBtn.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleProceed() {
        double subtotal = (adultCount * adultPrice) + (childCount * childPrice);
        double discountAmt = subtotal * (discountPercentage / 100.0);
        double total = subtotal - discountAmt;
        String formattedTotal = String.format("$%.2f", total);
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Booking");
        alert.setHeaderText("Please confirm the booking details:");
        
        String discountText = "";
        if (discountPercentage > 0) {
            discountText = "\nSubtotal: " + String.format("$%.2f", subtotal) +
                           "\nDiscount (" + String.format("%.0f", discountPercentage) + "%): -" + String.format("$%.2f", discountAmt);
        }
        
        alert.setContentText(
            "Movie: " + movieTitle + "\n" +
            "Showtime: " + showtimeDetails + "\n" +
            "Seats: " + String.join(", ", selectedSeats) + "\n" +
            "Adults: " + adultCount + ", Children: " + childCount + 
            discountText + "\n" +
            "Total Amount: " + formattedTotal
        );

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            proceedBtn.setDisable(true);
            proceedBtn.setText("Booking...");
            
            javafx.concurrent.Task<String> task = new javafx.concurrent.Task<String>() {
                @Override
                protected String call() throws Exception {
                    models.BookingDAO dao = new models.BookingDAO();
                    // Using a dummy user ID = 2 for Counter Staff
                    return dao.createBooking(showId, 2, adultCount, childCount, total, selectedSeats);
                }
            };
            
            task.setOnSucceeded(e -> {
                String bookingId = task.getValue();
                if (bookingId != null) {
                    System.out.println("Booking confirmed and saved to DB! ID: " + bookingId);
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ticket/BookingConfirmed.fxml"));
                        Parent root = loader.load();
                        
                        BookingConfirmedController controller = loader.getController();
                        controller.setReceiptData(
                            bookingId,
                            movieTitle,
                            showtimeDetails,
                            String.join(", ", selectedSeats),
                            subtotal,
                            discountAmt,
                            total
                        );
                        
                        StackPane contentArea = (StackPane) proceedBtn.getScene().lookup("#contentArea");
                        if (contentArea != null) {
                            contentArea.getChildren().clear();
                            contentArea.getChildren().add(root);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR, "Failed to save booking to database!");
                    err.show();
                    proceedBtn.setText("Proceed to Booking");
                    updateSummary();
                }
            });
            
            task.setOnFailed(e -> {
                Alert err = new Alert(Alert.AlertType.ERROR, "An error occurred during booking!");
                err.show();
                proceedBtn.setText("Proceed to Booking");
                updateSummary();
            });
            
            new Thread(task).start();
        }
    }
}
