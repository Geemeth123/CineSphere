package views.components;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import utils.ThemeManager;

/**
 * Panel for managing movies and searching TMDB.
 * Styled to match TicketCancellationPanel for visual consistency.
 */
public class MovieManagementPanel extends JPanel {

    private final JTextField searchField;
    private final JButton searchButton;

    private final RoundedPanel detailsCard;
    private final JLabel detailsTitleLabel;
    private final JLabel detailsDescLabel;
    private final JLabel detailsRatingLabel;
    private final JLabel detailsReleaseDateLabel;
    private final JLabel detailsGenreLabel;

    private final JButton addToTheaterButton;
    private final JButton clearSelectionButton;

    private final JTable resultsTable;
    private final DefaultTableModel tableModel;

    public MovieManagementPanel() {
        setLayout(new BorderLayout(0, 25));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(40, 50, 40, 50));

        // ==========================================
        // NORTH — Header & Search Bar
        // ==========================================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Movie Management");
        titleLabel.setFont(ThemeManager.FONT_TITLE);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Search TMDB for movies and add them to your theater catalog.");
        subtitleLabel.setFont(ThemeManager.FONT_BODY);
        subtitleLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        subtitleLabel.setBorder(new EmptyBorder(5, 0, 0, 0));

        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchBarPanel.setBackground(Color.WHITE);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(280, 45));
        searchField.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchField.putClientProperty("JTextField.placeholderText", "Search movies on TMDB...");
        searchField.putClientProperty("JComponent.roundRect", true);

        searchButton = ThemeManager.createPrimaryButton("Search");
        searchButton.setPreferredSize(new Dimension(140, 45));
        searchButton.putClientProperty("JButton.buttonType", "roundRect");

        searchBarPanel.add(searchField);
        searchBarPanel.add(searchButton);
        headerPanel.add(searchBarPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // ==========================================
        // CENTER — Details Card + Results Table
        // ==========================================
        JPanel centerPanel = new JPanel(new BorderLayout(0, 30));
        centerPanel.setBackground(Color.WHITE);

        // --- Details Card (RoundedPanel, same as TicketCancellationPanel) ---
        detailsCard = new RoundedPanel(16, ThemeManager.BORDER);
        detailsCard.setLayout(new BorderLayout());
        detailsCard.setBackground(ThemeManager.SURFACE);
        detailsCard.setBorder(new EmptyBorder(25, 30, 25, 30));
        detailsCard.setVisible(false);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        detailsTitleLabel = new JLabel("Title: -");
        detailsTitleLabel.setFont(ThemeManager.FONT_HEADING);
        detailsTitleLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        detailsDescLabel = new JLabel("Description: -");
        detailsDescLabel.setFont(ThemeManager.FONT_BODY);
        detailsDescLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        detailsDescLabel.setBorder(new EmptyBorder(8, 0, 4, 0));

        detailsRatingLabel = new JLabel("Rating: -");
        detailsRatingLabel.setFont(ThemeManager.FONT_BODY);
        detailsRatingLabel.setForeground(ThemeManager.TEXT_SECONDARY);

        detailsReleaseDateLabel = new JLabel("Release Date: -");
        detailsReleaseDateLabel.setFont(ThemeManager.FONT_BODY);
        detailsReleaseDateLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        detailsReleaseDateLabel.setBorder(new EmptyBorder(4, 0, 0, 0));

        detailsGenreLabel = new JLabel("Genre: -");
        detailsGenreLabel.setFont(ThemeManager.FONT_BODY);
        detailsGenreLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        detailsGenreLabel.setBorder(new EmptyBorder(4, 0, 0, 0));

        infoPanel.add(detailsTitleLabel);
        infoPanel.add(detailsDescLabel);
        infoPanel.add(detailsRatingLabel);
        infoPanel.add(detailsReleaseDateLabel);
        infoPanel.add(detailsGenreLabel);

        // Action buttons on the right side of the details card
        JPanel actionContainer = new JPanel(new BorderLayout());
        actionContainer.setOpaque(false);

        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actionButtonPanel.setOpaque(false);

        addToTheaterButton = ThemeManager.createPrimaryButton("Add to Theater");
        addToTheaterButton.setPreferredSize(new Dimension(160, 42));
        addToTheaterButton.putClientProperty("JButton.buttonType", "roundRect");

        clearSelectionButton = ThemeManager.createSecondaryButton("Clear");
        clearSelectionButton.setPreferredSize(new Dimension(100, 42));
        clearSelectionButton.putClientProperty("JButton.buttonType", "roundRect");

        actionButtonPanel.add(addToTheaterButton);
        actionButtonPanel.add(clearSelectionButton);

        actionContainer.add(actionButtonPanel, BorderLayout.SOUTH);

        detailsCard.add(infoPanel, BorderLayout.CENTER);
        detailsCard.add(actionContainer, BorderLayout.EAST);

        // --- Results Table ---
        JPanel tableContainer = new JPanel(new BorderLayout(0, 15));
        tableContainer.setBackground(Color.WHITE);

        JLabel tableTitle = new JLabel("Search Results");
        tableTitle.setFont(ThemeManager.FONT_SUBHEADING);
        tableTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        tableContainer.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"Title", "Rating", "Release Date", "Genre", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultsTable = new JTable(tableModel);
        styleResultsTable(resultsTable);

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER, 1));
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // Wrap details card so it sits at the top
        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setBackground(Color.WHITE);
        topWrapper.add(detailsCard, BorderLayout.NORTH);

        centerPanel.add(topWrapper, BorderLayout.NORTH);
        centerPanel.add(tableContainer, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Styles the results table — matches TicketCancellationPanel pattern exactly.
     */
    private void styleResultsTable(JTable table) {
        table.setRowHeight(45);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(ThemeManager.BORDER);
        table.setFont(ThemeManager.FONT_BODY);
        table.setSelectionBackground(new Color(240, 248, 255));
        table.setSelectionForeground(Color.BLACK);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(ThemeManager.TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(100, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.BORDER));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        // Column widths and alignment
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 0) {
                // Title — left aligned, wider
                table.getColumnModel().getColumn(i).setPreferredWidth(200);
            } else if (i == 1) {
                // Rating — centered, narrow
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
                table.getColumnModel().getColumn(i).setPreferredWidth(70);
                table.getColumnModel().getColumn(i).setMaxWidth(90);
            } else if (i == 2) {
                // Release Date — centered
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
                table.getColumnModel().getColumn(i).setPreferredWidth(110);
                table.getColumnModel().getColumn(i).setMaxWidth(130);
            } else if (i == 3) {
                // Genre — centered
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
                table.getColumnModel().getColumn(i).setPreferredWidth(150);
            } else if (i == 4) {
                // Description — left aligned, takes remaining space
                table.getColumnModel().getColumn(i).setPreferredWidth(350);
            }
        }
    }

    // ==========================================
    // Public Methods for Controller
    // ==========================================

    /**
     * Shows the details card with info for the selected movie.
     */
    public void showMovieDetails(String title, String description, String rating,
                                  String releaseDate, String genre) {
        detailsTitleLabel.setText(title);

        // Truncate description for display
        if (description.length() > 150) {
            description = description.substring(0, 147) + "...";
        }
        detailsDescLabel.setText(description);

        detailsRatingLabel.setText("⭐ " + rating + " / 10");
        detailsReleaseDateLabel.setText("📅 " + releaseDate);
        detailsGenreLabel.setText("🎬 " + genre);

        detailsCard.setVisible(true);
        revalidate();
        repaint();
    }

    /**
     * Hides the details card and clears the selection.
     */
    public void clearDetails() {
        detailsCard.setVisible(false);
        resultsTable.clearSelection();
        revalidate();
        repaint();
    }

    /**
     * Clears all rows from the results table.
     */
    public void clearResults() {
        tableModel.setRowCount(0);
        clearDetails();
    }

    // ==========================================
    // Expose Elements for Controller
    // ==========================================
    public JTextField getSearchField() { return searchField; }
    public JButton getSearchButton() { return searchButton; }
    public JButton getAddToTheaterButton() { return addToTheaterButton; }
    public JButton getClearSelectionButton() { return clearSelectionButton; }
    public JTable getResultsTable() { return resultsTable; }
    public DefaultTableModel getTableModel() { return tableModel; }
}
