/**
 * Snack Data Access Object (SnackDAO)
 * 
 * Responsibility:
 * Executes SQL queries against the `snacks` database table.
 * Supports CRUD operations (Create, Read, Update, Delete) and stock quantity adjustments.
 */
package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import utils.DBUtils;

public class SnackDAO {

    /**
     * Retrieves all snack records from the database regardless of status or stock.
     */
    public List<Snack> getAllSnacks() {
        List<Snack> snacks = new ArrayList<>();
        String query = "SELECT * FROM snacks";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
             
             while (rs.next()) {
                 snacks.add(mapResultSetToSnack(rs));
             }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return snacks;
    }

    /**
     * Retrieves active snacks with positive stock (quantity > 0) for display in POS view.
     */
    public List<Snack> getActiveSnacks() {
        List<Snack> snacks = new ArrayList<>();
        String query = "SELECT * FROM snacks WHERE status = 'ACTIVE' AND quantity > 0";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
             
             while (rs.next()) {
                 snacks.add(mapResultSetToSnack(rs));
             }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return snacks;
    }

    /**
     * Inserts a new snack item into the database and assigns auto-generated primary key ID.
     */
    public boolean addSnack(Snack snack) {
        String query = "INSERT INTO snacks (name, description, price, cost_price, quantity, min_stock, category, status, image_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
             
            stmt.setString(1, snack.getName());
            stmt.setString(2, snack.getDescription());
            stmt.setBigDecimal(3, snack.getPrice());
            stmt.setBigDecimal(4, snack.getCostPrice() != null ? snack.getCostPrice() : java.math.BigDecimal.ZERO);
            stmt.setInt(5, snack.getQuantity());
            stmt.setInt(6, snack.getMinStock());
            stmt.setString(7, snack.getCategory());
            stmt.setString(8, snack.getStatus() != null ? snack.getStatus() : "ACTIVE");
            stmt.setString(9, snack.getImagePath());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        snack.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates details (name, price, quantity, category, status, image) for an existing snack.
     */
    public boolean updateSnack(Snack snack) {
        String query = "UPDATE snacks SET name = ?, description = ?, price = ?, cost_price = ?, quantity = ?, min_stock = ?, category = ?, status = ?, image_path = ? WHERE id = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
             
            stmt.setString(1, snack.getName());
            stmt.setString(2, snack.getDescription());
            stmt.setBigDecimal(3, snack.getPrice());
            stmt.setBigDecimal(4, snack.getCostPrice() != null ? snack.getCostPrice() : java.math.BigDecimal.ZERO);
            stmt.setInt(5, snack.getQuantity());
            stmt.setInt(6, snack.getMinStock());
            stmt.setString(7, snack.getCategory());
            stmt.setString(8, snack.getStatus());
            stmt.setString(9, snack.getImagePath());
            stmt.setInt(10, snack.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Adjusts inventory stock quantity (e.g. -2 for sale, +10 for restock).
     * Prevents quantity from dropping below 0 via GREATEST(0, ...).
     */
    public boolean updateSnackQuantity(int snackId, int quantityChange) {
        String query = "UPDATE snacks SET quantity = GREATEST(0, quantity + ?) WHERE id = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, quantityChange);
            stmt.setInt(2, snackId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a snack record from the database by ID.
     */
    public boolean deleteSnack(int id) {
        String query = "DELETE FROM snacks WHERE id = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Helper method mapping database ResultSet row fields to a Snack domain object.
     */
    private Snack mapResultSetToSnack(ResultSet rs) throws SQLException {
        return new Snack(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getBigDecimal("price"),
            rs.getBigDecimal("cost_price"),
            rs.getInt("quantity"),
            rs.getInt("min_stock"),
            rs.getString("category"),
            rs.getString("status"),
            rs.getString("image_path"),
            rs.getTimestamp("created_at"),
            rs.getTimestamp("updated_at")
        );
    }
}

