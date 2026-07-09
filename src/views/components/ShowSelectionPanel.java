package views.components;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import utils.ThemeManager;

public class ShowSelectionPanel extends JPanel {

    private String selectedMovieTitle = null;
    private String selectedTimeSlot = null;

    private final JPanel movieListPanel;
    private final JTextField searchField;
    private final JButton proceedButton;

    //right dia
    private final JLabel detailTitle;
    private final JLabel detailGenre;
    private final JTextArea detailDesc;
    private final JPanel timeSlotContainer;
    private final ButtonGroup timeSlotGroup;

    private final List<MovieCard> movieCards = new ArrayList<>();

    public ShowSelectionPanel() {
        setLayout(new BorderLayout());
        setBackground(ThemeManager.BACKGROUND);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        //card main
        RoundedPanel mainCard = new RoundedPanel(24, ThemeManager.BORDER);
        mainCard.setLayout(new BorderLayout());
        mainCard.setBackground(Color.WHITE);
        mainCard.setOpaque(false);

        //left dialog
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setPreferredSize(new Dimension(420, 0));
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeManager.BORDER));

        // Search  
        JPanel searchContainer = new JPanel(new BorderLayout());
        searchContainer.setBackground(Color.WHITE);
        searchContainer.setBorder(new EmptyBorder(30, 30, 20, 30));

        searchField = new JTextField();
        searchField.setFont(ThemeManager.FONT_BODY);
        searchField.setPreferredSize(new Dimension(100, 45));
        searchField.putClientProperty("JTextField.placeholderText", "Search movies...");
        searchField.putClientProperty("JComponent.roundRect", true); // Fully rounded search bar

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filterMovies();
            }

            public void removeUpdate(DocumentEvent e) {
                filterMovies();
            }

            public void changedUpdate(DocumentEvent e) {
                filterMovies();
            }
        });

        searchContainer.add(searchField, BorderLayout.CENTER);
        leftPanel.add(searchContainer, BorderLayout.NORTH);

        // Movie List 
        movieListPanel = new JPanel();
        movieListPanel.setLayout(new BoxLayout(movieListPanel, BoxLayout.Y_AXIS));
        movieListPanel.setBackground(Color.WHITE);
        movieListPanel.setBorder(new EmptyBorder(0, 30, 30, 30));

        JScrollPane scrollPane = new JScrollPane(movieListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        // right dialog
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(40, 50, 40, 50));

        JPanel detailTop = new JPanel();
        detailTop.setLayout(new BoxLayout(detailTop, BoxLayout.Y_AXIS));
        detailTop.setBackground(Color.WHITE);
        detailTop.setAlignmentX(Component.LEFT_ALIGNMENT);

        detailTitle = new JLabel("Select a movie to view details");
        detailTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));
        detailTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        detailTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        detailGenre = new JLabel(" ");
        detailGenre.setFont(ThemeManager.FONT_SUBHEADING);
        detailGenre.setForeground(ThemeManager.PRIMARY);
        detailGenre.setBorder(new EmptyBorder(8, 0, 20, 0));
        detailGenre.setAlignmentX(Component.LEFT_ALIGNMENT);

        detailDesc = new JTextArea("Search and select a movie from the left panel to see available showtimes and book your tickets.");
        detailDesc.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        detailDesc.setForeground(ThemeManager.TEXT_SECONDARY);
        detailDesc.setLineWrap(true);
        detailDesc.setWrapStyleWord(true);
        detailDesc.setEditable(false);
        detailDesc.setOpaque(false);
        detailDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailDesc.setMaximumSize(new Dimension(800, 100));

        detailTop.add(detailTitle);
        detailTop.add(detailGenre);
        detailTop.add(detailDesc);

        // Time Slots 
        JPanel timeSlotSection = new JPanel();
        timeSlotSection.setLayout(new BoxLayout(timeSlotSection, BoxLayout.Y_AXIS));
        timeSlotSection.setBackground(Color.WHITE);
        timeSlotSection.setBorder(new EmptyBorder(50, 0, 0, 0));
        timeSlotSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel timeSlotLabel = new JLabel("Available Time Slots");
        timeSlotLabel.setFont(ThemeManager.FONT_HEADING);
        timeSlotLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        timeSlotLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        timeSlotLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        timeSlotSection.add(timeSlotLabel);

        // Time slot pills 
        timeSlotContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        timeSlotContainer.setBackground(Color.WHITE);
        timeSlotContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        timeSlotGroup = new ButtonGroup();

        timeSlotSection.add(timeSlotContainer);
        detailTop.add(timeSlotSection);

        rightPanel.add(detailTop, BorderLayout.CENTER);

        //  Proceed Button
        JPanel bottomNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottomNav.setBackground(Color.WHITE);
        proceedButton = ThemeManager.createPrimaryButton("Select Seats \u2192");
        proceedButton.setPreferredSize(new Dimension(220, 48));
        proceedButton.putClientProperty("JButton.buttonType", "roundRect"); // Fully rounded
        proceedButton.setEnabled(false);
        bottomNav.add(proceedButton);
        rightPanel.add(bottomNav, BorderLayout.SOUTH);

        // Add to main rounded card
        mainCard.add(leftPanel, BorderLayout.WEST);
        mainCard.add(rightPanel, BorderLayout.CENTER);

        // Add card to this panel
        add(mainCard, BorderLayout.CENTER);

        loadDummyData();
    }

    private void filterMovies() {
        String query = searchField.getText().toLowerCase();
        for (MovieCard card : movieCards) {
            boolean matches = card.title.toLowerCase().contains(query);
            card.setVisible(matches);
        }
        movieListPanel.revalidate();
        movieListPanel.repaint();
    }

    private void selectMovie(MovieCard selectedCard) {
        for (MovieCard card : movieCards) {
            card.setSelected(card == selectedCard);
        }
        selectedMovieTitle = selectedCard.title;
        selectedTimeSlot = null;
        proceedButton.setEnabled(false);

        detailTitle.setText(selectedCard.title);
        detailGenre.setText(selectedCard.genre + " • " + selectedCard.duration);
        detailDesc.setText(selectedCard.description);

        // Render time slots as modern rounded pill buttons
        timeSlotContainer.removeAll();
        for (String time : selectedCard.times) {
            JToggleButton pill = createTimeSlotPill(time);
            timeSlotGroup.add(pill);
            timeSlotContainer.add(pill);
        }
        timeSlotContainer.revalidate();
        timeSlotContainer.repaint();
    }

    private JToggleButton createTimeSlotPill(String time) {
        JToggleButton btn = new JToggleButton(time);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(Color.WHITE);
        btn.setForeground(ThemeManager.TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Makes the toggle button a perfect rounded pill shape in FlatLaf
        btn.putClientProperty("JButton.buttonType", "roundRect");

        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER, 1, true),
                new EmptyBorder(12, 24, 12, 24)
        ));

        btn.addItemListener(e -> {
            if (btn.isSelected()) {
                btn.setBackground(ThemeManager.PRIMARY);
                btn.setForeground(Color.WHITE);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ThemeManager.PRIMARY, 1, true),
                        new EmptyBorder(12, 24, 12, 24)
                ));
                selectedTimeSlot = time;
                proceedButton.setEnabled(true);
            } else {
                btn.setBackground(Color.WHITE);
                btn.setForeground(ThemeManager.TEXT_PRIMARY);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ThemeManager.BORDER, 1, true),
                        new EmptyBorder(12, 24, 12, 24)
                ));
            }
        });
        return btn;
    }

    private void loadDummyData() {
        addMovie("Dune: Part Two", "Action / Sci-Fi", "166 mins",
                "Paul Atreides unites with Chani and the Fremen while on a warpath of revenge against the conspirators who destroyed his family.",
                new String[]{"14:30 PM - Hall 1", "18:00 PM - Hall 1 (IMAX)"});

        addMovie("Kung Fu Panda 4", "Animation / Family", "94 mins",
                "Po must train a new warrior when he's chosen to become the spiritual leader of the Valley of Peace.",
                new String[]{"10:00 AM - Hall 2", "15:00 PM - Hall 2"});

        addMovie("Godzilla x Kong", "Action / Sci-Fi", "115 mins",
                "Two ancient titans, Godzilla and Kong, clash in an epic battle as humans unravel their intertwined origins.",
                new String[]{"16:45 PM - Hall 3", "21:00 PM - Hall 3"});

        addMovie("Civil War", "Action / Thriller", "109 mins",
                "A journey across a dystopian future America, following a team of military-embedded journalists as they race against time.",
                new String[]{"19:30 PM - Hall 2"});
    }

    private void addMovie(String title, String genre, String duration, String desc, String[] times) {
        MovieCard card = new MovieCard(title, genre, duration, desc, times);
        movieCards.add(card);
        movieListPanel.add(card);
        movieListPanel.add(Box.createVerticalStrut(15));
    }

    // Modern rounded list card for movies
    private class MovieCard extends RoundedPanel {

        String title, genre, duration, description;
        String[] times;

        public MovieCard(String title, String genre, String duration, String description, String[] times) {
            super(16, ThemeManager.BORDER); // Generous 16px border radius
            this.title = title;
            this.genre = genre;
            this.duration = duration;
            this.description = description;
            this.times = times;

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(20, 20, 20, 20));
            setMaximumSize(new Dimension(400, 95));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel titleLbl = new JLabel(title);
            titleLbl.setFont(ThemeManager.FONT_SUBHEADING);
            titleLbl.setForeground(ThemeManager.TEXT_PRIMARY);
            titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel genreLbl = new JLabel(genre);
            genreLbl.setFont(ThemeManager.FONT_SMALL);
            genreLbl.setForeground(ThemeManager.TEXT_SECONDARY);
            genreLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

            add(titleLbl);
            add(Box.createVerticalStrut(6));
            add(genreLbl);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    selectMovie(MovieCard.this);
                }
            });
        }

        public void setSelected(boolean selected) {
            setBorderColor(selected ? ThemeManager.PRIMARY : ThemeManager.BORDER);
            setBackground(selected ? new Color(240, 248, 255) : Color.WHITE);
        }

        public void preselectMovieByTitle(String targetTitle) {
            for (MovieCard card : movieCards) {
                if (card.title.equalsIgnoreCase(targetTitle)) {
                    selectMovie(card);

                    // Auto-scroll the list to ensure the selected movie is visible
                    Rectangle bounds = card.getBounds();
                    movieListPanel.scrollRectToVisible(bounds);
                    break;
                }
            }
        }
    }

    public void preselectMovieByTitle(String targetTitle) {
        for (MovieCard card : movieCards) {
            if (card.title.equalsIgnoreCase(targetTitle)) {
                selectMovie(card);

                Rectangle bounds = card.getBounds();
                movieListPanel.scrollRectToVisible(bounds);
                break;
            }
        }
    }

    public String getSelectedMovieTitle() {
        return selectedMovieTitle;
    }

    public String getSelectedTimeSlot() {
        return selectedTimeSlot;
    }

    public JButton getProceedButton() {
        return proceedButton;
    }
}
