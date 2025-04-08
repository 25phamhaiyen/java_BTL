package repository;

import model.Promotion;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class PromotionRepository implements IRepository<Promotion> {

    private static PromotionRepository instance;

    public static PromotionRepository getInstance() {
        if (instance == null) {
            synchronized (PromotionRepository.class) {
                if (instance == null) {
                    instance = new PromotionRepository();
                }
            }
        }
        return instance;
    }

    @Override
    public int insert(Promotion promotion) {
        String sql = "INSERT INTO promotion (name, description, requiredPoints, discountPercent, startDate, endDate) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, promotion.getName());
            pstmt.setString(2, promotion.getDescription());
            pstmt.setInt(3, promotion.getRequiredPoints());
            pstmt.setDouble(4, promotion.getDiscountPercent());
            pstmt.setDate(5, new java.sql.Date(promotion.getStartDate().getTime()));
            pstmt.setDate(6, new java.sql.Date(promotion.getEndDate().getTime()));

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        promotion.setPromotionID(rs.getInt(1));
                    }
                }
            }
            return affectedRows;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm khuyến mãi: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int update(Promotion promotion) {
        String sql = "UPDATE promotion SET name=?, description=?, requiredPoints=?, discountPercent=?, startDate=?, endDate=? WHERE promotionID=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, promotion.getName());
            pstmt.setString(2, promotion.getDescription());
            pstmt.setInt(3, promotion.getRequiredPoints());
            pstmt.setDouble(4, promotion.getDiscountPercent());
            pstmt.setDate(5, new java.sql.Date(promotion.getStartDate().getTime()));
            pstmt.setDate(6, new java.sql.Date(promotion.getEndDate().getTime()));

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật khuyến mãi: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int delete(Promotion promotion) {
        String sql = "DELETE FROM promotion WHERE promotionID=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, promotion.getPromotionID());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa khuyến mãi: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public List<Promotion> selectAll() {
        List<Promotion> list = new ArrayList<>();
        String sql = "SELECT * FROM promotion";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToPromotion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách khuyến mãi: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Promotion selectById(Promotion promotion) {
        return selectById(promotion.getPromotionID());
    }

    public Promotion selectById(int promotionID) {
        String sql = "SELECT * FROM promotion WHERE promotionID=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, promotionID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPromotion(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm khuyến mãi theo ID: " + e.getMessage());
        }
        return null;
    }

    private Promotion mapResultSetToPromotion(ResultSet rs) throws SQLException {
        return new Promotion(
            rs.getInt("promotionID"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getInt("requiredPoints"),
            rs.getDouble("discountPercent"),
            rs.getDate("startDate"), 
            rs.getDate("endDate")
        );
    }


    @Override
    public List<Promotion> selectByCondition(String condition, Object... params) {
        List<Promotion> list = new ArrayList<>();
        String sql = "SELECT * FROM promotion WHERE " + condition;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPromotion(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm khuyến mãi theo điều kiện: " + e.getMessage());
        }
        return list;
    }

}
