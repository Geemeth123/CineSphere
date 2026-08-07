/**
 * Schedule Movie Controller (Scheduler User Role)
 * 
 * Responsibility:
 * 1. Displays licensed movie details (Title, Genre, Poster, Licensed Start/End Dates).
 * 2. Allows selecting active cinema halls, adding multiple target show dates and showtimes (HH:mm).
 * 3. Validates showtimes for internal time window overlaps and database hall conflicts before saving.
 */
package controllers.scheduler;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import controllers.MainLayoutController;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import models.Hall;
import models.HallDAO;
import models.Movie;
import models.ShowDAO;

public class ScheduleMovieController implements Initializable {

    // UI Movie Info Controls
    @FXML private ImageView moviePoster;
    @FXML private Label movieTitle;
    @FXML private Label movieGenre;
    @FXML private Label movieDuration;
    @FXML private Label movieLicensedPeriod;

    // Form inputs for Hall, Date, and Time
    @FXML private ComboBox<Hall> hallComboBox;
    @FXML private DatePicker dateField;
    @FXML private TextField timeField;
    @FXML private FlowPane datesGrid;
    @FXML private FlowPane timesGrid;
    @FXML private Label errorLabel;

    private Movie currentMovie;
    private HallDAO hallDAO = new HallDAO();
    private ShowDAO showDAO = new ShowDAO();
    
    // Lists holding selected dates and showtime strings before batch saving
    private List<LocalDate> addedDates = new ArrayList<>();
    private List<String> addedTimes = new ArrayList<>();
    private String previousPage = "/views/scheduler/ShowScheduling.fxml";

    public void setPreviousPage(String fxmlPath) {
        this.previousPage = fxmlPath;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load only ACTIVE (available) halls into the selection dropdown
        List<Hall> halls = hallDAO.getAllHalls();
        List<Hall> activeHalls = new ArrayList<>();
        for (Hall h : halls) {
            if ("ACTIVE".equalsIgnoreCase(h.getStatus())) {
                activeHalls.add(h);
            }
        }
        hallComboBox.setItems(FXCollections.observableArrayList(activeHalls));
    }

    /**
     * Initializes the form with selected Movie metadata and populates existing showtimes.
     */
    public void setMovie(Movie movie) {
        this.currentMovie = movie;
        movieTitle.setText(movie.getTitle());
        movieGenre.setText(movie.getGenre());
        movieDuration.setText(movie.getRuntime());
        movieLicensedPeriod.setText("Licensed: " + movie.getShowingFrom() + " to " + movie.getShowingUntil());

        // Load poster image from local file or TMDB URL
        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            try {
                String posterUrl = (movie.getPosterPath().startsWith("http") || movie.getPosterPath().startsWith("file:")) ? movie.getPosterPath() : utils.TMDBUtils.getImageUrl(movie.getPosterPath(), "w500");
                Image image = new Image(posterUrl, true);
                moviePoster.setImage(image);
            } catch (Exception e) {
                System.out.println("Could not load image: " + movie.getPosterPath());
            }
        }

        // Fetch pre-existing showtimes for this movie from DB and populate UI pills
        if (movie.getId() != null && !movie.getId().equals("-1")) {
            try {
                int movieId = Integer.parseInt(movie.getId().replace("M", ""));
                List<models.Showtime> existingShows = showDAO.getShowsForMovie(movieId);
                if (existingShows != null) {
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    for (models.Showtime st : existingShows) {
                        try {
                            LocalDate d = LocalDate.parse(st.getRawDate(), df);
                            if (!addedDates.contains(d)) {
                                addedDates.add(d);
                            }
                            String t = st.getRawTime();
                            if (!addedTimes.contains(t)) {
                                addedTimes.add(t);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    renderDates();
                    renderTimes();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Validates and adds a target show date.
     * Ensures date falls within movie's licensing start/end period.
     */
    @FXML
    public void handleAddDate(ActionEvent event) {
        LocalDate selectedDate = dateField.getValue();
        if (selectedDate == null) return;
        
        try {
            LocalDate validStart = LocalDate.MIN;
            LocalDate validEnd = LocalDate.MAX;
            
            try {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                if (currentMovie.getShowingFrom() != null && !currentMovie.getShowingFrom().trim().isEmpty()) {
                    validStart = LocalDate.parse(currentMovie.getShowingFrom(), df);
                }
                if (currentMovie.getShowingUntil() != null && !currentMovie.getShowingUntil().trim().isEmpty()) {
                    validEnd = LocalDate.parse(currentMovie.getShowingUntil(), df);
                }
            } catch (Exception parseEx) {
                System.out.println("Warning: DB License dates could not be parsed. Proceeding without strict validation.");
            }
            
            // Check if selected date is outside licensed window
            if (selectedDate.isBefore(validStart) || selectedDate.isAfter(validEnd)) {
                showError("Date must be within the licensed period (" + currentMovie.getShowingFrom() + " to " + currentMovie.getShowingUntil() + ").");
                return;
            }
            
            if (addedDates.contains(selectedDate)) {
                showError("Date already added.");
                return;
            }

            hideError();
            addedDates.add(selectedDate);
            dateField.setValue(null);
            renderDates();
        } catch (Exception e) {
            e.printStackTrace();
            showError("An unexpected error occurred.");
        }
    }
    
    /**
     * Renders removable green pills for added show dates.
     */
    private void renderDates() {
        datesGrid.getChildren().clear();
        for (LocalDate d : addedDates) {
            HBox pill = new HBox(5);
            pill.setAlignment(Pos.CENTER);
            pill.setStyle("-fx-background-color: #dcfce7; -fx-border-color: #bbf7d0; -fx-border-radius: 16; -fx-background-radius: 16; -fx-padding: 6 12;");
            
            Label dLbl = new Label(d.toString());
            dLbl.setStyle("-fx-text-fill: #166534; -fx-font-weight: bold;");
            
            Button removeBtn = new Button("\u00D7");
            removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #dc2626; -fx-padding: 0; -fx-font-size: 14px; -fx-cursor: hand;");
            removeBtn.setOnAction(e -> {
                addedDates.remove(d);
                renderDates();
            });
            
            pill.getChildren().addAll(dLbl, removeBtn);
            datesGrid.getChildren().add(pill);
        }
    }

    /**
     * Validates HH:mm time input and adds to showtimes list.
     */
    @FXML
    public void handleAddTime(ActionEvent event) {
        String timeStr = timeField.getText();
        if (timeStr == null || timeStr.trim().isEmpty()) return;

        // Verify HH:mm format (e.g. 14:30)
        if (!timeStr.matches("([01]?[0-9]|2[0-3]):[0-5][0-9]")) {
            showError("Time must be in HH:mm format.");
            return;
        }
        
        String[] parts = timeStr.split(":");
        if (parts[0].length() == 1) {
            timeStr = "0" + timeStr;
        }
        
        if (addedTimes.contains(timeStr)) {
            showError("Time already added.");
            return;
        }

        hideError();
        addedTimes.add(timeStr);
        timeField.clear();
        renderTimes();
    }

    /**
     * Renders removable blue pills for added showtimes.
     */
    private void renderTimes() {
        timesGrid.getChildren().clear();
        for (String t : addedTimes) {
            HBox pill = new HBox(5);
            pill.setAlignment(Pos.CENTER);
            pill.setStyle("-fx-background-color: #dbeafe; -fx-border-color: #bfdbfe; -fx-border-radius: 16; -fx-background-radius: 16; -fx-padding: 6 12;");
            
            Label timeLbl = new Label(t);
            timeLbl.setStyle("-fx-text-fill: #1e3a8a; -fx-font-weight: bold;");
            
            Button removeBtn = new Button("\u00D7");
            removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-padding: 0; -fx-font-size: 14px; -fx-cursor: hand;");
            removeBtn.setOnAction(e -> {
                addedTimes.remove(t);
                renderTimes();
            });
            
            pill.getChildren().addAll(timeLbl, removeBtn);
            timesGrid.getChildren().add(pill);
        }
    }

    /**
     * Validates hall availability and generates show records across all selected dates & times.
     * Checks:
     * 1. Self-overlap between added times (accounting for movie runtime length).
     * 2. Hall occupation conflicts against existing database shows via ShowDAO.
     */
    @FXML
    public void handleGenerateSchedule(ActionEvent event) {
        Hall hall = hallComboBox.getValue();

        if (hall == null) {
            showError("Please select a hall.");
            return;
        }
        
        if (addedDates.isEmpty()) {
            showError("Please add at least one date.");
            return;
        }
        
        if (addedTimes.isEmpty()) {
            showError("Please add at least one showtime.");
            return;
        }

        // Extract movie runtime in minutes
        int runtimeMins = 120;
        try {
            runtimeMins = Integer.parseInt(currentMovie.getRuntime().replace(" mins", "").trim());
        } catch (Exception e) {}

        // Check for internal time window overlaps among added showtimes
        for (LocalDate d : addedDates) {
            for (int i = 0; i < addedTimes.size(); i++) {
                for (int j = i + 1; j < addedTimes.size(); j++) {
                    LocalTime start1 = LocalTime.parse(addedTimes.get(i));
                    LocalTime end1 = start1.plusMinutes(runtimeMins);
                    LocalTime start2 = LocalTime.parse(addedTimes.get(j));
                    LocalTime end2 = start2.plusMinutes(runtimeMins);

                    if ((start1.isBefore(end2) && end1.isAfter(start2))) {
                        showError("Time Overlap Detected: Shows at " + addedTimes.get(i) + " and " + addedTimes.get(j) + " overlap on " + d.toString() + ". Please adjust the times.");
                        return;
                    }
                }
            }
        }

        // Conflict Detection: Query DB to check if selected hall is already occupied at specified times
        int movieId = Integer.parseInt(currentMovie.getId().replace("M", ""));
        for (LocalDate d : addedDates) {
            for (String t : addedTimes) {
                LocalTime start = LocalTime.parse(t);
                LocalTime end = start.plusMinutes(runtimeMins);

                if (showDAO.isHallOccupied(hall.getId(), d.toString(), start.toString(), end.toString(), movieId)) {
                    showError("Conflict Detected: Hall " + hall.getName() + " is already occupied on " + d.toString() + " between " + start.toString() + " and " + end.toString() + ". Please select different times or another hall.");
                    return;
                }
            }
        }

        hideError();
        
        // Persist batch show schedule entries into database
        if (showDAO.addBatchShowsSpecificDates(movieId, hall.getId(), addedDates, addedTimes)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Shows generated successfully!");
            alert.showAndWait();
            handleBack(null);
        } else {
            showError("Failed to generate shows. Please check database connection.");
        }
    }

    @FXML
    public void handleBack(ActionEvent event) {
        MainLayoutController.getInstance().loadPageDirectly(previousPage);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}

