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
   

    private boolean isFieldExists(String fieldName, String value, Integer excludeId) throws SQLException {
        String sql = String.format("SELECT COUNT(*) FROM staff WHERE %s = ? AND StaffID != ?", fieldName);
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, value);
            pstmt.setInt(2, excludeId != null ? excludeId : 0);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Validate dữ liệu theo ràng buộc database
    private void validateStaff(Staff staff) throws IllegalArgumentException {
        // Validate tên
        if (staff.getLastName() == null || staff.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Họ không được để trống");
        }
        if (staff.getFirstName() == null || staff.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên không được để trống");
        }

        // Validate số điện thoại (đúng 10 số)
        if (staff.getPhoneNumber() == null || !staff.getPhoneNumber().matches("^[0-9]{10}$")) {
            throw new IllegalArgumentException("Số điện thoại phải có đúng 10 chữ số");
        }

        // Validate số CCCD (đúng 12 số)
        if (staff.getCitizenNumber() == null || !staff.getCitizenNumber().matches("^[0-9]{12}$")) {
            throw new IllegalArgumentException("Số CCCD phải có đúng 12 chữ số");
        }

        // Validate địa chỉ
        if (staff.getAddress() == null || staff.getAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ không được để trống");
        }
    }

    @Override
    public int insert(Staff staff) {
        try {
            // 1. Validate dữ liệu
            validateStaff(staff);

            // 2. Kiểm tra trùng lặp
            if (isFieldExists("phoneNumber", staff.getPhoneNumber(), null)) {
                throw new IllegalArgumentException("Số điện thoại đã tồn tại trong hệ thống");
            }
            if (isFieldExists("CitizenNumber", staff.getCitizenNumber(), null)) {
                throw new IllegalArgumentException("Số CCCD đã tồn tại trong hệ thống");
            }

            // 3. Thực hiện insert
            String sql = "INSERT INTO staff (lastName, firstName, Sex, phoneNumber, CitizenNumber, Address, Role_ID, AccountID) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (Connection con = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setString(1, staff.getLastName());
                pstmt.setString(2, staff.getFirstName());
                pstmt.setInt(3, staff.getSex().getCode());
                pstmt.setString(4, staff.getPhoneNumber());
                pstmt.setString(5, staff.getCitizenNumber());
                pstmt.setString(6, staff.getAddress());
                pstmt.setInt(7, staff.getRole().getRoleID());
                pstmt.setInt(8, staff.getAccountID());

                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            staff.setStaffID(rs.getInt(1));
                        }
                    }
                    LOGGER.info("Thêm nhân viên thành công. ID: " + staff.getStaffID());
                }
                return affectedRows;
            }
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Lỗi dữ liệu: " + e.getMessage());
            throw e;
        } catch (SQLException e) {
            LOGGER.severe("Lỗi database: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int update(Staff staff) {
        try {
            // 1. Validate dữ liệu
            validateStaff(staff);

            // 2. Kiểm tra trùng lặp (trừ bản ghi hiện tại)
            if (isFieldExists("phoneNumber", staff.getPhoneNumber(), staff.getStaffID())) {
                throw new IllegalArgumentException("Số điện thoại đã tồn tại cho nhân viên khác");
            }
            if (isFieldExists("CitizenNumber", staff.getCitizenNumber(), staff.getStaffID())) {
                throw new IllegalArgumentException("Số CCCD đã tồn tại cho nhân viên khác");
            }

            // 3. Thực hiện update
            String sql = "UPDATE staff SET lastName=?, firstName=?, Sex=?, phoneNumber=?, CitizenNumber=?, Address=?, Role_ID=?, AccountID=? "
                       + "WHERE StaffID=?";
            
            try (Connection con = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = con.prepareStatement(sql)) {

                pstmt.setString(1, staff.getLastName());
                pstmt.setString(2, staff.getFirstName());
                pstmt.setInt(3, staff.getSex().getCode());
                pstmt.setString(4, staff.getPhoneNumber());
                pstmt.setString(5, staff.getCitizenNumber());
                pstmt.setString(6, staff.getAddress());
                pstmt.setInt(7, staff.getRole().getRoleID());
                pstmt.setInt(8, staff.getAccountID());
                pstmt.setInt(9, staff.getStaffID());

                return pstmt.executeUpdate();
            }
        } catch (IllegalArgumentException | SQLException e) {
            LOGGER.severe("Lỗi khi cập nhật: " + e.getMessage());
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
        String sql = "SELECT s.staffID, s.lastName, s.firstName, s.sex, s.phoneNumber, s.citizenNumber, s.address, s.AccountID, " +
                     "r.Role_ID, r.roleName FROM Staff s JOIN Role r ON s.Role_ID = r.Role_ID";
        return executeQuery(sql);
    }

    public Staff selectById(int staffID) {
        String sql = "SELECT s.staffID, s.lastName, s.firstName, s.sex, s.phoneNumber, s.citizenNumber, s.address, s.AccountID, " +
                     "r.Role_ID, r.roleName FROM Staff s JOIN Role r ON s.Role_ID = r.Role_ID WHERE s.staffID = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, staffID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStaff(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm nhân viên theo ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Staff selectById(Staff staff) {
        return selectById(staff.getStaffID());
    }

    public List<Staff> selectByCondition(String whereClause, Object... params) {
        String baseQuery = "SELECT s.staffID, s.lastName, s.firstName, s.sex, s.phoneNumber, s.citizenNumber, s.address, s.AccountID, " +
                           "r.Role_ID, r.roleName FROM Staff s JOIN Role r ON s.Role_ID = r.Role_ID WHERE " + whereClause;
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
        int genderCode = rs.getInt("sex");
        GenderEnum gender = GenderEnum.fromCode(genderCode); // Sử dụng fromCode()

        return new Staff(
            rs.getInt("staffID"),
            rs.getString("lastName"),
            rs.getString("firstName"),
            gender,
            rs.getString("phoneNumber"),
            rs.getString("citizenNumber"),
            rs.getString("address"),
            new Role(rs.getInt("Role_ID"), rs.getString("roleName")),
            rs.getInt("AccountID")
        );
    }
}