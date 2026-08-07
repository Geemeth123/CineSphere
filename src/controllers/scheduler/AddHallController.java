/**
 * Add Hall Controller (Scheduler User Role)
 * 
 * Responsibility:
 * 1. Collects details for creating a new cinema hall (Name, Technology Type, Rows, Columns, Kids Hall tag).
 * 2. Dynamically generates a real-time visual seat map preview based on row and column inputs.
 * 3. Enforces row/column size constraints (max 10 rows, 15 columns) and saves new hall & seat records to the database.
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

    // FXML Form Inputs & Components
    @FXML private TextField nameField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField rowsField;
    @FXML private TextField colsField;
    @FXML private CheckBox kidsHallCheck;
    @FXML private Label errorLabel;
    @FXML private GridPane seatGridPreview;

    public boolean saveSuccessful = false;
    private HallDAO hallDAO = new HallDAO();
    
    // Grid Size Constraints: Maximum 10 Rows x 15 Columns allowed per hall
    private static final int MAX_ROWS = 10;
    private static final int MAX_COLS = 15;

    /**
     * Checks whether user-entered dimensions exceed maximum grid limits.
     */
    private boolean exceedsLimits(int rows, int cols) {
        return rows > MAX_ROWS || cols > MAX_COLS;
    }

    @FXML
    public void initialize() {
        // Initialize projection/sound technology types
        typeComboBox.setItems(FXCollections.observableArrayList(
                "Digital 2D", "IMAX", "Dolby Atmos", "Scope", "3D"
        ));
        
        // Add dynamic change listeners to update grid seat map preview instantly as numbers are typed
        rowsField.textProperty().addListener((obs, oldV, newV) -> generatePreview());
        colsField.textProperty().addListener((obs, oldV, newV) -> generatePreview());
    }

    /**
     * Dynamically renders interactive seat buttons in seatGridPreview based on row/column numbers.
     * Seat IDs are named using letter rows (A, B, C...) and column numbers (1, 2, 3...).
     */
    private void generatePreview() {
        seatGridPreview.getChildren().clear();
        errorLabel.setVisible(false);
        
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
                // Build interactive seat grid buttons
                for (int i = 0; i < r; i++) {
                    for (int j = 0; j < c; j++) {
                        // Converts row index 0->'A', 1->'B', 2->'C'
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

    /**
     * Validates hall creation form and persists new hall and auto-generated seat records via HallDAO.
     */
    @FXML
    public void handleSave(ActionEvent event) {
        String name = nameField.getText();
        String type = typeComboBox.getValue();
        String rowsStr = rowsField.getText();
        String colsStr = colsField.getText();
        boolean isKids = kidsHallCheck.isSelected();

        // Validate mandatory input fields
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
            
            // Construct new Hall object and attempt database insertion
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

