package dao;

import entity.HappenStatus;
import utils.DatabaseConnection;
import Enum.StatusCode;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HappenStatusDAO implements DAOInterface<HappenStatus> {

    @Override
    public int insert(HappenStatus t) {
        String sql = "INSERT INTO HappenStatus (UN_StatusCode, StatusName) VALUES (?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, t.getStatusCode().ordinal());
            pstmt.setString(2, t.getStatusName());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        t.setHappenStatusID(generatedKeys.getInt(1));
                        return affectedRows;
                    }
                }
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("Error inserting HappenStatus: " + e.getMessage());
            return -1; // Return -1 to indicate error
        }
    }

    @Override
    public int update(HappenStatus t) {
        String sql = "UPDATE HappenStatus SET UN_StatusCode = ?, StatusName = ? WHERE HappenStatusID = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setInt(1, t.getStatusCode().ordinal());
            pstmt.setString(2, t.getStatusName());
            pstmt.setInt(3, t.getHappenStatusID());
            
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating HappenStatus ID " + t.getHappenStatusID() + ": " + e.getMessage());
            return -1;
        }
    }

    @Override
    public int delete(HappenStatus t) {
        String sql = "DELETE FROM HappenStatus WHERE HappenStatusID = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setInt(1, t.getHappenStatusID());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting HappenStatus ID " + t.getHappenStatusID() + ": " + e.getMessage());
            return -1;
        }
    }

    @Override
    public List<HappenStatus> selectAll() {
        List<HappenStatus> list = new ArrayList<>();
        String sql = "SELECT HappenStatusID, UN_StatusCode, StatusName FROM HappenStatus";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                HappenStatus status = new HappenStatus();
                status.setHappenStatusID(rs.getInt("HappenStatusID"));
                
                int statusCodeValue = rs.getInt("UN_StatusCode");
                StatusCode statusCode = StatusCode.values()[statusCodeValue];
                status.setStatusCode(statusCode);
                
                status.setStatusName(rs.getString("StatusName"));
                list.add(status);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all HappenStatus: " + e.getMessage());
        }
        return list;
    }

    @Override
    public HappenStatus selectById(HappenStatus t) {
        return selectById(t.getHappenStatusID());
    }

    public HappenStatus selectById(int id) {
        HappenStatus happenStatus = null;
        String sql = "SELECT HappenStatusID, UN_StatusCode, StatusName FROM HappenStatus WHERE HappenStatusID = ?";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    happenStatus = new HappenStatus();
                    happenStatus.setHappenStatusID(rs.getInt("HappenStatusID"));
                    
                    int statusCodeValue = rs.getInt("UN_StatusCode");
                    StatusCode statusCode = StatusCode.values()[statusCodeValue];
                    happenStatus.setStatusCode(statusCode);
                    
                    happenStatus.setStatusName(rs.getString("StatusName"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving HappenStatus with ID " + id + ": " + e.getMessage());
        }
        return happenStatus;
    }

    @Override
    public List<HappenStatus> selectByCondition(String condition, Object... params) {
        List<HappenStatus> list = new ArrayList<>();
        String sql = "SELECT HappenStatusID, UN_StatusCode, StatusName FROM HappenStatus";
        
        if (condition != null && !condition.trim().isEmpty()) {
            sql += " WHERE " + condition;
        }

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    HappenStatus status = new HappenStatus();
                    status.setHappenStatusID(rs.getInt("HappenStatusID"));
                    
                    int statusCodeValue = rs.getInt("UN_StatusCode");
                    StatusCode statusCode = StatusCode.values()[statusCodeValue];
                    status.setStatusCode(statusCode);
                    
                    status.setStatusName(rs.getString("StatusName"));
                    list.add(status);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving HappenStatus by condition: " + e.getMessage());
        }
        return list;
    }

    // Additional utility methods
    public HappenStatus findByStatusCode(StatusCode statusCode) {
        List<HappenStatus> result = selectByCondition("UN_StatusCode = ?", statusCode.ordinal());
        return result.isEmpty() ? null : result.get(0);
    }
}