package views.components;

import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Random;

public class ReceiptPanel extends JPanel {

    private final JLabel bookingIdLabel;
    private final JLabel movieTitleLabel;
    private final JLabel timeHallLabel;
    private final JLabel seatsLabel;
    private final JLabel totalLabel;

    private final JButton downloadButton;
    private final JButton homeButton;
    private final JButton cancellationDeskButton;

    public ReceiptPanel() {
        setLayout(new GridBagLayout()); // Centers the receipt card
        setBackground(ThemeManager.BACKGROUND);

        RoundedPanel receiptCard = new RoundedPanel(24, ThemeManager.BORDER);
        receiptCard.setLayout(new BoxLayout(receiptCard, BoxLayout.Y_AXIS));
        receiptCard.setBackground(Color.WHITE);
        receiptCard.setBorder(new EmptyBorder(40, 50, 40, 50));
        receiptCard.setPreferredSize(new Dimension(600, 550));
        receiptCard.setMaximumSize(new Dimension(600, 550));

        // --- Header: Success Message ---
        JLabel successIcon = new JLabel("\u2713", SwingConstants.CENTER); // Checkmark
        successIcon.setFont(new Font("Segoe UI", Font.BOLD, 48));
        successIcon.setForeground(ThemeManager.SUCCESS);
        successIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel successLabel = new JLabel("Booking Confirmed!");
        successLabel.setFont(ThemeManager.FONT_TITLE);
        successLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        successLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        bookingIdLabel = new JLabel("Booking ID: -");
        bookingIdLabel.setFont(ThemeManager.FONT_BODY);
        bookingIdLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        bookingIdLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bookingIdLabel.setBorder(new EmptyBorder(5, 0, 30, 0));

        receiptCard.add(successIcon);
        receiptCard.add(successLabel);
        receiptCard.add(bookingIdLabel);

        // --- Center: Ticket Details & QR Code ---
        JPanel detailsContainer = new JPanel(new BorderLayout());
        detailsContainer.setOpaque(false);
        detailsContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 2, 0, ThemeManager.SURFACE), // Top & Bottom borders
                new EmptyBorder(25, 0, 25, 0)
        ));

        // Left Side: Text Details
        JPanel textDetails = new JPanel();
        textDetails.setLayout(new BoxLayout(textDetails, BoxLayout.Y_AXIS));
        textDetails.setOpaque(false);

        movieTitleLabel = new JLabel("Movie Title");
        movieTitleLabel.setFont(ThemeManager.FONT_HEADING);
        movieTitleLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        timeHallLabel = new JLabel("Time & Hall");
        timeHallLabel.setFont(ThemeManager.FONT_BODY);
        timeHallLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        timeHallLabel.setBorder(new EmptyBorder(5, 0, 15, 0));

        seatsLabel = new JLabel("Seats: -");
        seatsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        seatsLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        totalLabel = new JLabel("Total Paid: $0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalLabel.setForeground(ThemeManager.PRIMARY);
        totalLabel.setBorder(new EmptyBorder(5, 0, 0, 0));

        textDetails.add(movieTitleLabel);
        textDetails.add(timeHallLabel);
        textDetails.add(seatsLabel);
        textDetails.add(totalLabel);

        detailsContainer.add(textDetails, BorderLayout.WEST);

        // Right Side: QR Code Generator
        JPanel qrPanel = createQRCodePanel();
        detailsContainer.add(qrPanel, BorderLayout.EAST);

        receiptCard.add(detailsContainer);

        // --- Bottom: Actions ---
        JPanel actionContainer = new JPanel();
        actionContainer.setLayout(new BoxLayout(actionContainer, BoxLayout.Y_AXIS));
        actionContainer.setOpaque(false);
        actionContainer.setBorder(new EmptyBorder(30, 0, 0, 0));

        downloadButton = ThemeManager.createPrimaryButton("\u21E3 Download Digital Receipt (PDF)");
        downloadButton.setPreferredSize(new Dimension(Integer.MAX_VALUE, 45));
        downloadButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        downloadButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel secondaryActions = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        secondaryActions.setOpaque(false);
        secondaryActions.setBorder(new EmptyBorder(15, 0, 0, 0));

        homeButton = ThemeManager.createSecondaryButton("Back to Dashboard");
        homeButton.setPreferredSize(new Dimension(180, 40));

        cancellationDeskButton = ThemeManager.createSecondaryButton("Validation Desk \u2192");
        cancellationDeskButton.setPreferredSize(new Dimension(180, 40));

        secondaryActions.add(homeButton);
        secondaryActions.add(cancellationDeskButton);

        actionContainer.add(downloadButton);
        actionContainer.add(secondaryActions);

        receiptCard.add(actionContainer);

        add(receiptCard);
    }

    /**
     * Draws a stylized mock QR code to fit the Minimalist UI.
     */
    private JPanel createQRCodePanel() {
        JPanel qrMock = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.BLACK);

                // Fixed seed so the QR code doesn't randomly change shape when the mouse moves
                Random rand = new Random(42);
                int size = 120;
                int blocks = 6;
                int blockSize = size / blocks;

                for (int i = 0; i < blocks; i++) {
                    for (int j = 0; j < blocks; j++) {
                        if (rand.nextBoolean()) {
                            g2.fillRect(i * blockSize, j * blockSize, blockSize, blockSize);
                        }
                    }
                }
                // Draw locator squares
                g2.fillRect(0, 0, blockSize * 2, blockSize * 2);
                g2.setColor(Color.WHITE);
                g2.fillRect(blockSize / 2, blockSize / 2, blockSize, blockSize);
                g2.setColor(Color.BLACK);

                g2.fillRect((blocks - 2) * blockSize, 0, blockSize * 2, blockSize * 2);
                g2.setColor(Color.WHITE);
                g2.fillRect((blocks - 2) * blockSize + blockSize / 2, blockSize / 2, blockSize, blockSize);
                g2.setColor(Color.BLACK);

                g2.fillRect(0, (blocks - 2) * blockSize, blockSize * 2, blockSize * 2);
                g2.setColor(Color.WHITE);
                g2.fillRect(blockSize / 2, (blocks - 2) * blockSize + blockSize / 2, blockSize, blockSize);

                g2.dispose();
            }
        };
        qrMock.setPreferredSize(new Dimension(120, 120));
        qrMock.setMaximumSize(new Dimension(120, 120));
        qrMock.setBackground(Color.WHITE);
        qrMock.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER, 2));

        // Wrap to align nicely
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(qrMock);
        return wrapper;
    }

    public void updateReceipt(String bookingId, String movie, String timeHall, String seats, String total) {
        bookingIdLabel.setText("Booking ID: " + bookingId);
        movieTitleLabel.setText(movie);
        timeHallLabel.setText(timeHall);
        seatsLabel.setText("Seats: " + seats);
        totalLabel.setText("Total Paid: " + total);
    }

    public JButton getDownloadButton() {
        return downloadButton;
    }

    public JButton getHomeButton() {
        return homeButton;
    }

    public JButton getCancellationDeskButton() {
        return cancellationDeskButton;
    }
}
