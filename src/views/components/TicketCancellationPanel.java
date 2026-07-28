package views.components;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import utils.ThemeManager;

public class TicketCancellationPanel extends JPanel {

    private final JTextField searchField;
    private final JButton searchButton;

    private final RoundedPanel detailsCard;
    private final JLabel bookingIdLabel;
    private final JLabel movieLabel;
    private final JLabel ticketsLabel;
    private final JLabel priceLabel;
    private final JLabel statusLabel;

    private final JButton refundButton;
    private final JButton checkInButton;

    private final JTable activityTable;
    private final DefaultTableModel tableModel;

    private String currentMovie = "";
    private String currentBookingId = "";

    public TicketCancellationPanel() {
        setLayout(new BorderLayout(0, 25));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(40, 50, 40, 50));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Validation & Refund Desk");
        titleLabel.setFont(ThemeManager.FONT_TITLE);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Search for a ticket to validate entry or process a cancellation.");
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
        searchField.putClientProperty("JTextField.placeholderText", "Enter Booking ID (e.g. BK-1001)...");
        searchField.putClientProperty("JComponent.roundRect", true);

        searchButton = ThemeManager.createPrimaryButton("Search Ticket");
        searchButton.setPreferredSize(new Dimension(140, 45));
        searchButton.putClientProperty("JButton.buttonType", "roundRect");

        searchBarPanel.add(searchField);
        searchBarPanel.add(searchButton);
        headerPanel.add(searchBarPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 30));
        centerPanel.setBackground(Color.WHITE);

        detailsCard = new RoundedPanel(16, ThemeManager.BORDER);
        detailsCard.setLayout(new BorderLayout());
        detailsCard.setBackground(ThemeManager.SURFACE);
        detailsCard.setBorder(new EmptyBorder(25, 30, 25, 30));
        detailsCard.setVisible(false);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        bookingIdLabel = new JLabel("Booking ID: -");
        bookingIdLabel.setFont(ThemeManager.FONT_HEADING);
        bookingIdLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        movieLabel = new JLabel("Movie: -");
        movieLabel.setFont(ThemeManager.FONT_BODY);
        movieLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        movieLabel.setBorder(new EmptyBorder(8, 0, 4, 0));

        ticketsLabel = new JLabel("Seats: -");
        ticketsLabel.setFont(ThemeManager.FONT_BODY);
        ticketsLabel.setForeground(ThemeManager.TEXT_SECONDARY);

        priceLabel = new JLabel("Amount: -");
        priceLabel.setFont(ThemeManager.FONT_BODY);
        priceLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        priceLabel.setBorder(new EmptyBorder(4, 0, 0, 0));

        infoPanel.add(bookingIdLabel);
        infoPanel.add(movieLabel);
        infoPanel.add(ticketsLabel);
        infoPanel.add(priceLabel);

        JPanel actionContainer = new JPanel(new BorderLayout());
        actionContainer.setOpaque(false);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        statusPanel.setOpaque(false);
        statusLabel = new JLabel("Status: -");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusPanel.add(statusLabel);

        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actionButtonPanel.setOpaque(false);
        actionButtonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        checkInButton = ThemeManager.createPrimaryButton("Validate / Check-In");
        checkInButton.setPreferredSize(new Dimension(170, 42));
        checkInButton.setBackground(ThemeManager.SUCCESS);
        checkInButton.putClientProperty("JButton.buttonType", "roundRect");

        refundButton = new JButton("Cancel & Refund");
        refundButton.setFont(ThemeManager.FONT_BUTTON);
        refundButton.setForeground(Color.WHITE);
        refundButton.setBackground(ThemeManager.ERROR);
        refundButton.setFocusPainted(false);
        refundButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refundButton.setPreferredSize(new Dimension(160, 42));
        refundButton.putClientProperty("JButton.buttonType", "roundRect");

        actionButtonPanel.add(checkInButton);
        actionButtonPanel.add(refundButton);

        actionContainer.add(statusPanel, BorderLayout.NORTH);
        actionContainer.add(actionButtonPanel, BorderLayout.SOUTH);

        detailsCard.add(infoPanel, BorderLayout.WEST);
        detailsCard.add(actionContainer, BorderLayout.EAST);

        JPanel tableContainer = new JPanel(new BorderLayout(0, 15));
        tableContainer.setBackground(Color.WHITE);

        JLabel tableTitle = new JLabel("Recent Desk Activity");
        tableTitle.setFont(ThemeManager.FONT_SUBHEADING);
        tableTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        tableContainer.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"Booking ID", "Movie", "Action Taken", "Time"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        activityTable = new JTable(tableModel);
        styleActivityTable(activityTable);

        JScrollPane scrollPane = new JScrollPane(activityTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER, 1));
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setBackground(Color.WHITE);
        topWrapper.add(detailsCard, BorderLayout.NORTH);

        centerPanel.add(topWrapper, BorderLayout.NORTH);
        centerPanel.add(tableContainer, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void styleActivityTable(JTable table) {
        table.setRowHeight(45);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(ThemeManager.BORDER);
        table.setFont(ThemeManager.FONT_BODY);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(ThemeManager.TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(100, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.BORDER));

        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(column == 1 ? SwingConstants.LEFT : SwingConstants.CENTER);

                if (!isSelected) {
                    String action = table.getModel().getValueAt(table.convertRowIndexToModel(row), 2).toString();
                    if (action.equalsIgnoreCase("Checked-In")) {
                        c.setBackground(new Color(235, 255, 240));
                    } else if (action.equalsIgnoreCase("Refunded") || action.equalsIgnoreCase("Cancelled")) {
                        c.setBackground(new Color(255, 235, 235));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                    c.setForeground(ThemeManager.TEXT_PRIMARY);
                } else {
                    c.setBackground(new Color(240, 248, 255));
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }
    }

    public void showTicketDetails(String bookingId, String movie, String tickets, String price, String status) {
        this.currentBookingId = bookingId;
        this.currentMovie = movie;

        bookingIdLabel.setText("Booking ID: " + bookingId);
        movieLabel.setText("Movie: " + movie);
        ticketsLabel.setText("Seats: " + tickets);
        priceLabel.setText("Amount: " + price);
        statusLabel.setText(status.toUpperCase());

        detailsCard.setBackground(ThemeManager.SURFACE);
        detailsCard.setBorderColor(ThemeManager.BORDER);

        if (status.equalsIgnoreCase("Cancelled") || status.equalsIgnoreCase("Refunded")) {
            statusLabel.setForeground(ThemeManager.ERROR);
            refundButton.setEnabled(false);
            checkInButton.setEnabled(false);
        } else if (status.equalsIgnoreCase("Checked-In")) {
            statusLabel.setForeground(ThemeManager.SUCCESS);
            refundButton.setEnabled(false);
            checkInButton.setEnabled(false);
        } else {
            statusLabel.setForeground(ThemeManager.PRIMARY);
            refundButton.setEnabled(true);
            checkInButton.setEnabled(true);
        }

        detailsCard.setVisible(true);
    }

    public void logActivity(String action) {
        String time = new SimpleDateFormat("hh:mm a").format(new Date());
        tableModel.insertRow(0, new Object[]{currentBookingId, currentMovie, action, time});
    }

    public void reset() {
        searchField.setText("");
        detailsCard.setVisible(false);
    }

    public JButton getSearchButton() {
        return searchButton;
    }

    public JButton getRefundButton() {
        return refundButton;
    }

    public JButton getCheckInButton() {
        return checkInButton;
    }

    public JTextField getSearchField() {
        return searchField;
    }
}
