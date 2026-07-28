package views;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import models.User;
import utils.ThemeManager;
import views.components.*;

public class TicketView {

    private final JFrame frame;
    private final CardLayout cardLayout;
    private final JPanel contentPanel;

    private final SidebarPanel sidebar;
    private JButton bookTicketButton;
    private JTextField searchField;
    private JButton searchButton;

    private StatCardPanel movieCountCard;
    private StatCardPanel showCountCard;
    private StatCardPanel bookingCountCard;
    private StatCardPanel ticketsSoldCard;

    private JTable showsTable;
    private DefaultTableModel tableModel;

    // Core Application Panels
    private final SeatMapPanel seatMapPanel;
    private final ShowSelectionPanel showSelectionPanel;
    private final NowShowingPanel nowShowingPanel;
    private final BookingHistoryPanel bookingHistoryPanel;
    private final TicketCancellationPanel ticketCancellationPanel;
    private final MovieDetailsPanel movieDetailsPanel;
    private final ReceiptPanel receiptPanel;

    public TicketView(User user) {
        frame = new JFrame("CineSphere - Ticket Counter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1366, 820);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        String[] navLabels = {
            "Dashboard",
            "Now Showing",
            "Book Tickets",
            "Booking History",
            "Cancellation Desk"
        };

        sidebar = new SidebarPanel("TICKET STAFF", user.getFullName(), "Front Desk", navLabels);
        frame.add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);

        seatMapPanel = new SeatMapPanel();
        showSelectionPanel = new ShowSelectionPanel();
        nowShowingPanel = new NowShowingPanel();
        bookingHistoryPanel = new BookingHistoryPanel();
        ticketCancellationPanel = new TicketCancellationPanel();
        movieDetailsPanel = new MovieDetailsPanel();
        receiptPanel = new ReceiptPanel();

        contentPanel.add(createDashboardOverview(user), "dashboard");
        contentPanel.add(showSelectionPanel, "show_selection");
        contentPanel.add(seatMapPanel, "seatmap");
        contentPanel.add(nowShowingPanel, "now_showing");
        contentPanel.add(bookingHistoryPanel, "booking_history");
        contentPanel.add(ticketCancellationPanel, "cancellation_desk");
        contentPanel.add(movieDetailsPanel, "movie_details");
        contentPanel.add(receiptPanel, "receipt");

        frame.add(contentPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private JPanel createDashboardOverview(User user) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(40, 50, 40, 50));

        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.setBackground(Color.WHITE);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.WHITE);

        JLabel header = new JLabel("Ticket Counter");
        header.setFont(ThemeManager.FONT_TITLE);
        header.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel welcome = new JLabel("Welcome back, " + user.getFullName() + ". Have a great shift.");
        welcome.setFont(ThemeManager.FONT_BODY);
        welcome.setForeground(ThemeManager.TEXT_SECONDARY);
        welcome.setBorder(new EmptyBorder(5, 0, 0, 0));

        titlePanel.add(header);
        titlePanel.add(welcome);
        headerContainer.add(titlePanel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        buttonPanel.setBackground(Color.WHITE);

        bookTicketButton = ThemeManager.createPrimaryButton("+ New Booking");
        bookTicketButton.setPreferredSize(new Dimension(160, 45));
        bookTicketButton.putClientProperty("JButton.buttonType", "roundRect");
        buttonPanel.add(bookTicketButton);

        headerContainer.add(buttonPanel, BorderLayout.EAST);
        panel.add(headerContainer, BorderLayout.NORTH);

        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setBackground(Color.WHITE);
        centerContent.setBorder(new EmptyBorder(40, 0, 0, 0));

        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 25, 0));
        statsGrid.setBackground(Color.WHITE);
        statsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        movieCountCard = new StatCardPanel("Now Showing", "0", "Movies available");
        showCountCard = new StatCardPanel("Today's Shows", "0", "Currently scheduled");
        bookingCountCard = new StatCardPanel("Total Bookings", "0", "Confirmed today");
        ticketsSoldCard = new StatCardPanel("Tickets Sold", "0", "Total tickets today");

        statsGrid.add(movieCountCard);
        statsGrid.add(showCountCard);
        statsGrid.add(bookingCountCard);
        statsGrid.add(ticketsSoldCard);

        centerContent.add(statsGrid);
        centerContent.add(Box.createVerticalStrut(40));

        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setBackground(Color.WHITE);
        tableHeaderPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableHeaderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

        JLabel tableLabel = new JLabel("Today's Schedule");
        tableLabel.setFont(ThemeManager.FONT_HEADING);
        tableLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        tableHeaderPanel.add(tableLabel, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setBackground(Color.WHITE);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(220, 38));
        searchField.putClientProperty("JTextField.placeholderText", "Search movies...");
        searchField.putClientProperty("JComponent.roundRect", true);

        searchButton = ThemeManager.createSecondaryButton("Search");
        searchButton.setPreferredSize(new Dimension(80, 38));
        searchButton.putClientProperty("JButton.buttonType", "roundRect");

        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        tableHeaderPanel.add(searchPanel, BorderLayout.EAST);

        centerContent.add(tableHeaderPanel);
        centerContent.add(Box.createVerticalStrut(15));

        String[] columnNames = {"\u2139", "Show ID", "Movie Title", "Hall", "Time", "Seats", "Tickets Sold"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        showsTable = new JTable(tableModel);
        styleTable(showsTable);

        JScrollPane scrollPane = new JScrollPane(showsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER, 1));
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        centerContent.add(scrollPane);
        panel.add(centerContent, BorderLayout.CENTER);

        return panel;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(45);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(ThemeManager.BORDER);
        table.setFont(ThemeManager.FONT_BODY);
        table.setSelectionBackground(new Color(240, 248, 255));
        table.setSelectionForeground(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(ThemeManager.TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(100, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.BORDER));

        table.getColumnModel().getColumn(0).setMaxWidth(40);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer infoRenderer = new DefaultTableCellRenderer();
        infoRenderer.setHorizontalAlignment(JLabel.CENTER);
        infoRenderer.setForeground(ThemeManager.PRIMARY);
        infoRenderer.setFont(new Font("Segoe UI", Font.BOLD, 16));

        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 0) {
                table.getColumnModel().getColumn(i).setCellRenderer(infoRenderer);
            } else if (i != 2) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }

    public JFrame getFrame() {
        return frame;
    }

    public void showCard(String cardName) {
        cardLayout.show(contentPanel, cardName);
    }

    public void setActiveNav(int index) {
        sidebar.setActiveNav(index);
    }

    public JTable getShowsTable() {
        return showsTable;
    }

    public void updateStats(int movieCount, int showCount, int bookingCount, int ticketsSold) {
        movieCountCard.setValue(String.valueOf(movieCount));
        showCountCard.setValue(String.valueOf(showCount));
        bookingCountCard.setValue(String.valueOf(bookingCount));
        ticketsSoldCard.setValue(String.valueOf(ticketsSold));
    }

    public void clearShowsTable() {
        tableModel.setRowCount(0);
    }

    public void addShowToTable(Object[] rowData) {
        tableModel.addRow(rowData);
    }

    public void addNavListener(int index, ActionListener listener) {
        sidebar.addNavListener(index, listener);
    }

    public void addLogoutListener(ActionListener listener) {
        sidebar.addLogoutListener(listener);
    }

    public void addBookTicketListener(ActionListener listener) {
        bookTicketButton.addActionListener(listener);
    }

    public void addSearchListener(ActionListener listener) {
        searchButton.addActionListener(listener);
    }

    // Panel Getters for Controller
    public SeatMapPanel getSeatMapPanel() {
        return seatMapPanel;
    }

    public ShowSelectionPanel getShowSelectionPanel() {
        return showSelectionPanel;
    }

    public NowShowingPanel getNowShowingPanel() {
        return nowShowingPanel;
    }

    public BookingHistoryPanel getBookingHistoryPanel() {
        return bookingHistoryPanel;
    }

    public TicketCancellationPanel getTicketCancellationPanel() {
        return ticketCancellationPanel;
    }

    public MovieDetailsPanel getMovieDetailsPanel() {
        return movieDetailsPanel;
    }

    public ReceiptPanel getReceiptPanel() {
        return receiptPanel;
    }

    public void dispose() {
        frame.dispose();
    }
}
