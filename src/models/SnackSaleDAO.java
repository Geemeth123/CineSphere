/**
 * Snack Sale Data Access Object (SnackSaleDAO)
 * 
 * Responsibility:
 * 1. Executes atomic database transactions for POS snack purchases (inserts sale header, line items, and deducts inventory stock).
 * 2. Provides query methods for reporting (sales history, line items, date range filtering, best-selling snack calculation).
 */
package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import utils.DBUtils;

public class SnackSaleDAO {

    private static boolean schemaInitialized = false;

    public SnackSaleDAO() {
        if (!schemaInitialized) {
            initializeSchema();
            schemaInitialized = true;
        }
    }

    /**
     * Ensures `seat_number` column exists in `snack_sales` table schema.
     */
    private void initializeSchema() {
        try (Connection conn = DBUtils.getConnection();
             Statement stmt = conn.createStatement()) {
            try {
                stmt.execute("ALTER TABLE snack_sales ADD COLUMN seat_number VARCHAR(10) DEFAULT NULL AFTER booking_id");
            } catch (SQLException e) {
                // Ignore if column already exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Executes an atomic database transaction for POS snack sales.
     * Actions in single transaction:
     * 1. Inserts parent record into `snack_sales`.
     * 2. Inserts batch line item records into `snack_sale_items`.
     * 3. Deducts purchased quantities from `snacks` stock (with stock >= quantity guard).
     * Rollbacks automatically if any step or stock guard fails.
     */
    public boolean createSale(SnackSale sale, List<SnackSaleItem> items) {
        String insertSaleQuery = "INSERT INTO snack_sales (booking_id, seat_number, user_id, total_amount) VALUES (?, ?, ?, ?)";
        String insertItemQuery = "INSERT INTO snack_sale_items (snack_sale_id, snack_id, quantity, price_at_sale, discount_applied) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBUtils.getConnection();
            conn.setAutoCommit(false); // Disable auto-commit to start atomic transaction

            // Step 1: Insert parent sale record
            int saleId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(insertSaleQuery, Statement.RETURN_GENERATED_KEYS)) {
                if (sale.getBookingId() != null) {
                    stmt.setInt(1, sale.getBookingId());
                } else {
                    stmt.setNull(1, Types.INTEGER);
                }
                stmt.setString(2, sale.getSeatNumber());
                if (sale.getUserId() != null) {
                    stmt.setInt(3, sale.getUserId());
                } else {
                    stmt.setNull(3, Types.INTEGER);
                }
                stmt.setBigDecimal(4, sale.getTotalAmount());
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        saleId = rs.getInt(1);
                        sale.setId(saleId);
                        sale.setSaleTime(new java.sql.Timestamp(System.currentTimeMillis()));
                    }
                }
            }

            if (saleId == -1) {
                conn.rollback();
                return false;
            }

            // Step 2 & 3: Insert items and update stock concurrently with stock availability guard
            String guardedStockQuery = "UPDATE snacks SET quantity = quantity - ? WHERE id = ? AND quantity >= ?";
            try (PreparedStatement itemStmt = conn.prepareStatement(insertItemQuery);
                 PreparedStatement stockStmt = conn.prepareStatement(guardedStockQuery)) {

                for (SnackSaleItem item : items) {
                    // Insert Line Item
                    itemStmt.setInt(1, saleId);
                    itemStmt.setInt(2, item.getSnackId());
                    itemStmt.setInt(3, item.getQuantity());
                    itemStmt.setBigDecimal(4, item.getPriceAtSale());
                    itemStmt.setBigDecimal(5, item.getDiscountApplied());
                    itemStmt.addBatch();

                    // Deduct inventory stock
                    stockStmt.setInt(1, item.getQuantity());
                    stockStmt.setInt(2, item.getSnackId());
                    stockStmt.setInt(3, item.getQuantity());
                    stockStmt.addBatch();
                }

                itemStmt.executeBatch();
                int[] stockResults = stockStmt.executeBatch();

                // Validate stock update results (rollback if stock was insufficient)
                for (int result : stockResults) {
                    if (result == 0) {
                        conn.rollback();
                        return false;
                    }
                }
            }

            conn.commit(); // Commit transaction if all operations succeed
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Retrieves all completed sales headers sorted descending by sale time.
     */
    public List<SnackSale> getAllSales() {
        List<SnackSale> sales = new ArrayList<>();
        String query = "SELECT s.*, u.username as cashier_name FROM snack_sales s LEFT JOIN users u ON s.user_id = u.id ORDER BY s.sale_time DESC";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
             
             while (rs.next()) {
                 SnackSale sale = new SnackSale(
                     rs.getInt("id"),
                     (Integer) rs.getObject("booking_id"),
                     rs.getString("seat_number"),
                     (Integer) rs.getObject("user_id"),
                     rs.getBigDecimal("total_amount"),
                     rs.getTimestamp("sale_time")
                 );
                 sale.setCashierName(rs.getString("cashier_name"));
                 sales.add(sale);
             }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }

    /**
     * Retrieves completed sales headers for a specific date.
     */
    public List<SnackSale> getSalesByDate(java.time.LocalDate date) {
        List<SnackSale> sales = new ArrayList<>();
        String query = "SELECT s.*, u.username as cashier_name FROM snack_sales s LEFT JOIN users u ON s.user_id = u.id WHERE DATE(s.sale_time) = ? ORDER BY s.sale_time DESC";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
             
             stmt.setDate(1, java.sql.Date.valueOf(date));
             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                     SnackSale sale = new SnackSale(
                         rs.getInt("id"),
                         (Integer) rs.getObject("booking_id"),
                         rs.getString("seat_number"),
                         (Integer) rs.getObject("user_id"),
                         rs.getBigDecimal("total_amount"),
                         rs.getTimestamp("sale_time")
                     );
                     sale.setCashierName(rs.getString("cashier_name"));
                     sales.add(sale);
                 }
             }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }

    /**
     * Retrieves purchased line item records for a given sale ID.
     */
    public List<SnackSaleItem> getItemsForSale(int saleId) {
        List<SnackSaleItem> items = new ArrayList<>();
        String query = "SELECT si.*, COALESCE(s.name, 'Deleted Item') as snack_name FROM snack_sale_items si LEFT JOIN snacks s ON si.snack_id = s.id WHERE si.snack_sale_id = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

             stmt.setInt(1, saleId);
             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                     SnackSaleItem item = new SnackSaleItem(
                         rs.getInt("id"),
                         rs.getInt("snack_sale_id"),
                         rs.getInt("snack_id"),
                         rs.getInt("quantity"),
                         rs.getBigDecimal("price_at_sale"),
                         rs.getBigDecimal("discount_applied")
                     );
                     item.setSnackName(rs.getString("snack_name"));
                     items.add(item);
                 }
             }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * Retrieves sales headers within a start and end date range.
     */
    public List<SnackSale> getSalesByDateRange(java.time.LocalDate start, java.time.LocalDate end) {
        List<SnackSale> sales = new ArrayList<>();
        String query = "SELECT s.*, u.username as cashier_name FROM snack_sales s LEFT JOIN users u ON s.user_id = u.id WHERE DATE(s.sale_time) >= ? AND DATE(s.sale_time) <= ? ORDER BY s.sale_time DESC";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
              
              stmt.setDate(1, java.sql.Date.valueOf(start));
              stmt.setDate(2, java.sql.Date.valueOf(end));
              try (ResultSet rs = stmt.executeQuery()) {
                  while (rs.next()) {
                      SnackSale sale = new SnackSale(
                          rs.getInt("id"),
                          (Integer) rs.getObject("booking_id"),
                          rs.getString("seat_number"),
                          (Integer) rs.getObject("user_id"),
                          rs.getBigDecimal("total_amount"),
                          rs.getTimestamp("sale_time")
                      );
                      sale.setCashierName(rs.getString("cashier_name"));
                      sales.add(sale);
                  }
              }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }

    /**
     * Calculates and returns the name of the top selling snack by total volume in specified date range.
     */
    public String getBestSellingSnack(java.time.LocalDate start, java.time.LocalDate end) {
        String query = "SELECT s.name, SUM(si.quantity) as total_qty " +
                       "FROM snack_sale_items si " +
                       "JOIN snacks s ON si.snack_id = s.id " +
                       "JOIN snack_sales ss ON si.snack_sale_id = ss.id ";
        
        if (start != null && end != null) {
            query += "WHERE DATE(ss.sale_time) >= ? AND DATE(ss.sale_time) <= ? ";
        } else if (start != null) {
            query += "WHERE DATE(ss.sale_time) = ? ";
        }
        
        query += "GROUP BY s.id ORDER BY total_qty DESC LIMIT 1";

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
             
             if (start != null && end != null) {
                 stmt.setDate(1, java.sql.Date.valueOf(start));
                 stmt.setDate(2, java.sql.Date.valueOf(end));
             } else if (start != null) {
                 stmt.setDate(1, java.sql.Date.valueOf(start));
             }

             try (ResultSet rs = stmt.executeQuery()) {
                 if (rs.next()) {
                     return rs.getString("name");
                 }
             }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }
}

