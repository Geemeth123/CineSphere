package controllers.ticket;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.Movie;
import models.Showtime;
import models.ShowDAO;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 * Controller for ticket booking flow, handling active movie listing,
 * search filtering, showtime selection, and seat map navigation.
 */
public class BookingTicketController {

    @FXML private TextField searchField;
    @FXML private ListView<Movie> movieListView;
    @FXML private VBox emptyStatePane;
    @FXML private BorderPane detailsPane;
    
    @FXML private Label movieTitleLabel;
    @FXML private Label movieMetaLabel;
    @FXML private Label movieDescLabel;
    @FXML private VBox timeSlotsPane;
    @FXML private Button selectSeatsBtn;

    private Showtime selectedShowtime = null;
    private Movie preselectedMovie = null;

    public void setPreselectedMovie(Movie movie) {
        this.preselectedMovie = movie;
    }

    /**
     * Initializes the booking ticket view and loads active movies asynchronously.
     */
    @FXML
    public void initialize() {
        // Load live movies from DB in background thread to keep UI responsive
        new Thread(() -> {
            ShowDAO dao = new ShowDAO();
            java.util.List<Movie> activeMovies = dao.getActiveMoviesWithShowtimes();
            
            Platform.runLater(() -> {
                ObservableList<Movie> movies = FXCollections.observableArrayList(activeMovies);
                FilteredList<Movie> filteredData = new FilteredList<>(movies, p -> true);
                
                if (searchField != null) {
                    searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                        filteredData.setPredicate(movie -> {
                            if (newValue == null || newValue.isEmpty()) return true;
                            String lowerCaseFilter = newValue.toLowerCase();
                            return movie.getTitle().toLowerCase().contains(lowerCaseFilter) ||
                                   movie.getGenre().toLowerCase().contains(lowerCaseFilter);
                        });
                    });
                }
                movieListView.setItems(filteredData);

                if (preselectedMovie != null) {
                    for (Movie m : filteredData) {
                        if (m.getId().equals(preselectedMovie.getId())) {
                            movieListView.getSelectionModel().select(m);
                            break;
                        }
                    }
                }
            });
        }).start();

        // Custom ListCell for Movies
        movieListView.setCellFactory(lv -> new ListCell<Movie>() {
            @Override
            protected void updateItem(Movie movie, boolean empty) {
                super.updateItem(movie, empty);
                if (empty || movie == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                } else {
                    HBox box = new HBox(15);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    box.setPadding(new Insets(20, 20, 20, 20));
                    
                    Label iconLabel = new Label("🎬");
                    iconLabel.setStyle("-fx-font-size: 24px;");
                    
                    VBox textContainer = new VBox(5);
                    Label titleLabel = new Label(movie.getTitle());
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #212529;");
                    
                    Label genreLabel = new Label(movie.getGenre());
                    genreLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #adb5bd;");
                    textContainer.getChildren().addAll(titleLabel, genreLabel);
                    
                    box.getChildren().addAll(iconLabel, textContainer);
                    
                    StackPane wrapper = new StackPane(box);
                    wrapper.setPadding(new Insets(0, 0, 15, 0));
                    
                    if (isSelected()) {
                        box.getStyleClass().setAll("movie-list-item-selected");
                    } else {
                        box.getStyleClass().setAll("movie-list-item");
                    }
                    setDisable(false);
                    
                    setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                    setGraphic(wrapper);
                }
            }
        });

        // Handle selection cleanly
        movieListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showMovieDetails(newVal);
            }
        });
    }

    /**
     * Displays details and available showtimes for selected movie.
     * @param movie selected Movie instance
     */
    private void showMovieDetails(Movie movie) {
        emptyStatePane.setVisible(false);
        emptyStatePane.setManaged(false);
        detailsPane.setVisible(true);
        detailsPane.setManaged(true);

        movieTitleLabel.setText(movie.getTitle());
        movieMetaLabel.setText(movie.getGenre() + " • " + movie.getRuntime());
        movieDescLabel.setText(movie.getDescription());

        selectedShowtime = null;
        updateSelectSeatsButton();
        timeSlotsPane.getChildren().clear();

        // Group showtimes by date
        java.util.Map<String, java.util.List<Showtime>> groupedShowtimes = new java.util.LinkedHashMap<>();
        for (Showtime slot : movie.getShowtimes()) {
            String date = slot.getRawDate() != null ? slot.getRawDate() : "Today";
            groupedShowtimes.computeIfAbsent(date, k -> new java.util.ArrayList<>()).add(slot);
        }

        for (java.util.Map.Entry<String, java.util.List<Showtime>> entry : groupedShowtimes.entrySet()) {
            VBox dateGroup = new VBox(8);
            dateGroup.setPadding(new Insets(5, 0, 10, 0));

            Label dateLbl = new Label(entry.getKey());
            dateLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
            
            FlowPane slotsFlow = new FlowPane();
            slotsFlow.setHgap(15);
            slotsFlow.setVgap(15);

            for (Showtime slot : entry.getValue()) {
                Button slotBtn = new Button();
                slotBtn.setPrefWidth(140);
                slotBtn.setMinHeight(50);
                
                VBox btnContent = new VBox(2);
                btnContent.setAlignment(javafx.geometry.Pos.CENTER);
                
                Label timeHallLbl = new Label(slot.getTime() + " - " + slot.getHall());
                Label seatsLbl;
                
                if (slot.getAvailableSeats() <= 0) {
                    timeHallLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #991b1b;");
                    seatsLbl = new Label("Sold Out");
                    seatsLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    
                    slotBtn.setDisable(true);
                    slotBtn.setStyle("-fx-background-color: #fee2e2; -fx-border-color: #fca5a5; -fx-border-radius: 6; -fx-background-radius: 6; -fx-opacity: 0.95;");
                } else {
                    timeHallLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1e293b;");
                    seatsLbl = new Label(slot.getAvailableSeats() + " left");
                    seatsLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #0ea5e9; -fx-font-weight: bold;");
                    
                    slotBtn.getStyleClass().add("time-slot-btn");
                    slotBtn.setOnAction(e -> {
                        selectedShowtime = slot;
                        // Clear active style from all other buttons in all date groups
                        for (javafx.scene.Node groupNode : timeSlotsPane.getChildren()) {
                            if (groupNode instanceof VBox) {
                                for (javafx.scene.Node groupChild : ((VBox) groupNode).getChildren()) {
                                    if (groupChild instanceof FlowPane) {
                                        for (javafx.scene.Node btnNode : ((FlowPane) groupChild).getChildren()) {
                                            if (btnNode instanceof Button) {
                                                Button otherBtn = (Button) btnNode;
                                                otherBtn.getStyleClass().remove("time-slot-btn-active");
                                                if (!otherBtn.getStyleClass().contains("time-slot-btn") && !otherBtn.isDisable()) {
                                                    otherBtn.getStyleClass().add("time-slot-btn");
                                                }
                                                // Reset text fills for normal slot buttons
                                                if (!otherBtn.isDisable() && otherBtn.getGraphic() instanceof VBox) {
                                                    VBox otherContent = (VBox) otherBtn.getGraphic();
                                                    if (otherContent.getChildren().size() >= 2) {
                                                        otherContent.getChildren().get(0).setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1e293b;");
                                                        otherContent.getChildren().get(1).setStyle("-fx-font-size: 10px; -fx-text-fill: #0ea5e9; -fx-font-weight: bold;");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        slotBtn.getStyleClass().remove("time-slot-btn");
                        slotBtn.getStyleClass().add("time-slot-btn-active");
                        // Active colors for selected button
                        timeHallLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #ffffff;");
                        seatsLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #e0f2fe; -fx-font-weight: bold;");
                        updateSelectSeatsButton();
                    });
                }
                
                btnContent.getChildren().addAll(timeHallLbl, seatsLbl);
                slotBtn.setGraphic(btnContent);
                slotsFlow.getChildren().add(slotBtn);
            }
            dateGroup.getChildren().addAll(dateLbl, slotsFlow);
            timeSlotsPane.getChildren().add(dateGroup);
        }
    }

    /**
     * Updates the enable status and styling of the seat selection button.
     */
    private void updateSelectSeatsButton() {
        if (selectedShowtime != null) {
            selectSeatsBtn.setDisable(false);
            selectSeatsBtn.getStyleClass().remove("primary-action-btn-disabled");
            if (!selectSeatsBtn.getStyleClass().contains("primary-action-btn")) {
                selectSeatsBtn.getStyleClass().add("primary-action-btn");
            }
        } else {
            selectSeatsBtn.setDisable(true);
            selectSeatsBtn.getStyleClass().remove("primary-action-btn");
            if (!selectSeatsBtn.getStyleClass().contains("primary-action-btn-disabled")) {
                selectSeatsBtn.getStyleClass().add("primary-action-btn-disabled");
            }
        }
    }

    /**
     * Handles navigating to seat selection view for chosen showtime.
     */
    @FXML
    public void handleSelectSeats() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ticket/SeatSelection.fxml"));
            Parent root = loader.load();
            
            SeatSelectionController controller = loader.getController();
            controller.setBookingData(selectedShowtime.getId(), movieTitleLabel.getText(), selectedShowtime.getTime() + " - " + selectedShowtime.getHall());
            
            StackPane contentArea = (StackPane) selectSeatsBtn.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

