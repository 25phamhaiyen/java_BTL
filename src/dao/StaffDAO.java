package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import Enum.GenderEnum;
import entity.Role;
import entity.Staff;
import utils.DatabaseConnection;

public class StaffDAO implements DAOInterface<Staff> {
	private static final Logger LOGGER = Logger.getLogger(StaffDAO.class.getName());

	@Override
	public int insert(Staff staff) {
		String sql = "INSERT INTO Staff (lastName, firstName, sex, phoneNumber, citizenNumber, address, role_ID, AccountID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection con = DatabaseConnection.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			pstmt.setString(1, staff.getLastName());
			pstmt.setString(2, staff.getFirstName());
			pstmt.setInt(3, staff.getSex() == GenderEnum.MALE ? 1 : (staff.getSex() == GenderEnum.FEMALE ? 0 : 2)); // Chỉnh
			pstmt.setString(4, staff.getPhoneNumber());
			pstmt.setString(5, staff.getCitizenNumber());
			pstmt.setString(6, staff.getAddress());
			pstmt.setInt(7, staff.getRole().getRoleID());
			pstmt.setInt(8, staff.getAccountID()); // Thêm AccountID vào câu truy vấn

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
		String sql = "UPDATE Staff SET lastName=?, firstName=?, sex=?, phoneNumber=?, citizenNumber=?, address=?, role_ID=? WHERE staffID=?";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setString(1, staff.getLastName());
			pstmt.setString(2, staff.getFirstName());
			pstmt.setInt(3, staff.getSex() == GenderEnum.MALE ? 1 : (staff.getSex() == GenderEnum.FEMALE ? 0 : 2));
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
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setInt(1, staff.getStaffID());
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			LOGGER.severe("Lỗi khi xóa nhân viên: " + e.getMessage());
			return 0;
		}
	}

	@Override
	public List<Staff> selectAll() {
		String sql = "SELECT s.staffID, s.lastName, s.firstName, s.sex, s.phoneNumber, s.citizenNumber, s.address, s.AccountID, r.role_ID, r.roleName "
				+ "FROM Staff s JOIN Role r ON s.role_ID = r.role_ID";

		return executeQuery(sql);
	}

	public Staff selectById(int staffID) {
		String sql = "SELECT s.staffID, s.lastName, s.firstName, s.sex, s.phoneNumber, s.citizenNumber, s.address, s.AccountID, r.role_ID, r.roleName "
				+ "FROM Staff s JOIN Role r ON s.role_ID = r.role_ID WHERE s.staffID = ?";

		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

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
		String baseQuery = "SELECT s.staffID, s.lastName, s.firstName, s.sex, s.phoneNumber, s.citizenNumber, s.address, s.AccountID, r.role_ID, r.roleName "
				+ "FROM Staff s JOIN Role r ON s.role_ID = r.role_ID WHERE " + whereClause;

		return executeQuery(baseQuery, params);
	}

	private List<Staff> executeQuery(String sql, Object... params) {
		List<Staff> list = new ArrayList<>();
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

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
		// Lấy giới tính từ DB (đọc dưới dạng số nguyên)
		int genderValue = rs.getInt("sex");
		GenderEnum gender;

		// Kiểm tra giá trị và ánh xạ về Enum
		if (genderValue == 1) {
			gender = GenderEnum.MALE;
		} else if (genderValue == 0) {
			gender = GenderEnum.FEMALE;
		} else {
			gender = GenderEnum.UNKNOWN;
			System.err.println("Lỗi: Giới tính '" + genderValue + "' không hợp lệ. Đặt mặc định là UNKNOWN.");
		}

		// Lấy thông tin Role từ DB
		int roleID = rs.getInt("role_ID");
		String roleName = rs.getString("roleName");
		Role role = new Role(roleID, roleName); // Tạo đối tượng Role
		int accountID = rs.getInt("AccountID");

		return new Staff(rs.getInt("staffID"), rs.getString("lastName"), rs.getString("firstName"), gender,
				rs.getString("phoneNumber"), rs.getString("citizenNumber"), rs.getString("address"), role, accountID);
	}

}
