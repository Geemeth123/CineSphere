/**
 * handle user interactions and UI logic for the SnackReceipt view.
 */
package controllers.snackbar;

import java.math.BigDecimal;
import java.util.List;

import controllers.MainLayoutController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import models.SnackSale;
import models.SnackSaleItem;

public class SnackReceiptController {
    @FXML private VBox receiptCard;
    @FXML private Label saleIdLabel;
    @FXML private Label dateLabel;
    @FXML private Label cashierLabel;
    @FXML private Label bookingIdLabel;
    @FXML private Label seatLabel;
    @FXML private VBox itemsContainer;
    @FXML private Label subtotalLabel;
    @FXML private Label discountLabel;
    @FXML private Label totalLabel;
    @FXML private Label receiptBadgeLabel;

    private SnackSale currentSale;

    public void setReceiptData(SnackSale sale, List<SnackSaleItem> items) {
        this.currentSale = sale;
        saleIdLabel.setText("Sale ID: RCPT-" + sale.getId());
        dateLabel.setText("Date: " + (sale.getSaleTime() != null ? sale.getSaleTime().toString().replace("T", " ") : "N/A"));
        
        String cashier = sale.getCashierName();
        if (cashier == null || cashier.isEmpty()) { cashier = "Admin"; }
        cashierLabel.setText("Cashier: " + cashier);
        
        if (sale.getBookingId() != null && sale.getBookingId() > 0) {
            bookingIdLabel.setText("Booking ID: BK-" + sale.getBookingId());
            bookingIdLabel.setVisible(true);
            bookingIdLabel.setManaged(true);
            
            if (sale.getSeatNumber() != null && !sale.getSeatNumber().isEmpty()) {
                seatLabel.setText("Seat Number: " + sale.getSeatNumber());
                seatLabel.setVisible(true);
                seatLabel.setManaged(true);
            } else {
                seatLabel.setVisible(false);
                seatLabel.setManaged(false);
            }
        } else {
            bookingIdLabel.setVisible(false);
            bookingIdLabel.setManaged(false);
            seatLabel.setVisible(false);
            seatLabel.setManaged(false);
        }

        // Populate items
        itemsContainer.getChildren().clear();
        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (SnackSaleItem item : items) {
            HBox itemRow = new HBox();
            Label nameLbl = new Label(item.getSnackName() + " (x" + item.getQuantity() + ")");
            nameLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #495057;");
            
            Region r = new Region();
            HBox.setHgrow(r, javafx.scene.layout.Priority.ALWAYS);
            
            Label totalLbl = new Label(String.format("$%.2f", item.getLineTotal()));
            totalLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #212529;");
            
            itemRow.getChildren().addAll(nameLbl, r, totalLbl);
            itemsContainer.getChildren().add(itemRow);
            
            subtotal = subtotal.add(item.getPriceAtSale().multiply(new BigDecimal(item.getQuantity())));
        }

        subtotalLabel.setText(String.format("$%.2f", subtotal));
        BigDecimal discount = subtotal.subtract(sale.getTotalAmount());
        discountLabel.setText(String.format("-$%.2f", discount));
        totalLabel.setText(String.format("$%.2f", sale.getTotalAmount()));
    }

    @FXML
    public void handleDownloadReceipt() {
        if (currentSale != null) {
            utils.ReceiptUtils.downloadReceiptAsImage(receiptCard, saleIdLabel.getScene().getWindow(), "Receipt_Snack_" + currentSale.getId());
        }
    }

    @FXML
    public void handleBack(ActionEvent event) {
        MainLayoutController.getInstance().loadPageDirectly("/views/snackbar/SnackBills.fxml");
    }
}

