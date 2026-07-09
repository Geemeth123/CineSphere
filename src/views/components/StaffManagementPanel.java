package views.components;

import models.User;
import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class StaffManagementPanel extends JPanel {

    private final JTable staffTable;
    private final DefaultTableModel tableModel;
    private final JTextField searchField;
    
    private final JButton addButton;
    private final JButton editButton;
    private final JButton deleteButton;

    public StaffManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(40, 50, 40, 50));

        // --- TOP: Header & Controls ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Staff Management");
        titleLabel.setFont(ThemeManager.FONT_TITLE);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        
        JLabel subtitleLabel = new JLabel("Create, update, and deactivate system user accounts.");
        subtitleLabel.setFont(ThemeManager.FONT_BODY);
        subtitleLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        subtitleLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
        
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        // Control Panel (Search + Actions)
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        controlPanel.setBackground(Color.WHITE);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 40));
        searchField.setFont(ThemeManager.FONT_BODY);
        searchField.putClientProperty("JTextField.placeholderText", "Search staff...");
        searchField.putClientProperty("JComponent.roundRect", true);

        addButton = ThemeManager.createPrimaryButton("+ Add Staff");
        addButton.setPreferredSize(new Dimension(120, 40));

        editButton = ThemeManager.createSecondaryButton("Edit");
        editButton.setPreferredSize(new Dimension(80, 40));

        deleteButton = ThemeManager.createDeletionButton("Deactivate");
        deleteButton.setPreferredSize(new Dimension(120, 40));

        controlPanel.add(searchField);
        controlPanel.add(addButton);
        controlPanel.add(editButton);
        controlPanel.add(deleteButton);
        headerPanel.add(controlPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // --- CENTER: Data Table ---
        String[] columns = {"ID", "Full Name", "Username", "Role", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        staffTable = new JTable(tableModel);
        styleTable(staffTable);

        JScrollPane scrollPane = new JScrollPane(staffTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER, 1));
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
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

        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected) {
                    String status = value != null ? value.toString() : "";
                    if (status.equalsIgnoreCase("ACTIVE")) {
                        c.setForeground(ThemeManager.SUCCESS);
                    } else if (status.equalsIgnoreCase("INACTIVE")) {
                        c.setForeground(ThemeManager.DELETE);
                    } else {
                        c.setForeground(ThemeManager.TEXT_PRIMARY);
                    }
                }
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 4) { // Status column
                table.getColumnModel().getColumn(i).setCellRenderer(statusRenderer);
            } else if (i != 1) { // Center everything except Full Name
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
        
        // Hide ID column ID logic but keep it for reference
        table.getColumnModel().getColumn(0).setMinWidth(60);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
    }

    /**
     * Shows a modal dialog to collect User data. 
     * @param owner The parent frame
     * @param user The user to edit, or null to create a new user.
     * @return A populated User object, or null if cancelled.
     */
    public User showStaffFormDialog(JFrame owner, User user) {
        boolean isEdit = (user != null);
        
        JTextField fullNameField = new JTextField(isEdit ? user.getFullName() : "");
        JTextField usernameField = new JTextField(isEdit ? user.getUsername() : "");
        JPasswordField passwordField = new JPasswordField(isEdit ? user.getPassword() : "");
        
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"TICKET_STAFF", "ADMIN"});
        if (isEdit) roleCombo.setSelectedItem(user.getRole());
        
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        if (isEdit) statusCombo.setSelectedItem(user.getStatus());

        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 10));
        panel.setBackground(Color.WHITE);
        panel.add(new JLabel("Full Name:"));
        panel.add(fullNameField);
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Role:"));
        panel.add(roleCombo);
        panel.add(new JLabel("Status:"));
        panel.add(statusCombo);

        int result = JOptionPane.showConfirmDialog(owner, panel, 
            isEdit ? "Edit Staff Account" : "Add New Staff Account",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            User resultUser = new User();
            if (isEdit) resultUser.setId(user.getId());
            resultUser.setFullName(fullNameField.getText().trim());
            resultUser.setUsername(usernameField.getText().trim());
            resultUser.setPassword(new String(passwordField.getPassword()));
            resultUser.setRole(roleCombo.getSelectedItem().toString());
            resultUser.setStatus(statusCombo.getSelectedItem().toString());
            
            if (resultUser.getFullName().isEmpty() || resultUser.getUsername().isEmpty() || resultUser.getPassword().isEmpty()) {
                JOptionPane.showMessageDialog(owner, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            return resultUser;
        }
        return null; // Cancelled
    }

    // --- Expose Elements for Controller ---
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTable getStaffTable() { return staffTable; }
    public JButton getAddButton() { return addButton; }
    public JButton getEditButton() { return editButton; }
    public JButton getDeleteButton() { return deleteButton; }
    public JTextField getSearchField() { return searchField; }
}