/**
 * Edit Hall Controller (Scheduler User Role)
 * 
 * Responsibility:
 * 1. Loads an existing cinema hall's properties (Name, Type, Dimensions, Kids Hall status).
 * 2. Renders an interactive seat layout grid where individual seats can be toggled in/out of MAINTENANCE status.
 * 3. Persists updated hall details and seat maintenance records in the database.
 */
package controllers.scheduler;

import java.util.ArrayList;
import java.util.List;

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

public class EditHallController {

    // Form inputs & UI grid container
    @FXML private TextField nameField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField rowsField;
    @FXML private TextField colsField;
    @FXML private CheckBox kidsHallCheck;
    @FXML private Label errorLabel;
    @FXML private GridPane seatGridPreview;

    private Hall currentHall;
    private HallDAO hallDAO = new HallDAO();
    // List tracking seat labels marked for maintenance (e.g. ["A1", "B4"])
    private List<String> maintenanceSeats = new ArrayList<>();

    @FXML
    public void initialize() {
        typeComboBox.setItems(FXCollections.observableArrayList(
                "Digital 2D", "IMAX", "Dolby Atmos", "Scope", "3D"
        ));
    }

    /**
     * Initializes the edit form with target Hall data and loads seat maintenance statuses from DB.
     */
    public void setHallData(Hall hall) {
        this.currentHall = hall;
        nameField.setText(hall.getName());
        typeComboBox.setValue(hall.getType());
        rowsField.setText(String.valueOf(hall.getSeatRows()));
        colsField.setText(String.valueOf(hall.getSeatColumns()));
        kidsHallCheck.setSelected(hall.isKidsHall());

        // Fetch currently broken/under-maintenance seats from DB
        maintenanceSeats = hallDAO.getMaintenanceSeats(hall.getId());
        
        generateSeatGrid();
    }

    /**
     * Generates interactive seat grid buttons.
     * Seats under maintenance are highlighted in yellow.
     */
    private void generateSeatGrid() {
        seatGridPreview.getChildren().clear();
        int r = currentHall.getSeatRows();
        int c = currentHall.getSeatColumns();
        
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                String seatId = (char)('A' + i) + String.valueOf(j + 1);
                Button seatBtn = new Button(seatId);
                seatBtn.setPrefSize(45, 45);
                
                // Highlight yellow if seat is marked for maintenance
                if (maintenanceSeats.contains(seatId)) {
                    seatBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #ffc107; -fx-text-fill: #000; -fx-border-color: #e0a800; -fx-border-radius: 4px; -fx-background-radius: 4px;");
                } else {
                    seatBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #ffffff; -fx-border-color: #ced4da; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-text-fill: #495057;");
                }
                
                Tooltip tooltip = new Tooltip(seatId);
                seatBtn.setTooltip(tooltip);
                
                // Clicking a seat button toggles its maintenance status
                seatBtn.setOnAction(e -> handleSeatToggle(seatBtn, seatId));
                
                seatGridPreview.add(seatBtn, j, i);
            }
        }
    }

    /**
     * Toggles a seat between AVAILABLE (white) and MAINTENANCE (yellow) when clicked.
     */
    private void handleSeatToggle(Button seatBtn, String seatId) {
        if (maintenanceSeats.contains(seatId)) {
            maintenanceSeats.remove(seatId);
            seatBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #ffffff; -fx-border-color: #ced4da; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-text-fill: #495057;");
        } else {
            maintenanceSeats.add(seatId);
            seatBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #ffc107; -fx-text-fill: #000; -fx-border-color: #e0a800; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        }
    }

    /**
     * Saves updated hall configuration and seat maintenance state to the database.
     */
    @FXML
    public void handleSave(ActionEvent event) {
        String name = nameField.getText();
        String type = typeComboBox.getValue();
        boolean isKids = kidsHallCheck.isSelected();

        if (name == null || name.trim().isEmpty() || type == null) {
            showError("Name and Type are required.");
            return;
        }

        currentHall.setName(name.trim());
        currentHall.setType(type);
        currentHall.setKidsHall(isKids);

        if (hallDAO.updateHall(currentHall)) {
            // Update seat maintenance statuses in seats table
            hallDAO.updateMaintenanceSeats(currentHall.getId(), maintenanceSeats);
            MainLayoutController.getInstance().loadPageDirectly("/views/scheduler/HallManagement.fxml");
        } else {
            showError("Failed to update hall details.");
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

