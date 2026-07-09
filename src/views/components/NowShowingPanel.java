package views.components;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import utils.ThemeManager;

public class NowShowingPanel extends JPanel {

    private final JPanel gridPanel;
    private final JTextField searchField;
    private final JComboBox<String> genreFilter;

    // Callback for controller
    private Consumer<String[]> movieClickListener;

    public NowShowingPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(40, 50, 40, 50));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(0, 0, 30, 0));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Now Showing");
        titleLabel.setFont(ThemeManager.FONT_TITLE);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Browse currently active movies across all halls.");
        subtitleLabel.setFont(ThemeManager.FONT_BODY);
        subtitleLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        subtitleLabel.setBorder(new EmptyBorder(5, 0, 0, 0));

        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        JPanel controlsContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        controlsContainer.setBackground(Color.WHITE);

        genreFilter = new JComboBox<>(new String[]{"All Genres", "Action", "Animation", "Sci-Fi", "Comedy", "Thriller"});
        genreFilter.setPreferredSize(new Dimension(150, 42));
        genreFilter.setFont(ThemeManager.FONT_BODY);
        genreFilter.setBackground(Color.WHITE);
        genreFilter.putClientProperty("JComponent.roundRect", true);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(250, 42));
        searchField.setFont(ThemeManager.FONT_BODY);
        searchField.putClientProperty("JTextField.placeholderText", "Search movies...");
        searchField.putClientProperty("JComponent.roundRect", true);

        JButton searchButton = ThemeManager.createPrimaryButton("Search");
        searchButton.setPreferredSize(new Dimension(100, 42));
        searchButton.putClientProperty("JButton.buttonType", "roundRect");

        controlsContainer.add(genreFilter);
        controlsContainer.add(searchField);
        controlsContainer.add(searchButton);

        headerPanel.add(controlsContainer, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        gridPanel = new JPanel(new GridLayout(0, 3, 35, 35));
        gridPanel.setBackground(Color.WHITE);

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setBackground(Color.WHITE);
        gridWrapper.add(gridPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(gridWrapper);
        scrollPane.setBorder(null);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        add(scrollPane, BorderLayout.CENTER);

        loadDummyMovies();
    }

    public void setMovieClickListener(Consumer<String[]> listener) {
        this.movieClickListener = listener;
    }

    private void loadDummyMovies() {
        gridPanel.add(createMovieCard("Dune: Part Two", "Action / Sci-Fi", "166 mins", "8.8", "Paul Atreides unites with Chani and the Fremen while on a warpath of revenge against the conspirators who destroyed his family."));
        gridPanel.add(createMovieCard("Kung Fu Panda 4", "Animation", "94 mins", "7.5", "Po must train a new warrior when he's chosen to become the spiritual leader of the Valley of Peace."));
        gridPanel.add(createMovieCard("Godzilla x Kong", "Action / Sci-Fi", "115 mins", "6.7", "Two ancient titans, Godzilla and Kong, clash in an epic battle as humans unravel their intertwined origins."));
        gridPanel.add(createMovieCard("Civil War", "Thriller", "109 mins", "7.6", "A journey across a dystopian future America, following a team of military-embedded journalists."));
        gridPanel.add(createMovieCard("Ghostbusters", "Comedy / Sci-Fi", "115 mins", "6.4", "When the discovery of an ancient artifact unleashes an evil force, Ghostbusters new and old must join forces."));
        gridPanel.add(createMovieCard("Challengers", "Drama / Sport", "131 mins", "7.8", "Tashi, a tennis player turned coach, has transformed her husband into a world-famous grand slam champion."));
    }

    private JPanel createMovieCard(String title, String genre, String duration, String rating, String desc) {
        RoundedPanel card = new RoundedPanel(20, ThemeManager.BORDER);
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel topInfo = new JPanel(new BorderLayout());
        topInfo.setOpaque(false);

        JLabel genreLbl = new JLabel(genre.toUpperCase());
        genreLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        genreLbl.setForeground(ThemeManager.PRIMARY);

        JLabel ratingLbl = new JLabel("\u2605 " + rating);
        ratingLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ratingLbl.setForeground(new Color(230, 160, 15));

        topInfo.add(genreLbl, BorderLayout.WEST);
        topInfo.add(ratingLbl, BorderLayout.EAST);

        JPanel centerInfo = new JPanel();
        centerInfo.setLayout(new BoxLayout(centerInfo, BoxLayout.Y_AXIS));
        centerInfo.setOpaque(false);
        centerInfo.setBorder(new EmptyBorder(20, 0, 30, 0));

        JLabel titleLbl = new JLabel("<html><div style='width: 200px;'>" + title + "</div></html>");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLbl.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel durLbl = new JLabel("\u23F2 " + duration + "  •  Multiple Halls");
        durLbl.setFont(ThemeManager.FONT_BODY);
        durLbl.setForeground(ThemeManager.TEXT_SECONDARY);
        durLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        durLbl.setBorder(new EmptyBorder(10, 0, 0, 0));

        centerInfo.add(titleLbl);
        centerInfo.add(durLbl);

        JButton bookBtn = ThemeManager.createSecondaryButton("View Details");
        bookBtn.setPreferredSize(new Dimension(100, 45));
        bookBtn.putClientProperty("JButton.buttonType", "roundRect");
        bookBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        MouseAdapter clickAndHover = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (movieClickListener != null) {
                    movieClickListener.accept(new String[]{title, genre, duration, rating, desc});
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorderColor(ThemeManager.PRIMARY);
                bookBtn.setBackground(ThemeManager.PRIMARY);
                bookBtn.setForeground(Color.WHITE);
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorderColor(ThemeManager.BORDER);
                bookBtn.setBackground(Color.WHITE);
                bookBtn.setForeground(ThemeManager.TEXT_PRIMARY);
                card.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        };

        card.addMouseListener(clickAndHover);
        bookBtn.addMouseListener(clickAndHover);

        for (Component c : new Component[]{topInfo, centerInfo, titleLbl, durLbl, genreLbl, ratingLbl}) {
            c.addMouseListener(clickAndHover);
        }

        card.add(topInfo, BorderLayout.NORTH);
        card.add(centerInfo, BorderLayout.CENTER);
        card.add(bookBtn, BorderLayout.SOUTH);

        return card;
    }

    public JTextField getSearchField() {
        return searchField;
    }

    public JComboBox<String> getGenreFilter() {
        return genreFilter;
    }
}
