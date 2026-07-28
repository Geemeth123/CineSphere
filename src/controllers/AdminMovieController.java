package controllers;

import java.awt.Frame;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import models.Movie;
import models.TMDBMovie;
import utils.TMDBClient;
import views.AdminView;
import views.components.AddMovieDialog;
import views.components.LoadingDialog;
import views.components.MovieManagementPanel;

/**
 * Dedicated controller for all movie management operations.
 * Handles TMDB search, result display, and database insertion.
 * Kept separate from AdminController to maintain single responsibility.
 */
public class AdminMovieController {

    private final AdminView view;
    private final MovieManagementPanel panel;

    // Holds the current search results so we can reference by row index
    private List<TMDBMovie> currentResults;

    public AdminMovieController(AdminView view) {
        this.view = view;
        this.panel = view.getMovieManagementPanel();
        initializeListeners();
    }

    private void initializeListeners() {
        // Search button click
        panel.getSearchButton().addActionListener(e -> handleSearch());

        // Allow pressing Enter in the search field to trigger search
        panel.getSearchField().addActionListener(e -> handleSearch());

        // Table row selection → show details card
        panel.getResultsTable().getSelectionModel().addListSelectionListener(
            (ListSelectionEvent e) -> {
                if (!e.getValueIsAdjusting()) {
                    handleRowSelection();
                }
            }
        );

        // Add to Theater button
        panel.getAddToTheaterButton().addActionListener(e -> handleAddToTheater());

        // Clear selection button
        panel.getClearSelectionButton().addActionListener(e -> panel.clearDetails());
    }

    // ==========================================
    // Search Logic
    // ==========================================

    private void handleSearch() {
        String query = panel.getSearchField().getText().trim();

        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(view.getFrame(),
                "Please enter a movie name to search.",
                "Search Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!TMDBClient.isApiKeyConfigured()) {
            JOptionPane.showMessageDialog(view.getFrame(),
                "TMDB API key is not configured.\nEnsure your .env file exists with TMDB_API_KEY set.",
                "API Key Missing", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Disable search button to prevent double-clicks
        panel.getSearchButton().setEnabled(false);
        panel.clearResults();

        AtomicReference<List<TMDBMovie>> searchResultsRef = new AtomicReference<>();

        LoadingDialog.runWithLoading(
            (Frame) view.getFrame(),
            "Searching TMDB for \"" + query + "\"...",
            () -> {
                // Background thread
                searchResultsRef.set(TMDBClient.searchMovies(query));
            },
            () -> {
                // EDT callback after background completes
                panel.getSearchButton().setEnabled(true);
                currentResults = searchResultsRef.get();
                populateResultsTable(currentResults, query);
            }
        );
    }

    private void populateResultsTable(List<TMDBMovie> movies, String query) {
        panel.getTableModel().setRowCount(0);

        if (movies == null || movies.isEmpty()) {
            JOptionPane.showMessageDialog(view.getFrame(),
                "No movies found for \"" + query + "\".\nTry a different search term.",
                "No Results", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        for (TMDBMovie movie : movies) {
            String genre = TMDBClient.getGenreNames(movie.getGenreIds());
            String rating = String.format("%.1f", movie.getVoteAverage());
            String desc = movie.getOverview();
            if (desc.length() > 100) {
                desc = desc.substring(0, 97) + "...";
            }
            panel.getTableModel().addRow(new Object[]{
                movie.getTitle(),
                rating,
                movie.getReleaseDate(),
                genre,
                desc
            });
        }
    }

    // ==========================================
    // Selection Logic
    // ==========================================

    private void handleRowSelection() {
        int selectedRow = panel.getResultsTable().getSelectedRow();
        if (selectedRow == -1 || currentResults == null || selectedRow >= currentResults.size()) {
            return;
        }

        TMDBMovie selected = currentResults.get(selectedRow);
        String genre = TMDBClient.getGenreNames(selected.getGenreIds());

        panel.showMovieDetails(
            selected.getTitle(),
            selected.getOverview().isEmpty() ? "No description available." : selected.getOverview(),
            String.format("%.1f", selected.getVoteAverage()),
            selected.getReleaseDate().isEmpty() ? "N/A" : selected.getReleaseDate(),
            genre
        );
    }

    // ==========================================
    // Add to Theater Logic
    // ==========================================

    private void handleAddToTheater() {
        int selectedRow = panel.getResultsTable().getSelectedRow();

        if (selectedRow == -1 || currentResults == null || selectedRow >= currentResults.size()) {
            JOptionPane.showMessageDialog(view.getFrame(),
                "Please select a movie from the search results first.",
                "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TMDBMovie selected = currentResults.get(selectedRow);

        // Check for duplicate TMDB ID in DB
        boolean alreadyExists = Movie.existsByTmdbId(selected.getId());
        if (alreadyExists) {
            JOptionPane.showMessageDialog(view.getFrame(),
                "\"" + selected.getTitle() + "\" is already in your theater catalog.",
                "Duplicate Movie", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Open the details dialog to collect pricing/duration
        AddMovieDialog dialog = new AddMovieDialog(view.getFrame(), selected);
        dialog.setVisible(true);

        Movie movieToInsert = dialog.getResult();
        if (movieToInsert == null) {
            // Admin cancelled
            return;
        }

        // Insert into DB on background thread with loading indicator
        final boolean[] success = {false};

        LoadingDialog.runWithLoading(
            (Frame) view.getFrame(),
            "Adding \"" + movieToInsert.getTitle() + "\" to theater...",
            () -> {
                // Background thread
                success[0] = Movie.insert(movieToInsert);
            },
            () -> {
                // EDT callback
                if (success[0]) {
                    JOptionPane.showMessageDialog(view.getFrame(),
                        "\"" + movieToInsert.getTitle() + "\" has been added to your theater catalog!",
                        "Movie Added", JOptionPane.INFORMATION_MESSAGE);
                    panel.clearDetails();
                } else {
                    JOptionPane.showMessageDialog(view.getFrame(),
                        "Failed to add the movie to the database.\nPlease check your connection and try again.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        );
    }
}
