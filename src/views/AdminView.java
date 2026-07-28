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
import views.components.MovieManagementPanel;
import views.components.SidebarPanel;
import views.components.StaffManagementPanel;
import views.components.StatCardPanel;

public class AdminView {

    private final JFrame frame;
    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final StaffManagementPanel staffManagementPanel;
    private final MovieManagementPanel movieManagementPanel;

    private final SidebarPanel sidebar;
    private JButton addMovieButton;

    // Stat Cards
    private StatCardPanel movieCountCard;
    private StatCardPanel staffCountCard;
    private StatCardPanel bookingCountCard;
    private StatCardPanel revenueCard;

    // Shows Table
    private JTable showsTable;
    private DefaultTableModel tableModel;

    public AdminView(User admin) {
        frame = new JFrame("CineSphere - Admin Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1366, 820);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // Initialize Sidebar with Admin Navigation
        String[] navLabels = {
            "Dashboard Overview",
            "Movies & TMDB",
            "Staff Management",
            "Sales & Reports"
        };

        sidebar = new SidebarPanel("SYSTEM ADMIN", admin.getFullName(), "Administrator", navLabels);
        frame.add(sidebar, BorderLayout.WEST);

        // Main Content Area with CardLayout for routing
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);

        staffManagementPanel = new StaffManagementPanel();
        movieManagementPanel = new MovieManagementPanel();

        // Add the Dashboard Overview panel
        contentPanel.add(createDashboardOverview(admin), "dashboard");
        contentPanel.add(movieManagementPanel, "movies");
        contentPanel.add(staffManagementPanel, "staff");
        contentPanel.add(createPlaceholderPanel("Sales Reports & Analytics"), "reports");

        frame.add(contentPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private JPanel createDashboardOverview(User admin) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(40, 50, 40, 50));

        // ==========================================
        // TOP: Header & Actions
        // ==========================================
        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.setBackground(Color.WHITE);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.WHITE);

        JLabel header = new JLabel("Admin Overview");
        header.setFont(ThemeManager.FONT_TITLE);
        header.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel welcome = new JLabel("Welcome, " + admin.getFullName() + ". Here is today's system summary.");
        welcome.setFont(ThemeManager.FONT_BODY);
        welcome.setForeground(ThemeManager.TEXT_SECONDARY);
        welcome.setBorder(new EmptyBorder(5, 0, 0, 0));

        titlePanel.add(header);
        titlePanel.add(welcome);
        headerContainer.add(titlePanel, BorderLayout.WEST);

        // Add Movie Action Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        buttonPanel.setBackground(Color.WHITE);

        addMovieButton = ThemeManager.createPrimaryButton("+ Add New Movie");
        addMovieButton.setPreferredSize(new Dimension(180, 45));
        addMovieButton.putClientProperty("JButton.buttonType", "roundRect");
        buttonPanel.add(addMovieButton);

        headerContainer.add(buttonPanel, BorderLayout.EAST);
        panel.add(headerContainer, BorderLayout.NORTH);

        // ==========================================
        // CENTER: Stats Grid & Active Shows Table
        // ==========================================
        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setBackground(Color.WHITE);
        centerContent.setBorder(new EmptyBorder(40, 0, 0, 0));

        // 1. Stats Grid
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 25, 0));
        statsGrid.setBackground(Color.WHITE);
        statsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        movieCountCard = new StatCardPanel("Active Movies", "0", "Currently showing");
        staffCountCard = new StatCardPanel("Total Staff", "0", "Registered accounts");
        bookingCountCard = new StatCardPanel("Today's Bookings", "0", "Tickets sold today");
        revenueCard = new StatCardPanel("Today's Revenue", "$0.00", "Gross daily income");

        statsGrid.add(movieCountCard);
        statsGrid.add(staffCountCard);
        statsGrid.add(bookingCountCard);
        statsGrid.add(revenueCard);

        centerContent.add(statsGrid);
        centerContent.add(Box.createVerticalStrut(40));

        // 2. Table Section Header
        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setBackground(Color.WHITE);
        tableHeaderPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableHeaderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel tableLabel = new JLabel("Currently Active Shows");
        tableLabel.setFont(ThemeManager.FONT_HEADING);
        tableLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        tableHeaderPanel.add(tableLabel, BorderLayout.WEST);

        centerContent.add(tableHeaderPanel);
        centerContent.add(Box.createVerticalStrut(15));

        // 3. Shows Table
        String[] columnNames = {"Show ID", "Movie Title", "Hall", "Time", "Seats Filled", "Status"};
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

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        // Highlight Status Column based on value
        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected) {
                    String status = value.toString();
                    if (status.equalsIgnoreCase("Running")) {
                        c.setForeground(ThemeManager.SUCCESS);
                    } else if (status.equalsIgnoreCase("Sold Out")) {
                        c.setForeground(ThemeManager.ERROR);
                    } else if (status.equalsIgnoreCase("Almost Full")) {
                        c.setForeground(new Color(230, 160, 15)); // Gold/Warning
                    } else {
                        c.setForeground(ThemeManager.PRIMARY);
                    }
                }
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 5) {
                table.getColumnModel().getColumn(i).setCellRenderer(statusRenderer); // Status Column
            } else if (i != 1) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer); // Center all except Title
            }
        }
    }

    private JPanel createPlaceholderPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel(title + " Page");
        label.setFont(ThemeManager.FONT_TITLE);
        label.setForeground(ThemeManager.TEXT_SECONDARY);
        panel.add(label);
        return panel;
    }

    // ==========================================
    // Controller Hooks & Data Methods
    // ==========================================
    public JFrame getFrame() {
        return frame;
    }

    public void showCard(String cardName) {
        cardLayout.show(contentPanel, cardName);
    }

    public void setActiveNav(int index) {
        sidebar.setActiveNav(index);
    }

    public void dispose() {
        frame.dispose();
    }

    public void addNavListener(int index, ActionListener listener) {
        sidebar.addNavListener(index, listener);
    }

    public void addLogoutListener(ActionListener listener) {
        sidebar.addLogoutListener(listener);
    }

    public void addAddMovieListener(ActionListener listener) {
        addMovieButton.addActionListener(listener);
    }

    /**
     * Prompts the admin to choose between TMDB or Manual entry.
     *
     * @return 0 for TMDB, 1 for Manual, -1/CLOSED_OPTION for cancel.
     */
    public int showAddMovieOptions() {
        String[] options = {"\u2193 Fetch via TMDB API", "\u270E Add Manually", "Cancel"};
        return JOptionPane.showOptionDialog(frame,
                "How would you like to add a new movie to the system?",
                "Add Movie Configuration",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
    }

    public void updateStats(int movieCount, int staffCount, int bookingCount, double revenue) {
        movieCountCard.setValue(String.valueOf(movieCount));
        staffCountCard.setValue(String.valueOf(staffCount));
        bookingCountCard.setValue(String.valueOf(bookingCount));
        revenueCard.setValue(String.format("$%.2f", revenue));
    }

    public void clearShowsTable() {
        tableModel.setRowCount(0);
    }

    public void addShowToTable(Object[] rowData) {
        tableModel.addRow(rowData);
    }

    public StaffManagementPanel getStaffManagementPanel() {
        return staffManagementPanel;
    }

    public MovieManagementPanel getMovieManagementPanel() {
        return movieManagementPanel;
    }
}
