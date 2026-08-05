/**
 *handle user interactions and UI logic for the AddHall view.
 */
package controllers.scheduler;

import controllers.MainLayoutController;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import models.Hall;
import models.HallDAO;

public class AddHallController {

    @FXML private TextField nameField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField rowsField;
    @FXML private TextField colsField;
    @FXML private CheckBox kidsHallCheck;
    @FXML private Label errorLabel;
    @FXML private GridPane seatGridPreview;

    public boolean saveSuccessful = false;
    private HallDAO hallDAO = new HallDAO();
    
    private static final int MAX_ROWS = 10;
    private static final int MAX_COLS = 15;

    private boolean exceedsLimits(int rows, int cols) {
        return rows > MAX_ROWS || cols > MAX_COLS;
    }

    @FXML
    public void initialize() {
        typeComboBox.setItems(FXCollections.observableArrayList(
                "Digital 2D", "IMAX", "Dolby Atmos", "Scope", "3D"
        ));
        
        // Listeners for rows and columns to generate preview
        rowsField.textProperty().addListener((obs, oldV, newV) -> generatePreview());
        colsField.textProperty().addListener((obs, oldV, newV) -> generatePreview());
    }

    private void generatePreview() {
        seatGridPreview.getChildren().clear();
        errorLabel.setVisible(false); // Reset error initially
        
        try {
            String rText = rowsField.getText().trim();
            String cText = colsField.getText().trim();
            
            if (rText.isEmpty() || cText.isEmpty()) {
                return;
            }
            
            int r = Integer.parseInt(rText);
            int c = Integer.parseInt(cText);
            
            if (exceedsLimits(r, c)) {
                showError("Max limit exceeded: Rows (" + MAX_ROWS + " max), Columns (" + MAX_COLS + " max).");
                return;
            }
            
            if (r > 0 && c > 0) { 
                for (int i = 0; i < r; i++) {
                    for (int j = 0; j < c; j++) {
                        String seatId = (char)('A' + i) + String.valueOf(j + 1);
                        Button seatBtn = new Button(seatId);
                        seatBtn.setPrefSize(45, 45);
                        seatBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #e9ecef; -fx-border-color: #ced4da; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-text-fill: #495057;");
                        
                        Tooltip tooltip = new Tooltip(seatId);
                        seatBtn.setTooltip(tooltip);
                        
                        seatGridPreview.add(seatBtn, j, i);
                    }
                }
            }
        } catch (NumberFormatException e) {
            showError("Rows and columns must be numbers.");
        }
    }

    @FXML
    public void handleSave(ActionEvent event) {
        String name = nameField.getText();
        String type = typeComboBox.getValue();
        String rowsStr = rowsField.getText();
        String colsStr = colsField.getText();
        boolean isKids = kidsHallCheck.isSelected();

        if (name == null || name.trim().isEmpty() || type == null || 
            rowsStr == null || rowsStr.trim().isEmpty() || colsStr == null || colsStr.trim().isEmpty()) {
            showError("All fields are required.");
            return;
        }

        try {
            int rows = Integer.parseInt(rowsStr);
            int cols = Integer.parseInt(colsStr);
            
            if (rows <= 0 || cols <= 0) {
                showError("Rows and Columns must be > 0.");
                return;
            }
            
            if (exceedsLimits(rows, cols)) {
                showError("Max limit exceeded: Rows (" + MAX_ROWS + " max), Columns (" + MAX_COLS + " max).");
                return;
            }

            int totalSeats = rows * cols;
            
            Hall newHall = new Hall(0, name.trim(), type, totalSeats, rows, cols, "ACTIVE", isKids, null);
            if (hallDAO.addHall(newHall)) {
                saveSuccessful = true;
                MainLayoutController.getInstance().loadPageDirectly("/views/scheduler/HallManagement.fxml");
            } else {
                showError("Failed to add hall. Name must be unique.");
            }

        } catch (NumberFormatException e) {
            showError("Rows and columns must be numbers.");
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        MainLayoutController.getInstance().loadPageDirectly("/views/scheduler/HallManagement.fxml");
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}

