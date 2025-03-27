package dao;

import database.DatabaseConnection;
import entity.Service;
import Enum.TypeServiceEnum;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO implements DAOInterface<Service> {
    
    public static ServiceDAO getInstance() {
        return new ServiceDAO();
    }

    @Override
    public int insert(Service service) {
        String sql = "INSERT INTO service (serviceName, CostPrice, TypeServiceID, MoTa) VALUES (?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, service.getServiceName());
            pstmt.setDouble(2, service.getCostPrice());
            pstmt.setInt(3, service.getTypeService().getId());
            pstmt.setString(4, service.getDescription());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        service.setServiceID(rs.getInt(1));
                    }
                }
            }
            return affectedRows;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm dịch vụ: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int update(Service service) {
        String sql = "UPDATE service SET serviceName=?, CostPrice=?, TypeServiceID=?, MoTa=? WHERE serviceID=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, service.getServiceName());
            pstmt.setDouble(2, service.getCostPrice());
            pstmt.setInt(3, service.getTypeService().getId());
            pstmt.setString(4, service.getDescription());
            pstmt.setInt(5, service.getServiceID());
            
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật dịch vụ: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int delete(Service service) {
        String sql = "DELETE FROM service WHERE serviceID=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setInt(1, service.getServiceID());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa dịch vụ: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public List<Service> selectAll() {
        List<Service> list = new ArrayList<>();
        String sql = "SELECT * FROM service";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapResultSetToService(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách dịch vụ: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Service selectById(Service service) {
        return selectById(service.getServiceID());
    }
    
    public Service selectById(int serviceID) {
        String sql = "SELECT * FROM service WHERE serviceID = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setInt(1, serviceID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToService(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm dịch vụ theo ID: " + e.getMessage());
        }
        return null;
    }
    

    @Override
    public List<Service> selectByCondition(String condition, Object... params) {
        List<Service> list = new ArrayList<>();
        String sql = "SELECT * FROM service WHERE " + condition;
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToService(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm kiếm dịch vụ theo điều kiện: " + e.getMessage());
        }
        return list;
    }

    private Service mapResultSetToService(ResultSet rs) throws SQLException {
        int serviceID = rs.getInt("serviceID");
        String serviceName = rs.getString("serviceName");
        double costPrice = rs.getDouble("CostPrice");
        int typeServiceID = rs.getInt("TypeServiceID");
        String description = rs.getString("MoTa");

        TypeServiceEnum typeService = TypeServiceEnum.fromId(typeServiceID);
        return new Service(serviceID, serviceName, costPrice, typeService, description);
    }

}