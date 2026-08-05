/**
 * handle user interactions and UI logic for the NowShowing view.
 */
package controllers.ticket;

import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import models.Movie;

public class NowShowingController {

    @FXML private ComboBox<String> genreFilterCombo;
    @FXML private TextField searchField;
    @FXML private FlowPane moviesGrid;

    private List<Movie> allMovies;

    @FXML
    public void initialize() {
        genreFilterCombo.getItems().addAll("All Genres", "Action", "Comedy", "Drama", "Science Fiction", "Horror", "Thriller", "Animation");
        genreFilterCombo.getSelectionModel().selectFirst();
        
        genreFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> filterMovies());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterMovies());
        
        // Show loading spinner
        VBox loaderContainer = new VBox(15);
        loaderContainer.setAlignment(Pos.CENTER);
        loaderContainer.setPadding(new Insets(100, 0, 0, 0));
        
        atlantafx.base.controls.RingProgressIndicator loader = new atlantafx.base.controls.RingProgressIndicator();
        loader.setProgress(-1);
        
        Label waitLbl = new Label("Please wait, loading movies...");
        waitLbl.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d;");
        
        loaderContainer.getChildren().addAll(loader, waitLbl);
        moviesGrid.getChildren().clear();
        moviesGrid.setAlignment(Pos.CENTER);
        moviesGrid.getChildren().add(loaderContainer);
        
        // Fetch movies from local DB that have active showtimes
        new Thread(() -> {
            models.ShowDAO dao = new models.ShowDAO();
            allMovies = dao.getActiveMoviesWithShowtimes();
            Platform.runLater(this::filterMovies);
        }).start();
    }

    private void filterMovies() {
        if (allMovies == null) return;
        
        String searchQuery = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        String selectedGenre = genreFilterCombo != null ? genreFilterCombo.getValue() : "All Genres";
        
        List<Movie> filtered = allMovies.stream()
            .filter(m -> {
                boolean matchesSearch = m.getTitle().toLowerCase().contains(searchQuery);
                boolean matchesGenre = "All Genres".equals(selectedGenre) || (m.getGenre() != null && m.getGenre().equalsIgnoreCase(selectedGenre));
                return matchesSearch && matchesGenre;
            })
            .collect(Collectors.toList());
            
        populateGrid(filtered);
    }

    private void populateGrid(List<Movie> movies) {
        moviesGrid.getChildren().clear();
        moviesGrid.setAlignment(Pos.TOP_LEFT);
        
        if (movies == null || movies.isEmpty()) {
            Label emptyLbl = new Label("No active movies available in theater right now.");
            emptyLbl.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d; -fx-padding: 30;");
            moviesGrid.getChildren().add(emptyLbl);
            return;
        }

        for (Movie movie : movies) {
            VBox card = createMovieCard(movie);
            moviesGrid.getChildren().add(card);
        }
    }

    private VBox createMovieCard(Movie movie) {
        VBox card = new VBox();
        card.getStyleClass().add("movie-grid-card");
        card.setPrefWidth(350);
        card.setSpacing(15);
        card.setPadding(new Insets(20));

        // Genres and Rating
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        Label genreLabel = new Label(movie.getGenre() != null ? movie.getGenre().toUpperCase() : "GENERAL");
        genreLabel.setStyle("-fx-text-fill: #0d6efd; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label ratingLabel = new Label("\u2B50 " + String.format("%.1f", movie.getRating()));
        ratingLabel.setStyle("-fx-text-fill: #ffc107; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        topRow.getChildren().addAll(genreLabel, spacer, ratingLabel);

        // Title
        Label titleLabel = new Label(movie.getTitle());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #212529;");
        titleLabel.setWrapText(true);

        // Subtitle (Duration)
        Label subtitleLabel = new Label(movie.getRuntime() + " \u2022 Active Showing");
        subtitleLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 14px;");

        // Action Button Spacer
        Region buttonSpacer = new Region();
        VBox.setVgrow(buttonSpacer, Priority.ALWAYS);

        // Action Button
        Button detailsBtn = new Button("View Details");
        detailsBtn.getStyleClass().add("search-btn");
        detailsBtn.setMaxWidth(Double.MAX_VALUE);
        detailsBtn.setOnAction(e -> openMovieDetails(movie));

        card.getChildren().addAll(topRow, titleLabel, subtitleLabel, buttonSpacer, detailsBtn);
        return card;
    }

    private void openMovieDetails(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ticket/MovieDetails.fxml"));
            Parent root = loader.load();
            
            MovieDetailsController controller = loader.getController();
            controller.setLocalMovie(movie);
            
            StackPane contentArea = (StackPane) moviesGrid.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

