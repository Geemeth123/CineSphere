package views.components;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import utils.ThemeManager;

public class BookingHistoryPanel extends JPanel {

    private final JTable historyTable;
    private final DefaultTableModel tableModel;
    private final JComboBox<String> filterCombo;
    private final JTextField searchField;
    private final JButton searchButton;
    private final JLabel totalEarningsLabel;

    private final JButton downloadReceiptButton;

    public BookingHistoryPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(40, 50, 40, 50));

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(Color.WHITE);
        topContainer.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Booking History");
        titleLabel.setFont(ThemeManager.FONT_TITLE);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("View past transactions and receipts.");
        subtitleLabel.setFont(ThemeManager.FONT_BODY);
        subtitleLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        subtitleLabel.setBorder(new EmptyBorder(5, 0, 0, 0));

        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        topContainer.add(titlePanel, BorderLayout.WEST);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        controlPanel.setBackground(Color.WHITE);

        filterCombo = new JComboBox<>(new String[]{"Today", "Yesterday", "This Week", "This Month", "All Time"});
        filterCombo.setPreferredSize(new Dimension(150, 40));
        filterCombo.setFont(ThemeManager.FONT_BODY);
        filterCombo.setBackground(Color.WHITE);
        filterCombo.putClientProperty("JComponent.roundRect", true);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 40));
        searchField.setFont(ThemeManager.FONT_BODY);
        searchField.putClientProperty("JTextField.placeholderText", "Booking ID or Movie...");
        searchField.putClientProperty("JComponent.roundRect", true);

        searchButton = ThemeManager.createPrimaryButton("Search");
        searchButton.setPreferredSize(new Dimension(90, 40));
        searchButton.putClientProperty("JButton.buttonType", "roundRect");

        controlPanel.add(filterCombo);
        controlPanel.add(searchField);
        controlPanel.add(searchButton);
        topContainer.add(controlPanel, BorderLayout.EAST);

        add(topContainer, BorderLayout.NORTH);

        String[] columns = {"Booking ID", "Date", "Movie Title", "Hall", "Tickets", "Status", "Amount"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        styleHistoryTable(historyTable);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER, 1));
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(new EmptyBorder(25, 0, 0, 0));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionPanel.setBackground(Color.WHITE);

        downloadReceiptButton = ThemeManager.createSecondaryButton("Download Receipt");
        downloadReceiptButton.putClientProperty("JButton.buttonType", "roundRect");
        downloadReceiptButton.setPreferredSize(new Dimension(180, 40));

        actionPanel.add(downloadReceiptButton);
        bottomPanel.add(actionPanel, BorderLayout.WEST);

        JPanel earningsBox = new RoundedPanel(12, ThemeManager.BORDER);
        earningsBox.setBackground(ThemeManager.SURFACE);
        earningsBox.setLayout(new FlowLayout(FlowLayout.RIGHT, 25, 8));

        JLabel totalText = new JLabel("Total Earnings for selected period:");
        totalText.setFont(ThemeManager.FONT_BODY);
        totalText.setForeground(ThemeManager.TEXT_SECONDARY);

        totalEarningsLabel = new JLabel("$0.00");
        totalEarningsLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        totalEarningsLabel.setForeground(ThemeManager.PRIMARY);

        earningsBox.add(totalText);
        earningsBox.add(totalEarningsLabel);

        bottomPanel.add(earningsBox, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        loadDummyData();
    }

    private void styleHistoryTable(JTable table) {
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

        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(column == 2 ? SwingConstants.LEFT : SwingConstants.CENTER);

                if (!isSelected) {
                    String status = table.getModel().getValueAt(table.convertRowIndexToModel(row), 5).toString();
                    if (status.equalsIgnoreCase("Checked-In")) {
                        c.setBackground(new Color(235, 255, 240)); // Light Green Highlight
                    } else if (status.equalsIgnoreCase("Cancelled") || status.equalsIgnoreCase("Refunded")) {
                        c.setBackground(new Color(255, 235, 235)); // Light Red Highlight
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
            table.getColumnModel().getColumn(i).setCellRenderer(statusRenderer);
        }
    }

    private void loadDummyData() {
        tableModel.setRowCount(0);
        Object[][] data = {
            {"BK-1001", "2026-07-08", "Dune: Part Two", "Hall 1", "2 Adult, 1 Child", "Checked-In", "$40.00"},
            {"BK-1002", "2026-07-08", "Godzilla x Kong", "Hall 3", "1 Adult", "Confirmed", "$15.00"},
            {"BK-1003", "2026-07-08", "Kung Fu Panda 4", "Hall 2", "2 Child", "Confirmed", "$20.00"},
            {"BK-1004", "2026-07-07", "Civil War", "Hall 2", "3 Adult", "Checked-In", "$45.00"},
            {"BK-1005", "2026-07-07", "Ghostbusters", "Hall 1", "2 Adult", "Cancelled", "$0.00"}
        };

        double total = 0;
        for (Object[] row : data) {
            tableModel.addRow(row);
            if (row[5].equals("Confirmed") || row[5].equals("Checked-In")) {
                String amtStr = row[6].toString().replace("$", "");
                total += Double.parseDouble(amtStr);
            }
        }
        totalEarningsLabel.setText(String.format("$%.2f", total));
    }

    public JButton getDownloadReceiptButton() {
        return downloadReceiptButton;
    }

    public JButton getSearchButton() {
        return searchButton;
    }

    public JTextField getSearchField() {
        return searchField;
    }

    public JTable getHistoryTable() {
        return historyTable;
    }
}
