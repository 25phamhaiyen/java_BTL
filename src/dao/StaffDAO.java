package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import entity.Staff;
import entity.Role;
import utils.DatabaseConnection;
import Enum.GenderEnum;

public class StaffDAO implements DAOInterface<Staff> {
    private static final Logger LOGGER = Logger.getLogger(StaffDAO.class.getName());

    @Override
    public int insert(Staff staff) {
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
                LOGGER.info("Insert staff successful. ID: " + staff.getStaffID());
            }
            return affectedRows;
        } catch (SQLException e) {
            LOGGER.severe("Insert staff failed: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int update(Staff staff) {
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
        } catch (SQLException e) {
            LOGGER.severe("Update staff failed: " + e.getMessage());
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
            LOGGER.severe("Delete staff failed: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public List<Staff> selectAll() {
        return executeQuery(
            "SELECT s.staffID, s.lastName, s.firstName, s.sex, s.phoneNumber, " +
            "s.citizenNumber, s.address, s.AccountID, r.Role_ID, r.roleName " +
            "FROM Staff s JOIN Role r ON s.Role_ID = r.Role_ID"
        );
    }

    @Override
    public Staff selectById(Staff staff) {
        return selectById(staff.getStaffID());
    }

    public Staff selectById(int staffID) {
        List<Staff> result = executeQuery(
            "SELECT s.staffID, s.lastName, s.firstName, s.sex, s.phoneNumber, " +
            "s.citizenNumber, s.address, s.AccountID, r.Role_ID, r.roleName " +
            "FROM Staff s JOIN Role r ON s.Role_ID = r.Role_ID WHERE s.staffID = ?", 
            staffID
        );
        return result.isEmpty() ? null : result.get(0);
    }

    public List<Staff> selectByCondition(String whereClause, Object... params) {
        String sql = "SELECT s.staffID, s.lastName, s.firstName, s.sex, s.phoneNumber, " +
                     "s.citizenNumber, s.address, s.AccountID, r.Role_ID, r.roleName " +
                     "FROM Staff s JOIN Role r ON s.Role_ID = r.Role_ID";
        
        if (whereClause != null && !whereClause.trim().isEmpty()) {
            sql += " WHERE " + whereClause;
        }
        
        return executeQuery(sql, params);
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
            LOGGER.severe("Query failed: " + e.getMessage());
        }
        return list;
    }

    private Staff mapResultSetToStaff(ResultSet rs) throws SQLException {
        return new Staff(
            rs.getInt("staffID"),
            rs.getString("lastName"),
            rs.getString("firstName"),
            GenderEnum.fromCode(rs.getInt("sex")),
            rs.getString("phoneNumber"),
            rs.getString("citizenNumber"),
            rs.getString("address"),
            new Role(rs.getInt("Role_ID"), rs.getString("roleName")),
            rs.getInt("AccountID")
        );
    }
}