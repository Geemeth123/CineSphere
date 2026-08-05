/**
 * managing database operations for the PromoCode entity.
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

public class PromoCodeDAO {

    public PromoCodeDAO() {
    }

    public List<PromoCode> getAllPromoCodes() {
        List<PromoCode> codes = new ArrayList<>();
        String sql = "SELECT * FROM promo_codes ORDER BY created_at DESC";
        try (Connection conn = DBUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                codes.add(new PromoCode(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getBigDecimal("discount_percentage"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return codes;
    }

    public boolean addPromoCode(PromoCode promoCode) {
        String sql = "INSERT INTO promo_codes (code, discount_percentage, status) VALUES (?, ?, ?)";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, promoCode.getCode().toUpperCase().trim());
            stmt.setBigDecimal(2, promoCode.getDiscountPercentage());
            stmt.setString(3, promoCode.getStatus());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePromoCode(PromoCode promoCode) {
        String sql = "UPDATE promo_codes SET code = ?, discount_percentage = ?, status = ? WHERE id = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, promoCode.getCode().toUpperCase().trim());
            stmt.setBigDecimal(2, promoCode.getDiscountPercentage());
            stmt.setString(3, promoCode.getStatus());
            stmt.setInt(4, promoCode.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deletePromoCode(int id) {
        String sql = "DELETE FROM promo_codes WHERE id = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public PromoCode getPromoCode(String code) {
        String sql = "SELECT * FROM promo_codes WHERE code = ? AND status = 'ACTIVE'";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code.toUpperCase().trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new PromoCode(
                            rs.getInt("id"),
                            rs.getString("code"),
                            rs.getBigDecimal("discount_percentage"),
                            rs.getString("status"),
                            rs.getTimestamp("created_at")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

