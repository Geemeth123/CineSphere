/**
 * Handle user interactions and UI logic for the AddNewMovieTypeDialog view.
 */
package controllers.admin;

import controllers.MainLayoutController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class AddNewMovieTypeDialogController {
    
    @FXML private Button btnCancel;

    @FXML
    public void handleFetchTMDB() {
        // Close dialog
        closeDialog();
        // Navigate to TMDBSearch
        if (MainLayoutController.getInstance() != null) {
            MainLayoutController.getInstance().loadPageDirectly("/views/admin/TMDBSearch.fxml");
        }
    }

    @FXML
    public void handleAddManually() {
        closeDialog();
        if (MainLayoutController.getInstance() != null) {
            MainLayoutController.getInstance().loadPageDirectly("/views/admin/AddMovieManually.fxml");
        }
    }

    @FXML
    public void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}

