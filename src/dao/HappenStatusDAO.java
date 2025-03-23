package dao;

import entity.HappenStatus;
import Enum.StatusCode;
import database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HappenStatusDAO implements DAOInterface<HappenStatus> {

    @Override
    public int insert(HappenStatus t) {
        String sql = "INSERT INTO HappenStatus (statusCode, statusName) VALUES (?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, t.getStatusCode().ordinal());
            pstmt.setString(2, t.getStatusName());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        t.setHappenStatusID(generatedKeys.getInt(1));
                    }
                }
            }
            return affectedRows;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int update(HappenStatus t) {
        String sql = "UPDATE HappenStatus SET statusCode = ?, statusName = ? WHERE happenStatusID = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setInt(1, t.getStatusCode().ordinal());
            pstmt.setString(2, t.getStatusName());
            pstmt.setInt(3, t.getHappenStatusID());
            
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int delete(HappenStatus t) {
        String sql = "DELETE FROM HappenStatus WHERE happenStatusID = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setInt(1, t.getHappenStatusID());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<HappenStatus> selectAll() {
        List<HappenStatus> list = new ArrayList<>();
        String sql = "SELECT * FROM HappenStatus";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                list.add(new HappenStatus(
                    rs.getInt("happenStatusID"),
                    StatusCode.values()[rs.getInt("statusCode")],
                    rs.getString("statusName")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public HappenStatus selectById(HappenStatus t) {
        return selectById(t.getHappenStatusID());
    }

    public HappenStatus selectById(int id) {
        HappenStatus happenStatus = null;
        String sql = "SELECT * FROM HappenStatus WHERE happenStatusID = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                happenStatus = new HappenStatus(
                    rs.getInt("happenStatusID"),
                    StatusCode.values()[rs.getInt("statusCode")],
                    rs.getString("statusName")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return happenStatus;
    }

    @Override
    public List<HappenStatus> selectByCondition(String condition, Object... params) {
        List<HappenStatus> list = new ArrayList<>();
        String sql = "SELECT * FROM HappenStatus WHERE " + condition;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            // Set tham số vào PreparedStatement
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int statusCodeIndex = rs.getInt("statusCode");
                    
                    // Kiểm tra tránh lỗi ArrayIndexOutOfBoundsException
                    StatusCode statusCode = (statusCodeIndex >= 0 && statusCodeIndex < StatusCode.values().length) 
                        ? StatusCode.values()[statusCodeIndex] 
                        : null; // Hoặc có thể throw exception tùy yêu cầu

                    list.add(new HappenStatus(
                        rs.getInt("happenStatusID"),
                        statusCode,
                        rs.getString("statusName")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn HappenStatus: " + e.getMessage());
        }
        return list;
    }

}