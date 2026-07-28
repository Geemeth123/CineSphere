package views.components;

import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MovieDetailsPanel extends JPanel {

    private final JButton backButton;
    private final JButton bookButton;

    private final JLabel titleLabel;
    private final JLabel metaLabel;
    private final JLabel ratingLabel;
    private final JTextArea synopsisArea;
    private final JLabel bannerLabel;
    private final JLabel posterLabel;

    private String currentMovieTitle;

    public MovieDetailsPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 50, 40, 50));

        // --- TOP: Navigation ---
        JPanel topNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topNav.setBackground(Color.WHITE);
        topNav.setBorder(new EmptyBorder(0, 0, 20, 0));
        backButton = ThemeManager.createSecondaryButton("\u2190 Back to Now Showing");
        backButton.setPreferredSize(new Dimension(220, 40));
        backButton.putClientProperty("JButton.buttonType", "roundRect");
        topNav.add(backButton);
        add(topNav, BorderLayout.NORTH);

        // --- CENTER: Scrollable Content ---
        JPanel contentContainer = new JPanel();
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
        contentContainer.setBackground(Color.WHITE);

        // 1. Cinematic Banner
        RoundedPanel bannerPanel = new RoundedPanel(24, ThemeManager.BORDER);
        bannerPanel.setLayout(new BorderLayout());
        bannerPanel.setBackground(new Color(20, 20, 20)); // Dark cinematic background
        bannerPanel.setPreferredSize(new Dimension(1000, 300));
        bannerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        bannerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        bannerLabel = new JLabel("BANNER IMAGE PLACEHOLDER", SwingConstants.CENTER);
        bannerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        bannerLabel.setForeground(new Color(255, 255, 255, 100)); // Translucent text
        bannerPanel.add(bannerLabel, BorderLayout.CENTER);

        contentContainer.add(bannerPanel);
        contentContainer.add(Box.createVerticalStrut(40));

        // 2. Details Section (Split: Poster on Left, Text on Right)
        JPanel detailsSection = new JPanel(new BorderLayout(50, 0));
        detailsSection.setBackground(Color.WHITE);
        detailsSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));

        // Poster
        RoundedPanel posterPanel = new RoundedPanel(16, ThemeManager.BORDER);
        posterPanel.setLayout(new BorderLayout());
        posterPanel.setBackground(ThemeManager.SURFACE);
        posterPanel.setPreferredSize(new Dimension(260, 380));

        posterLabel = new JLabel("POSTER", SwingConstants.CENTER);
        posterLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        posterLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        posterPanel.add(posterLabel, BorderLayout.CENTER);
        detailsSection.add(posterPanel, BorderLayout.WEST);

        // Text & Actions
        JPanel textSection = new JPanel();
        textSection.setLayout(new BoxLayout(textSection, BoxLayout.Y_AXIS));
        textSection.setBackground(Color.WHITE);

        titleLabel = new JLabel("Movie Title");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        metaLabel = new JLabel("2026  •  Genre  •  120 mins");
        metaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        metaLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        metaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        metaLabel.setBorder(new EmptyBorder(10, 0, 15, 0));

        ratingLabel = new JLabel("\u2605 8.5/10  (IMDb)");
        ratingLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        ratingLabel.setForeground(new Color(230, 160, 15)); // Gold
        ratingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ratingLabel.setBorder(new EmptyBorder(0, 0, 25, 0));

        JLabel synopsisTitle = new JLabel("Synopsis");
        synopsisTitle.setFont(ThemeManager.FONT_SUBHEADING);
        synopsisTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        synopsisTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        synopsisArea = new JTextArea("Movie description goes here...");
        synopsisArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        synopsisArea.setForeground(ThemeManager.TEXT_SECONDARY);
        synopsisArea.setLineWrap(true);
        synopsisArea.setWrapStyleWord(true);
        synopsisArea.setEditable(false);
        synopsisArea.setOpaque(false);
        synopsisArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        synopsisArea.setBorder(new EmptyBorder(10, 0, 30, 0));
        synopsisArea.setMaximumSize(new Dimension(800, 150));

        bookButton = ThemeManager.createPrimaryButton("Book Tickets for this Movie");
        bookButton.setPreferredSize(new Dimension(280, 50));
        bookButton.setMaximumSize(new Dimension(280, 50));
        bookButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        bookButton.putClientProperty("JButton.buttonType", "roundRect");
        bookButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        textSection.add(titleLabel);
        textSection.add(metaLabel);
        textSection.add(ratingLabel);
        textSection.add(synopsisTitle);
        textSection.add(synopsisArea);
        textSection.add(Box.createVerticalGlue());
        textSection.add(bookButton);

        detailsSection.add(textSection, BorderLayout.CENTER);
        contentContainer.add(detailsSection);

        JScrollPane scrollPane = new JScrollPane(contentContainer);
        scrollPane.setBorder(null);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadMovieDetails(String title, String genre, String duration, String rating, String description) {
        this.currentMovieTitle = title;
        titleLabel.setText(title);
        metaLabel.setText("2026  •  " + genre + "  •  " + duration);
        ratingLabel.setText("\u2605 " + rating + " / 10");
        synopsisArea.setText(description);

        bannerLabel.setText(title.toUpperCase() + " BANNER");
        posterLabel.setText("POSTER");
    }

    public String getCurrentMovieTitle() {
        return currentMovieTitle;
    }

    public JButton getBackButton() {
        return backButton;
    }

    public JButton getBookButton() {
        return bookButton;
    }
}
