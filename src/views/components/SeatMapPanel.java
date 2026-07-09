package views.components;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import utils.ThemeManager;

public class SeatMapPanel extends JPanel {
    
    private final List<String> selectedSeats = new ArrayList<>();
    private final Map<String, RoundedPanel> seatPanels = new HashMap<>();
    
    private int adultCount = 0;
    private int kidsCount = 0;
    
    private final double adultPrice = 15.00;
    private final double kidsPrice = 10.00;

    private JLabel headerMovieTitle;
    private JLabel headerShowTime;
    
    private JTextArea selectedSeatsArea;
    private JTextField adultCountField;
    private JTextField kidsCountField;
    private JLabel totalPriceLabel;
    
    private JButton checkoutButton;
    private JButton backButton;
    private JButton clearSeatsButton;

    private final Set<String> booked = Set.of("0-2","0-3","0-4", "2-5","2-6", "6-7","6-8");
    private final Set<String> unavailable = Set.of("3-9", "5-0");

    public SeatMapPanel() {
        setLayout(new BorderLayout(30, 0));
        setBackground(ThemeManager.BACKGROUND);
        setBorder(new EmptyBorder(30, 50, 40, 50));

        JPanel topNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topNav.setBackground(ThemeManager.BACKGROUND);
        backButton = ThemeManager.createSecondaryButton("\u2190 Back to selecting movies");
        backButton.setPreferredSize(new Dimension(240, 40));
        topNav.add(backButton);
        add(topNav, BorderLayout.NORTH);

        add(createSeatGridPanel(), BorderLayout.CENTER);
        add(createRightPanel(), BorderLayout.EAST);
        
        updateSummary();
    }

    private JPanel createSeatGridPanel() {
        JPanel leftContainer = new JPanel();
        leftContainer.setLayout(new BoxLayout(leftContainer, BoxLayout.Y_AXIS));
        leftContainer.setBackground(ThemeManager.BACKGROUND);
        leftContainer.setBorder(new EmptyBorder(20, 0, 0, 0));

        RoundedPanel seatContainer = new RoundedPanel(16, ThemeManager.BORDER);
        seatContainer.setLayout(new BorderLayout());
        seatContainer.setBackground(ThemeManager.SURFACE);
        seatContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel screenPanel = new JPanel(new BorderLayout());
        screenPanel.setBackground(ThemeManager.SURFACE);
        JLabel screenLabel = new JLabel("SCREEN", SwingConstants.CENTER);
        screenLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        screenLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        screenLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, ThemeManager.BORDER));
        screenPanel.add(screenLabel, BorderLayout.CENTER);
        screenPanel.setBorder(new EmptyBorder(0, 40, 20, 40));
        seatContainer.add(screenPanel, BorderLayout.NORTH);

        JPanel seatGrid = new JPanel(new GridLayout(8, 10, 8, 8));
        seatGrid.setBackground(ThemeManager.SURFACE);
        seatGrid.setBorder(new EmptyBorder(10, 30, 10, 30));

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 10; col++) {
                String seatId = (char)(65 + row) + String.valueOf(col + 1);
                String gridKey = row + "-" + col;

                RoundedPanel seat = new RoundedPanel(8, ThemeManager.BORDER);
                seat.setPreferredSize(new Dimension(38, 38));
                seatPanels.put(seatId, seat);

                if (booked.contains(gridKey)) {
                    seat.setBackground(ThemeManager.TEXT_SECONDARY);
                    seat.setBorderColor(ThemeManager.TEXT_SECONDARY);
                    seat.setToolTipText("Booked");
                } else if (unavailable.contains(gridKey)) {
                    seat.setBackground(ThemeManager.BORDER);
                    seat.setBorderColor(ThemeManager.BORDER);
                    seat.setToolTipText("Unavailable");
                } else {
                    seat.setBackground(Color.WHITE);
                    seat.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    seat.setToolTipText("Available - " + seatId);
                    
                    seat.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) { toggleSeat(seatId, seat); }
                    });
                }
                seatGrid.add(seat);
            }
        }
        seatContainer.add(seatGrid, BorderLayout.CENTER);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        legend.setBackground(ThemeManager.SURFACE);
        legend.add(createLegendItem(Color.WHITE, ThemeManager.BORDER, "Available"));
        legend.add(createLegendItem(ThemeManager.PRIMARY, ThemeManager.PRIMARY, "Selected"));
        legend.add(createLegendItem(ThemeManager.TEXT_SECONDARY, ThemeManager.TEXT_SECONDARY, "Booked"));
        seatContainer.add(legend, BorderLayout.SOUTH);

        leftContainer.add(seatContainer);
        return leftContainer;
    }

    private JPanel createRightPanel() {
        JPanel rightContainer = new JPanel();
        rightContainer.setLayout(new BoxLayout(rightContainer, BoxLayout.Y_AXIS));
        rightContainer.setBackground(ThemeManager.BACKGROUND);
        rightContainer.setPreferredSize(new Dimension(380, 0));
        rightContainer.setBorder(new EmptyBorder(20, 0, 0, 0));

        headerMovieTitle = new JLabel("Select a Movie");
        headerMovieTitle.setFont(ThemeManager.FONT_HEADING);
        headerMovieTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        headerMovieTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        headerShowTime = new JLabel("Time • Hall");
        headerShowTime.setFont(ThemeManager.FONT_BODY);
        headerShowTime.setForeground(ThemeManager.TEXT_SECONDARY);
        headerShowTime.setAlignmentX(Component.LEFT_ALIGNMENT);

        RoundedPanel configBox = new RoundedPanel(16, ThemeManager.BORDER);
        configBox.setLayout(new BoxLayout(configBox, BoxLayout.Y_AXIS));
        configBox.setBackground(Color.WHITE);
        configBox.setBorder(new EmptyBorder(25, 25, 25, 25));
        configBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel seatHeaderPanel = new JPanel(new BorderLayout());
        seatHeaderPanel.setBackground(Color.WHITE);
        seatHeaderPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        seatHeaderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        JLabel seatsHeader = new JLabel("Selected Seats");
        seatsHeader.setFont(ThemeManager.FONT_SMALL);
        seatsHeader.setForeground(ThemeManager.TEXT_SECONDARY);
        
        clearSeatsButton = ThemeManager.createDeletionButton("Clear All");
        clearSeatsButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        clearSeatsButton.setPreferredSize(new Dimension(90, 28));
        clearSeatsButton.addActionListener(e -> clearSeats());

        seatHeaderPanel.add(seatsHeader, BorderLayout.WEST);
        seatHeaderPanel.add(clearSeatsButton, BorderLayout.EAST);
        
        selectedSeatsArea = new JTextArea("-");
        selectedSeatsArea.setFont(new Font("Segoe UI", Font.BOLD, 14));
        selectedSeatsArea.setForeground(ThemeManager.TEXT_PRIMARY);
        selectedSeatsArea.setLineWrap(true);
        selectedSeatsArea.setWrapStyleWord(true);
        selectedSeatsArea.setEditable(false);
        selectedSeatsArea.setOpaque(false);
        selectedSeatsArea.setBorder(new EmptyBorder(5, 0, 5, 0));

        JScrollPane seatsScrollPane = new JScrollPane(selectedSeatsArea);
        seatsScrollPane.setBorder(null);
        seatsScrollPane.setOpaque(false);
        seatsScrollPane.getViewport().setOpaque(false);
        seatsScrollPane.setPreferredSize(new Dimension(300, 60));
        seatsScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        seatsScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        adultCountField = createTypeableField();
        kidsCountField = createTypeableField();

        JPanel adultPanel = createCounterRow("Adult", "$15.00", adultCountField, true);
        adultPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        adultPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        
        JPanel kidsPanel = createCounterRow("Child", "$10.00", kidsCountField, false);
        kidsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        kidsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBackground(Color.WHITE);
        totalPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeManager.BORDER));
        totalPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        totalPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        JLabel totalText = new JLabel("Total Amount");
        totalText.setFont(ThemeManager.FONT_BODY);
        totalText.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        totalPriceLabel = new JLabel("$0.00");
        totalPriceLabel.setFont(ThemeManager.FONT_HEADING);
        totalPriceLabel.setForeground(ThemeManager.PRIMARY);
        totalPriceLabel.setBorder(new EmptyBorder(15, 0, 0, 0));

        totalPanel.add(totalText, BorderLayout.WEST);
        totalPanel.add(totalPriceLabel, BorderLayout.EAST);

        checkoutButton = ThemeManager.createPrimaryButton("Proceed to Book");
        checkoutButton.setPreferredSize(new Dimension(Integer.MAX_VALUE, 45));
        checkoutButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        checkoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        configBox.add(seatHeaderPanel);
        configBox.add(Box.createVerticalStrut(4));
        configBox.add(seatsScrollPane);
        configBox.add(Box.createVerticalStrut(15));
        configBox.add(adultPanel);
        configBox.add(Box.createVerticalStrut(15));
        configBox.add(kidsPanel);
        configBox.add(Box.createVerticalStrut(25));
        configBox.add(totalPanel);
        
        rightContainer.add(headerMovieTitle);
        rightContainer.add(Box.createVerticalStrut(5));
        rightContainer.add(headerShowTime);
        rightContainer.add(Box.createVerticalStrut(20));
        rightContainer.add(configBox);
        rightContainer.add(Box.createVerticalStrut(20));
        rightContainer.add(checkoutButton);

        return rightContainer;
    }

    private JTextField createTypeableField() {
        JTextField field = new JTextField("0");
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setPreferredSize(new Dimension(45, 32));
        field.setFont(new Font("Segoe UI", Font.BOLD, 14));
        field.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER));
        
        // if hit enter trigger 
        field.addActionListener(e -> recalculateLimits());
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) { recalculateLimits(); }
        });
        
        return field;
    }

    private JPanel createCounterRow(String type, String price, JTextField countField, boolean isAdult) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);

        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setBackground(Color.WHITE);
        JLabel typeLbl = new JLabel(type);
        typeLbl.setFont(ThemeManager.FONT_BODY);
        JLabel priceLbl = new JLabel(price);
        priceLbl.setFont(ThemeManager.FONT_SMALL);
        priceLbl.setForeground(ThemeManager.TEXT_SECONDARY);
        info.add(typeLbl);
        info.add(priceLbl);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        controls.setBackground(Color.WHITE);

        JButton minusBtn = createCircleButton("-");
        JButton plusBtn = createCircleButton("+");

        minusBtn.addActionListener(e -> {
            int val = parseField(countField);
            if (val > 0) {
                countField.setText(String.valueOf(val - 1));
                recalculateLimits();
            }
        });

        plusBtn.addActionListener(e -> {
            int val = parseField(countField);
            int otherVal = parseField(isAdult ? kidsCountField : adultCountField);
            if (val + otherVal < selectedSeats.size()) {
                countField.setText(String.valueOf(val + 1));
                recalculateLimits();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Cannot exceed the number of selected seats (" + selectedSeats.size() + ").", 
                    "Limit Reached", JOptionPane.WARNING_MESSAGE);
            }
        });

        controls.add(minusBtn);
        controls.add(countField);
        controls.add(plusBtn);

        row.add(info, BorderLayout.WEST);
        row.add(controls, BorderLayout.EAST);
        return row;
    }

    private int parseField(JTextField field) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void recalculateLimits() {
        int a = parseField(adultCountField);
        int k = parseField(kidsCountField);
        int max = selectedSeats.size();

        if (a < 0) a = 0;
        if (k < 0) k = 0;

        // bound limits - strict
        if (a + k > max) {
            if (a > max) {
                a = max;
                k = 0;
            } else {
                k = max - a;
            }
            JOptionPane.showMessageDialog(this, 
                "Quantities adjusted. You cannot select more tickets than seats selected (" + max + ").", 
                "Limit Reached", JOptionPane.INFORMATION_MESSAGE);
        }

        adultCount = a;
        kidsCount = k;

        //reset ui n
        adultCountField.setText(String.valueOf(a));
        kidsCountField.setText(String.valueOf(k));

        updateSummary();
    }

    private JButton createCircleButton(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(32, 32));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(ThemeManager.SURFACE);
        btn.setForeground(ThemeManager.TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createLegendItem(Color color, Color border, String label) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        item.setBackground(ThemeManager.SURFACE);
        RoundedPanel colorBox = new RoundedPanel(6, border);
        colorBox.setPreferredSize(new Dimension(16, 16));
        colorBox.setBackground(color);
        item.add(colorBox);
        JLabel text = new JLabel(label);
        text.setFont(ThemeManager.FONT_SMALL);
        text.setForeground(ThemeManager.TEXT_SECONDARY);
        item.add(text);
        return item;
    }

    private void toggleSeat(String seatId, RoundedPanel seat) {
        if (selectedSeats.contains(seatId)) {
            selectedSeats.remove(seatId);
            seat.setBackground(Color.WHITE);
            seat.setBorderColor(ThemeManager.BORDER);
        } else {
            selectedSeats.add(seatId);
            seat.setBackground(ThemeManager.PRIMARY);
            seat.setBorderColor(ThemeManager.PRIMARY);
            
            // auto adult
            if (adultCount + kidsCount < selectedSeats.size()) {
                adultCountField.setText(String.valueOf(adultCount + 1));
            }
        }
        recalculateLimits();
    }

    private void clearSeats() {
        selectedSeats.clear();
        adultCountField.setText("0");
        kidsCountField.setText("0");
        
        for (Map.Entry<String, RoundedPanel> entry : seatPanels.entrySet()) {
            RoundedPanel seat = entry.getValue();
            if (seat.getBackground().equals(ThemeManager.PRIMARY)) {
                seat.setBackground(Color.WHITE);
                seat.setBorderColor(ThemeManager.BORDER);
            }
        }
        recalculateLimits();
    }

    public void reset(String movieTitle, String timeHall) {
        headerMovieTitle.setText(movieTitle);
        headerShowTime.setText(timeHall);
        clearSeats();
    }

    private void updateSummary() {
        if (selectedSeats.isEmpty()) {
            selectedSeatsArea.setText("-");
        } else {
            selectedSeatsArea.setText(String.join(", ", selectedSeats));
        }

        double total = (adultCount * adultPrice) + (kidsCount * kidsPrice);
        totalPriceLabel.setText("$" + String.format("%.2f", total));

        int totalTickets = adultCount + kidsCount;
        boolean isValid = totalTickets > 0 && totalTickets == selectedSeats.size();
        
        checkoutButton.setEnabled(isValid);
        if (isValid) {
            checkoutButton.setText("Book " + totalTickets + " Tickets");
        } else if (selectedSeats.isEmpty()) {
            checkoutButton.setText("Proceed to Book");
        } else {
            checkoutButton.setText("Allocate " + (selectedSeats.size() - totalTickets) + " more ticket(s)");
        }
    }
    
    public JButton getCheckoutButton() { return checkoutButton; }
    public JButton getBackButton() { return backButton; }
    public List<String> getSelectedSeats() { return selectedSeats; }
    public int getAdultCount() { return adultCount; }
    public int getKidsCount() { return kidsCount; }
    public double getTotalAmount() { return (adultCount * adultPrice) + (kidsCount * kidsPrice); }
}