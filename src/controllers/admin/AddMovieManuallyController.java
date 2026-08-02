package controllers.admin;

import controllers.MainLayoutController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import models.Movie;
import models.MovieDAO;

import java.io.File;
import java.time.format.DateTimeFormatter;

import javafx.scene.control.ComboBox;

public class AddMovieManuallyController {

    @FXML private TextField titleField;
    @FXML private TextField taglineField;
    @FXML private TextField genreField;
    @FXML private ComboBox<String> genreComboBox;
    @FXML private TextField durationField;
    @FXML private TextField ratingField;
    @FXML private TextField popularityField;
    @FXML private TextField releaseDateField;
    @FXML private TextArea synopsisArea;
    
    @FXML private Label posterLabel;
    @FXML private Label bannerLabel;
    
    @FXML private TextField adultPriceField;
    @FXML private TextField kidsPriceField;
    @FXML private DatePicker showingFromPicker;
    @FXML private DatePicker showingUntilPicker;

    private String posterPath = "";
    private String bannerPath = "";

    private MovieDAO movieDAO = new MovieDAO();
    
    @FXML
    public void initialize() {
        if (genreComboBox != null) {
            genreComboBox.getItems().addAll(
                "Action", "Comedy", "Drama", "Sci-Fi", "Horror", "Romance", "Thriller", "Documentary", "Animation", "Family"
            );
            genreComboBox.getSelectionModel().selectFirst();
        }
    }

    @FXML
    public void handleBack() {
        if (MainLayoutController.getInstance() != null) {
            MainLayoutController.getInstance().loadPageDirectly("/views/admin/MovieManagement.fxml");
        }
    }

    @FXML
    public void handleChoosePoster() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Poster Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(titleField.getScene().getWindow());
        if (selectedFile != null) {
            posterPath = selectedFile.toURI().toString();
            posterLabel.setText(selectedFile.getName());
        }
    }

    @FXML
    public void handleChooseBanner() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Cinematic Banner Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(titleField.getScene().getWindow());
        if (selectedFile != null) {
            bannerPath = selectedFile.toURI().toString();
            bannerLabel.setText(selectedFile.getName());
        }
    }

    @FXML
    public void handleSave() {
        String title = titleField != null ? titleField.getText().trim() : "";
        String tagline = taglineField != null ? taglineField.getText().trim() : "";
        String genre = "";
        if (genreComboBox != null && genreComboBox.getValue() != null) {
            genre = genreComboBox.getValue();
        } else if (genreField != null) {
            genre = genreField.getText().trim();
        }
        String duration = durationField != null ? durationField.getText().trim() : "";
        String synopsis = synopsisArea != null ? synopsisArea.getText().trim() : "";
        String ratingStr = ratingField != null && !ratingField.getText().trim().isEmpty() ? ratingField.getText().trim() : "0.0";
        String popularityStr = popularityField != null && !popularityField.getText().trim().isEmpty() ? popularityField.getText().trim() : "0.0";
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String releaseDate = releaseDateField != null && !releaseDateField.getText().trim().isEmpty() ? releaseDateField.getText().trim() : "";
        if (releaseDate.isEmpty() && showingFromPicker != null && showingFromPicker.getValue() != null) {
            releaseDate = showingFromPicker.getValue().format(dtf);
        }
        
        String adultPriceStr = adultPriceField != null ? adultPriceField.getText().trim() : "";
        String kidsPriceStr = kidsPriceField != null ? kidsPriceField.getText().trim() : "";

        if (title.isEmpty() || genre.isEmpty() || duration.isEmpty() || synopsis.isEmpty() ||
            adultPriceStr.isEmpty() || kidsPriceStr.isEmpty() || showingFromPicker == null || showingFromPicker.getValue() == null ||
            showingUntilPicker == null || showingUntilPicker.getValue() == null || posterPath.isEmpty() || bannerPath.isEmpty()) {
            
            Alert alert = new Alert(Alert.AlertType.ERROR, "All required fields must be filled and images selected.");
            alert.showAndWait();
            return;
        }

        try {
            int durationInt = Integer.parseInt(duration);
            double adultPrice = Double.parseDouble(adultPriceStr);
            double kidsPrice = Double.parseDouble(kidsPriceStr);
            double rating = Double.parseDouble(ratingStr);
            double popularity = Double.parseDouble(popularityStr);

            if (durationInt < 0 || adultPrice < 0 || kidsPrice < 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Duration and prices cannot be negative.");
                alert.showAndWait();
                return;
            }
            if (rating < 0.0 || rating > 10.0) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Rating must be between 0.0 and 10.0.");
                alert.showAndWait();
                return;
            }
            
            if (showingUntilPicker.getValue().isBefore(showingFromPicker.getValue())) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Showing Until date cannot be before Showing From date.");
                alert.showAndWait();
                return;
            }
            String showingFrom = showingFromPicker.getValue().format(dtf);
            String showingUntil = showingUntilPicker.getValue().format(dtf);

            if (!releaseDate.isEmpty()) {
                try {
                    java.time.LocalDate.parse(releaseDate, dtf);
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Release Date must be in YYYY-MM-DD format.");
                    alert.showAndWait();
                    return;
                }
            }
            
            // Copy files locally
            try {
                java.io.File posterDir = new java.io.File("data/movies/posters");
                java.io.File bannerDir = new java.io.File("data/movies/banners");
                if (!posterDir.exists()) posterDir.mkdirs();
                if (!bannerDir.exists()) bannerDir.mkdirs();
                
                if (posterPath.startsWith("file:/")) {
                    java.io.File src = new java.io.File(java.net.URI.create(posterPath));
                    java.io.File dest = new java.io.File(posterDir, "manual_poster_" + System.currentTimeMillis() + "_" + src.getName());
                    java.nio.file.Files.copy(src.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    posterPath = dest.toURI().toString();
                }
                if (bannerPath.startsWith("file:/")) {
                    java.io.File src = new java.io.File(java.net.URI.create(bannerPath));
                    java.io.File dest = new java.io.File(bannerDir, "manual_banner_" + System.currentTimeMillis() + "_" + src.getName());
                    java.nio.file.Files.copy(src.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    bannerPath = dest.toURI().toString();
                }
            } catch (java.io.IOException ex) {
                ex.printStackTrace();
            }

            Movie movie = new Movie("-1", title, genre, durationInt + " mins", synopsis, null);
            movie.setPosterPath(posterPath);
            movie.setBannerPath(bannerPath);
            movie.setRating(rating);
            movie.setPopularity(popularity);
            movie.setReleaseDate(releaseDate);
            movie.setTagline(tagline);
            movie.setAdultPrice(adultPrice);
            movie.setKidsPrice(kidsPrice);
            movie.setShowingFrom(showingFrom);
            movie.setShowingUntil(showingUntil);
            
            boolean success = movieDAO.addManualMovie(movie);
            if (success) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Movie successfully added to the theater!");
                alert.showAndWait();
                handleBack(); // Navigate back on success
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save movie to database.");
                alert.showAndWait();
            }

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter valid numeric values for Duration and Prices.");
            alert.showAndWait();
        }
    }
}
