package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import entity.Role;
import entity.Staff;
import Enum.GenderEnum;
import database.DatabaseConnection;

public class StaffDAO implements DAOInterface<Staff> {
    private static final Logger LOGGER = Logger.getLogger(StaffDAO.class.getName());

    @Override
    public int insert(Staff staff) {
        String sql = "INSERT INTO Staff (lastName, firstName, sex, phoneNumber, citizenNumber, address, roleID) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, staff.getLastName());
            pstmt.setString(2, staff.getFirstName());
            pstmt.setString(3, staff.getSex().name());
            pstmt.setString(4, staff.getPhoneNumber());
            pstmt.setString(5, staff.getCitizenNumber());
            pstmt.setString(6, staff.getAddress());
            pstmt.setInt(7, staff.getRole().getRoleID());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        staff.setStaffID(rs.getInt(1));
                    }
                }
            }
            return affectedRows;
        } catch (SQLException e) {
            LOGGER.severe("Lỗi khi thêm nhân viên: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int update(Staff staff) {
        String sql = "UPDATE Staff SET lastName=?, firstName=?, sex=?, phoneNumber=?, citizenNumber=?, address=?, roleID=? WHERE staffID=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, staff.getLastName());
            pstmt.setString(2, staff.getFirstName());
            pstmt.setString(3, staff.getSex().name());
            pstmt.setString(4, staff.getPhoneNumber());
            pstmt.setString(5, staff.getCitizenNumber());
            pstmt.setString(6, staff.getAddress());
            pstmt.setInt(7, staff.getRole().getRoleID());
            pstmt.setInt(8, staff.getStaffID());

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.severe("Lỗi khi cập nhật nhân viên: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int delete(Staff staff) {
        String sql = "DELETE FROM Staff WHERE staffID=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, staff.getStaffID());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.severe("Lỗi khi xóa nhân viên: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public List<Staff> selectAll() {
        String sql = "SELECT s.staffID, s.lastName, s.firstName, s.sex, s.phoneNumber, s.citizenNumber, s.address, r.roleID, r.roleName " +
                     "FROM Staff s JOIN Role r ON s.roleID = r.roleID";
        return executeQuery(sql);
    }

    @Override
    public Staff selectById(Staff staff) {
        String sql = "SELECT s.staffID, s.lastName, s.firstName, s.sex, s.phoneNumber, s.citizenNumber, s.address, r.roleID, r.roleName " +
                     "FROM Staff s JOIN Role r ON s.roleID = r.roleID WHERE s.staffID = ?";
        List<Staff> results = executeQuery(sql, staff.getStaffID());
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Staff> selectByCondition(String whereClause, Object... params) {
        String baseQuery = "SELECT s.staffID, s.lastName, s.firstName, s.sex, s.phoneNumber, s.citizenNumber, s.address, r.roleID, r.roleName " +
                           "FROM Staff s JOIN Role r ON s.roleID = r.roleID WHERE " + whereClause;
        return executeQuery(baseQuery, params);
    }

    private List<Staff> executeQuery(String sql, Object... params) {
        List<Staff> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToStaff(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Lỗi khi truy vấn dữ liệu nhân viên: " + e.getMessage());
        }
        return list;
    }

    private Staff mapResultSetToStaff(ResultSet rs) throws SQLException {
        // Lấy giới tính từ DB (kiểm tra null)
        String genderStr = rs.getString("sex");
        GenderEnum gender = GenderEnum.UNKNOWN; // Mặc định nếu không hợp lệ
        if (genderStr != null) {
            try {
                gender = GenderEnum.valueOf(genderStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Lỗi: Giới tính '" + genderStr + "' không hợp lệ. Đặt mặc định là UNKNOWN.");
            }
        }

     // Lấy thông tin Role từ DB
        int roleID = rs.getInt("roleID");
        String roleName = rs.getString("roleName");
        Role role = new Role(roleID, roleName); // Tạo đối tượng Role
        
        return new Staff(
            rs.getInt("staffID"),
            rs.getString("lastName"),
            rs.getString("firstName"),
            gender,
            rs.getString("phoneNumber"),
            rs.getString("citizenNumber"),
            rs.getString("address"),
            role
        );
    }

}