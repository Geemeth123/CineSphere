package views.components;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import models.Movie;
import models.TMDBMovie;
import utils.ThemeManager;
import utils.TMDBClient;

/**
 * Modal dialog for adding a TMDB movie to the local theater database.
 * Shows read-only movie info from TMDB and collects system-specific details
 * (duration, adult/kid prices) from the admin.
 */
public class AddMovieDialog extends JDialog {

    private final JSpinner durationSpinner;
    private final JTextField adultPriceField;
    private final JTextField kidsPriceField;
    private Movie resultMovie;
    private boolean confirmed = false;

    public AddMovieDialog(Frame owner, TMDBMovie tmdbMovie) {
        super(owner, "Add Movie to Theater", true);
        setSize(520, 580);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(30, 35, 30, 35));

        // ==========================================
        // TOP — Movie Info (Read-Only)
        // ==========================================
        RoundedPanel infoCard = new RoundedPanel(16, ThemeManager.BORDER);
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setBackground(ThemeManager.SURFACE);
        infoCard.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel titleLabel = new JLabel(tmdbMovie.getTitle());
        titleLabel.setFont(ThemeManager.FONT_HEADING);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Truncate long descriptions
        String desc = tmdbMovie.getOverview();
        if (desc.length() > 200) {
            desc = desc.substring(0, 197) + "...";
        }
        JTextArea descArea = new JTextArea(desc);
        descArea.setFont(ThemeManager.FONT_BODY);
        descArea.setForeground(ThemeManager.TEXT_SECONDARY);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setOpaque(false);
        descArea.setBorder(new EmptyBorder(8, 0, 8, 0));
        descArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        descArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        String genreName = TMDBClient.getGenreNames(tmdbMovie.getGenreIds());
        JLabel metaLabel = new JLabel(
            "⭐ " + String.format("%.1f", tmdbMovie.getVoteAverage()) + " / 10   •   "
            + "📅 " + tmdbMovie.getReleaseDate() + "   •   "
            + "🎬 " + genreName
        );
        metaLabel.setFont(ThemeManager.FONT_SMALL);
        metaLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        metaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoCard.add(titleLabel);
        infoCard.add(descArea);
        infoCard.add(metaLabel);

        mainPanel.add(infoCard, BorderLayout.NORTH);

        // ==========================================
        // CENTER — Editable Fields
        // ==========================================
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(25, 0, 0, 0));

        JLabel formTitle = new JLabel("Theater Configuration");
        formTitle.setFont(ThemeManager.FONT_SUBHEADING);
        formTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(formTitle);
        formPanel.add(Box.createVerticalStrut(20));

        // Duration
        JPanel durationRow = createFormRow("Duration (minutes):");
        durationSpinner = new JSpinner(new SpinnerNumberModel(120, 1, 500, 1));
        durationSpinner.setFont(ThemeManager.FONT_BODY);
        durationSpinner.setPreferredSize(new Dimension(200, 40));
        ((JSpinner.DefaultEditor) durationSpinner.getEditor()).getTextField().setFont(ThemeManager.FONT_BODY);
        durationRow.add(durationSpinner);
        formPanel.add(durationRow);
        formPanel.add(Box.createVerticalStrut(15));

        // Adult Price
        JPanel adultRow = createFormRow("Adult Ticket Price ($):");
        adultPriceField = new JTextField("350.00");
        adultPriceField.setFont(ThemeManager.FONT_BODY);
        adultPriceField.setPreferredSize(new Dimension(200, 40));
        adultPriceField.putClientProperty("JComponent.roundRect", true);
        adultRow.add(adultPriceField);
        formPanel.add(adultRow);
        formPanel.add(Box.createVerticalStrut(15));

        // Kids Price
        JPanel kidsRow = createFormRow("Kids Ticket Price ($):");
        kidsPriceField = new JTextField("200.00");
        kidsPriceField.setFont(ThemeManager.FONT_BODY);
        kidsPriceField.setPreferredSize(new Dimension(200, 40));
        kidsPriceField.putClientProperty("JComponent.roundRect", true);
        kidsRow.add(kidsPriceField);
        formPanel.add(kidsRow);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // ==========================================
        // BOTTOM — Action Buttons
        // ==========================================
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(25, 0, 0, 0));

        JButton cancelButton = ThemeManager.createSecondaryButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(120, 42));
        cancelButton.putClientProperty("JButton.buttonType", "roundRect");

        JButton addButton = ThemeManager.createPrimaryButton("Add Movie");
        addButton.setPreferredSize(new Dimension(140, 42));
        addButton.putClientProperty("JButton.buttonType", "roundRect");

        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        addButton.addActionListener(e -> {
            if (validateAndBuild(tmdbMovie, genreName)) {
                confirmed = true;
                dispose();
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(addButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createFormRow(String labelText) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setBackground(Color.WHITE);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JLabel label = new JLabel(labelText);
        label.setFont(ThemeManager.FONT_BODY);
        label.setForeground(ThemeManager.TEXT_PRIMARY);
        label.setPreferredSize(new Dimension(200, 40));
        row.add(label);

        return row;
    }

    private boolean validateAndBuild(TMDBMovie tmdbMovie, String genreName) {
        double adultPrice;
        double kidsPrice;

        try {
            adultPrice = Double.parseDouble(adultPriceField.getText().trim());
            if (adultPrice < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid adult ticket price.",
                "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            kidsPrice = Double.parseDouble(kidsPriceField.getText().trim());
            if (kidsPrice < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid kids ticket price.",
                "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        int duration = (int) durationSpinner.getValue();

        resultMovie = new Movie(
            tmdbMovie.getTitle(),
            tmdbMovie.getOverview(),
            tmdbMovie.getPosterPath(),
            tmdbMovie.getVoteAverage(),
            tmdbMovie.getReleaseDate(),
            duration,
            genreName,
            adultPrice,
            kidsPrice,
            tmdbMovie.getId()
        );

        return true;
    }

    /**
     * Returns the populated Movie object if confirmed, or null if cancelled.
     */
    public Movie getResult() {
        return confirmed ? resultMovie : null;
    }
}
