package controllers.ticket;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Button;
import models.Movie;
import models.MovieDTO;
import utils.TMDBUtils;
import controllers.admin.EditMoviePricingDialogController;
import models.MovieDAO;

/**
 * Controller for displaying detailed information about a movie, including overview,
 * cast ratings, banner, genres, and booking or editing actions.
 */
public class MovieDetailsController {

    @FXML
    private StackPane heroBanner;
    @FXML
    private ImageView posterImage;
    @FXML
    private Label titleLabel;
    @FXML
    private Label taglineLabel;
    @FXML
    private FlowPane genresBox;
    @FXML
    private Label ratingLabel;
    @FXML
    private Label yearLabel;
    @FXML
    private Label languageLabel;
    @FXML
    private Label durationLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label overviewLabel;
    @FXML
    private javafx.scene.layout.VBox contentContainer;
    @FXML
    private javafx.scene.layout.VBox loadingOverlay;

    private boolean isAdminMode = false;
    private boolean isAddNewMode = false;
    private Movie localMovie;
    private int currentMovieId;
    private MovieDTO currentFetchedDto;
    private MovieDAO movieDAO = new MovieDAO();

    @FXML
    private Button actionButton;

    /**
     * Initializes the controller view.
     */
    @FXML
    public void initialize() {
        // Initial setup if required
    }

    private void showLoader() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(true);
            loadingOverlay.setManaged(true);
            contentContainer.setVisible(false);
        }
    }

    private void hideLoader() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(false);
            loadingOverlay.setManaged(false);
            contentContainer.setVisible(true);
        }
    }

    /**
     * Sets whether the view is operating in admin edit mode.
     * @param admin true to enable admin mode
     */
    public void setAdminMode(boolean admin) {
        this.isAdminMode = admin;
        this.isAddNewMode = false;
    }

    /**
     * Sets whether the view is in "Add New Movie" mode.
     * @param addNew true to enable add-new mode
     */
    public void setAddNewMode(boolean addNew) {
        this.isAddNewMode = addNew;
        this.isAdminMode = false;
    }

    /**
     * Loads details for a locally stored movie entity.
     * @param movie the Movie model to populate
     */
    public void setLocalMovie(Movie movie) {
        this.localMovie = movie;
        if (movie != null) {
            populateLocalDetails(movie);
        }
        if (movie != null && movie.getTmdbId() > 0) {
            fetchDetailsAsync(movie.getTmdbId(), movie);
        } else if (movie != null) {
            updateActionButtons();
            hideLoader();
        }
    }

    /**
     * Loads details for a given TMDB movie ID asynchronously.
     * @param tmdbId the TMDB movie identifier
     */
    public void setMovieId(int tmdbId) {
        this.currentMovieId = tmdbId;
        if (tmdbId <= 0) return;
        fetchDetailsAsync(tmdbId, localMovie);
    }

    /**
     * Helper to fetch movie details asynchronously from TMDB and update UI.
     * @param tmdbId TMDB movie ID to fetch
     * @param fallback Movie object to use if remote fetch returns null
     */
    private void fetchDetailsAsync(int tmdbId, Movie fallback) {
        this.currentMovieId = tmdbId;
        if (fallback == null) {
            showLoader();
        }
        new Thread(() -> {
            MovieDTO dto = TMDBUtils.getMovieDetails(tmdbId);
            this.currentFetchedDto = dto;
            Platform.runLater(() -> {
                if (dto != null) {
                    populateDetails(dto);
                } else if (fallback != null) {
                    populateLocalDetails(fallback);
                } else {
                    titleLabel.setText("Movie Details");
                }
                updateActionButtons();
                hideLoader();
            });
        }).start();
    }

    /**
     * Populates UI fields using local Movie object data.
     * @param movie Local Movie entity
     */
    private void populateLocalDetails(Movie movie) {
        if (movie == null) return;
        titleLabel.setText(movie.getTitle());
        taglineLabel.setText(movie.getTagline() != null && !movie.getTagline().isEmpty() ? "\"" + movie.getTagline() + "\"" : "");
        ratingLabel.setText(String.format("⭐ %.1f", movie.getRating()));

        if (movie.getReleaseDate() != null && movie.getReleaseDate().length() >= 4) {
            yearLabel.setText(movie.getReleaseDate().substring(0, 4));
        } else {
            yearLabel.setText("2024");
        }

        languageLabel.setText("EN");
        durationLabel.setText("⏱ " + movie.getRuntime());
        if (isAddNewMode) {
            statusLabel.setText("Released");
        } else {
            statusLabel.setText("Active Theater Showing");
        }
        overviewLabel.setText(movie.getDescription());

        genresBox.getChildren().clear();
        if (movie.getGenre() != null) {
            Label gLabel = new Label(movie.getGenre());
            gLabel.getStyleClass().add("genre-badge");
            genresBox.getChildren().add(gLabel);
        }

        if (movie.getBannerPath() != null && !movie.getBannerPath().isEmpty()) {
            String bannerUrl = TMDBUtils.resolveMovieImagePath(movie.getBannerPath());
            try {
                heroBanner.setStyle("-fx-background-image: url('" + bannerUrl + "'); -fx-background-size: cover; -fx-background-position: center;");
            } catch (Exception e) {
                System.err.println("Failed to set hero banner background: " + e.getMessage());
            }
        }

        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            String posterUrl = TMDBUtils.resolveMovieImagePath(movie.getPosterPath());
            try {
                posterImage.setImage(new Image(posterUrl, true));
            } catch (Exception e) {
                System.err.println("Failed to load poster image: " + e.getMessage());
            }
        }
    }

    /**
     * Updates action button label and click handlers depending on active mode.
     */
    private void updateActionButtons() {
        if (actionButton != null) {
            actionButton.getStyleClass().clear();
            actionButton.getStyleClass().add("primary-action-btn");
            
            actionButton.setStyle("");
            actionButton.setDisable(false);

            if (isAddNewMode) {
                if (currentFetchedDto != null && movieDAO.isMovieExistsByTmdbId(currentFetchedDto.id)) {
                    actionButton.setText("Already Added");
                    actionButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                    actionButton.setDisable(true);
                } else {
                    actionButton.setText("+ Add to Theater");
                    actionButton.setOnAction(e -> handleAddToTheater());
                }
            } else if (isAdminMode) {
                actionButton.setText("Edit Dates & Pricing");
                actionButton.setOnAction(e -> handleEditPricing());
            } else {
                actionButton.setText("+ Book Tickets");
                actionButton.setOnAction(e -> handleBookTickets());
            }
        }
    }

    /**
     * Opens pricing edit dialog for adding new movie to theater.
     */
    private void handleAddToTheater() {
        if (currentFetchedDto == null) {
            javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Movie details not fully loaded yet.");
            a.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/EditMoviePricingDialog.fxml"));
            Parent root = loader.load();
            EditMoviePricingDialogController controller = loader.getController();
            
            controller.initDataForNewTMDB(currentFetchedDto);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Edit Dates & Pricing");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();

            if (controller.saveSuccessful) {
                if (controllers.MainLayoutController.getInstance() != null) {
                    controllers.MainLayoutController.getInstance().loadPageDirectly("/views/admin/MovieManagement.fxml");
                }
            }
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Populates UI controls with remote TMDB movie data DTO.
     * @param movie Remote MovieDTO
     */
    private void populateDetails(MovieDTO movie) {
        if (movie == null) {
            titleLabel.setText("Error loading details");
            return;
        }

        titleLabel.setText(movie.title);
        taglineLabel.setText(movie.tagline != null && !movie.tagline.isEmpty() ? "\"" + movie.tagline + "\"" : "");

        if (localMovie != null && localMovie.getRating() > 0) {
            ratingLabel.setText(String.format("⭐ %.1f", localMovie.getRating()));
        } else {
            ratingLabel.setText(String.format("⭐ %.1f", movie.vote_average));
        }

        if (movie.release_date != null && movie.release_date.length() >= 4) {
            yearLabel.setText(movie.release_date.substring(0, 4));
        } else if (localMovie != null && localMovie.getReleaseDate() != null && localMovie.getReleaseDate().length() >= 4) {
            yearLabel.setText(localMovie.getReleaseDate().substring(0, 4));
        }

        languageLabel.setText(movie.original_language != null ? movie.original_language.toUpperCase() : "EN");
        durationLabel.setText("⏱ " + movie.runtime + " mins");
        
        if (isAddNewMode) {
            statusLabel.setText("Released");
        } else {
            statusLabel.setText("Active Theater Showing");
        }
        
        overviewLabel.setText(movie.overview != null && !movie.overview.isEmpty() ? movie.overview : (localMovie != null ? localMovie.getDescription() : ""));

        genresBox.getChildren().clear();
        if (movie.genres != null) {
            for (MovieDTO.GenreDTO genre : movie.genres) {
                Label gLabel = new Label(genre.name);
                gLabel.getStyleClass().add("genre-badge");
                genresBox.getChildren().add(gLabel);
            }
        } else if (localMovie != null && localMovie.getGenre() != null) {
            Label gLabel = new Label(localMovie.getGenre());
            gLabel.getStyleClass().add("genre-badge");
            genresBox.getChildren().add(gLabel);
        }

        // Prefer local image paths
        String localBanner = localMovie != null ? localMovie.getBannerPath() : null;
        if (localBanner != null && !localBanner.isEmpty()) {
            String bannerUrl = TMDBUtils.resolveMovieImagePath(localBanner);
            try {
                heroBanner.setStyle("-fx-background-image: url('" + bannerUrl + "'); -fx-background-size: cover; -fx-background-position: center 25%;");
            } catch (Exception e) {
                System.err.println("Failed to set hero banner background: " + e.getMessage());
            }
        } else if (movie.backdrop_path != null) {
            String imageUrl = movie.backdrop_path.startsWith("http") ? movie.backdrop_path : TMDBUtils.getImageUrl(movie.backdrop_path, "w1280");
            try {
                heroBanner.setStyle("-fx-background-image: url('" + imageUrl + "'); "
                        + "-fx-background-size: cover; "
                        + "-fx-background-position: center 25%;");
            } catch (Exception e) {
                System.err.println("Failed to set hero banner background: " + e.getMessage());
            }
        }

        String localPoster = localMovie != null ? localMovie.getPosterPath() : null;
        if (localPoster != null && !localPoster.isEmpty()) {
            String posterUrl = TMDBUtils.resolveMovieImagePath(localPoster);
            try {
                posterImage.setImage(new Image(posterUrl, true));
            } catch (Exception e) {
                System.err.println("Failed to load poster image: " + e.getMessage());
            }
        } else if (movie.poster_path != null) {
            String posterUrl = movie.poster_path.startsWith("http") ? movie.poster_path : TMDBUtils.getImageUrl(movie.poster_path, "w500");
            try {
                Image poster = new Image(posterUrl, true);
                posterImage.setImage(poster);
            } catch (Exception e) {
                System.err.println("Failed to load poster image: " + e.getMessage());
            }
        }
    }

    /**
     * Displays edit pricing dialog for existing theater movie.
     */
    public void handleEditPricing() {
        if (localMovie != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/EditMoviePricingDialog.fxml"));
                Parent root = loader.load();

                EditMoviePricingDialogController controller = loader.getController();
                controller.initData(localMovie);

                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("Edit Dates & Pricing");
                stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                stage.setScene(new javafx.scene.Scene(root));
                stage.showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Navigates to ticket booking view.
     */
    @FXML
    public void handleBookTickets() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ticket/BookingTicket.fxml"));
            Parent root = loader.load();
            
            BookingTicketController controller = loader.getController();
            if (localMovie != null) {
                controller.setPreselectedMovie(localMovie);
            }
            
            if (controllers.MainLayoutController.getInstance() != null) {
                controllers.MainLayoutController.getInstance().selectNavButtonByTitle("Booking Ticket");
                controllers.MainLayoutController.getInstance().loadPageDirectly(root);
            } else {
                StackPane contentArea = (StackPane) titleLabel.getScene().lookup("#contentArea");
                if (contentArea != null) {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(root);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Navigates back to parent list view based on current mode.
     */
    @FXML
    public void handleBack() {
        try {
            String viewPath = "/views/ticket/NowShowing.fxml";
            if (isAddNewMode) {
                viewPath = "/views/admin/TMDBSearch.fxml";
            } else if (isAdminMode) {
                viewPath = "/views/admin/MovieManagement.fxml";
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
            Parent root = loader.load();
            StackPane contentArea = (StackPane) titleLabel.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

