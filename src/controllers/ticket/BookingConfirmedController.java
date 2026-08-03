package controllers.ticket;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class BookingConfirmedController {

    @FXML private Label bookingIdLabel;
    @FXML private Label movieTitleLabel;
    @FXML private Label showtimeLabel;
    @FXML private Label seatsLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label discountLabel;
    @FXML private Label totalPaidLabel;
    @FXML private Label ticketBadgeLabel;
    @FXML private javafx.scene.layout.VBox qrCodeContainer;
    @FXML private javafx.scene.layout.VBox receiptCard;

    public void setReceiptData(String bookingId, String movieTitle, String showtime, String seats, double subtotal, double discount, double totalPaid) {
        bookingIdLabel.setText("Booking ID: " + bookingId);
        movieTitleLabel.setText(movieTitle);
        showtimeLabel.setText(showtime);
        seatsLabel.setText("Seats: " + seats);
        subtotalLabel.setText(String.format("$%.2f", subtotal));
        discountLabel.setText(String.format("-$%.2f", discount));
        totalPaidLabel.setText(String.format("$%.2f", totalPaid));

        if (ticketBadgeLabel != null) {
            ticketBadgeLabel.setText("CONFIRMED");
            ticketBadgeLabel.getStyleClass().removeAll("badge-confirmed", "badge-cancelled", "badge-pending");
            ticketBadgeLabel.getStyleClass().add("badge-confirmed");
        }

        // Generate QR Code with complete detailed lines (not minimum way)
        String qrData = "CineSphere Ticket\n" +
                        "Booking ID: " + bookingId + "\n" +
                        "Movie: " + movieTitle + "\n" +
                        "Showtime: " + showtime + "\n" +
                        "Seats: " + seats + "\n" +
                        "Subtotal: " + String.format("$%.2f", subtotal) + "\n" +
                        "Discount: " + String.format("-$%.2f", discount) + "\n" +
                        "Total Paid: " + String.format("$%.2f", totalPaid);
                        
        javafx.scene.image.Image qrImg = utils.QRCodeUtils.generateQRCodeImage(qrData, 180, 180);
        if (qrImg != null) {
            javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(qrImg);
            imgView.setFitWidth(180);
            imgView.setFitHeight(180);
            qrCodeContainer.getChildren().clear();
            qrCodeContainer.getChildren().add(imgView);
            qrCodeContainer.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;"); // Remove dashed border
        }
    }

    @FXML
    public void handleDownloadReceipt() {
        String bId = bookingIdLabel.getText().replace("Booking ID: ", "");
        utils.ReceiptUtils.downloadReceiptAsImage(receiptCard, bookingIdLabel.getScene().getWindow(), "Receipt_" + bId);
    }

    @FXML
    public void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ticket/TicketOverview.fxml"));
            Parent root = loader.load();
            StackPane contentArea = (StackPane) bookingIdLabel.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleTicketDesk() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ticket/BookingHistory.fxml"));
            Parent root = loader.load();
            StackPane contentArea = (StackPane) bookingIdLabel.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
