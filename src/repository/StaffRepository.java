package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import enums.GenderEnum;
import model.Account;
import model.Role;
import model.Staff;
import utils.DatabaseConnection;

public class StaffRepository implements IRepository<Staff> {
	private static final Logger LOGGER = Logger.getLogger(StaffRepository.class.getName());

	@Override
	public int insert(Staff staff) {
		String insertPersonSql = "INSERT INTO person (lastName, firstName, sex, phoneNumber, citizenNumber, address, email) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
		String insertStaffSql = "INSERT INTO staff (PersonID, Role_ID, AccountID, startDate, endDate, salary, workShift, position) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection con = DatabaseConnection.getConnection();
				PreparedStatement personStmt = con.prepareStatement(insertPersonSql, Statement.RETURN_GENERATED_KEYS);
				PreparedStatement staffStmt = con.prepareStatement(insertStaffSql)) {

			// Insert vào bảng person
			personStmt.setString(1, staff.getLastName());
			personStmt.setString(2, staff.getFirstName());
			personStmt.setInt(3, staff.getGender().getCode());
			personStmt.setString(4, staff.getPhoneNumber());
			personStmt.setString(5, staff.getCitizenNumber());
			personStmt.setString(6, staff.getAddress());
			personStmt.setString(7, staff.getEmail());

			int personAffectedRows = personStmt.executeUpdate();

			if (personAffectedRows > 0) {
				try (ResultSet rs = personStmt.getGeneratedKeys()) {
					if (rs.next()) {
						int personID = rs.getInt(1); // Lấy PersonID sau khi insert thành công

						// Insert vào bảng staff
						staffStmt.setInt(1, personID);
						staffStmt.setInt(2, staff.getRole().getRoleID());
						staffStmt.setInt(3, staff.getAccount().getAccountID());
						staffStmt.setDate(4, staff.getStartDate() != null ? java.sql.Date.valueOf(staff.getStartDate()) : null);
	                    staffStmt.setDate(5, staff.getEndDate() != null ? java.sql.Date.valueOf(staff.getEndDate()) : null);
						staffStmt.setDouble(6, staff.getSalary());
						staffStmt.setString(7, staff.getWorkShift());
						staffStmt.setString(8, staff.getPosition());

						return staffStmt.executeUpdate();
					}
				}
			}
		} catch (SQLException e) {
			LOGGER.severe("Insert staff failed: " + e.getMessage());
		}
		return 0;
	}

	@Override
	public int update(Staff staff) {
		String updatePersonSql = "UPDATE person SET lastName=?, firstName=?, sex=?, phoneNumber=?, citizenNumber=?, address=?, email=? WHERE PersonID=?";
		String updateStaffSql = "UPDATE staff SET Role_ID=?, AccountID=?, startDate=?, endDate=?, salary=?, workShift=?, position=? WHERE PersonID=?";

		try (Connection con = DatabaseConnection.getConnection();
				PreparedStatement personStmt = con.prepareStatement(updatePersonSql);
				PreparedStatement staffStmt = con.prepareStatement(updateStaffSql)) {

			// Cập nhật thông tin trong bảng person
			personStmt.setString(1, staff.getLastName());
			personStmt.setString(2, staff.getFirstName());
			personStmt.setInt(3, staff.getGender().getCode());
			personStmt.setString(4, staff.getPhoneNumber());
			personStmt.setString(5, staff.getCitizenNumber());
			personStmt.setString(6, staff.getAddress());
			personStmt.setString(7, staff.getEmail());
			personStmt.setInt(8, staff.getId());

			int personAffectedRows = personStmt.executeUpdate();

			// Nếu cập nhật bảng person thành công, tiếp tục cập nhật bảng staff
			if (personAffectedRows > 0) {
				staffStmt.setInt(1, staff.getRole().getRoleID());
				staffStmt.setInt(2, staff.getAccount().getAccountID());
				staffStmt.setDate(4, staff.getStartDate() != null ? java.sql.Date.valueOf(staff.getStartDate()) : null);
                staffStmt.setDate(5, staff.getEndDate() != null ? java.sql.Date.valueOf(staff.getEndDate()) : null);

				staffStmt.setDouble(5, staff.getSalary());
				staffStmt.setString(6, staff.getWorkShift());
				staffStmt.setString(7, staff.getPosition());
				staffStmt.setInt(8, staff.getId());

				return staffStmt.executeUpdate(); 
			}
		} catch (SQLException e) {
			LOGGER.severe("Update staff failed: " + e.getMessage());
		}
		return 0; 
	}

	@Override
    public int delete(Staff staff) {
        String deleteStaffSql = "DELETE FROM staff WHERE PersonID=?";
        String deletePersonSql = "DELETE FROM person WHERE PersonID=?";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement staffStmt = con.prepareStatement(deleteStaffSql);
             PreparedStatement personStmt = con.prepareStatement(deletePersonSql)) {

            // Xóa nhân viên từ bảng staff
            staffStmt.setInt(1, staff.getId());
            int staffAffectedRows = staffStmt.executeUpdate();
            
            // Nếu xóa bảng staff thành công, tiếp tục xóa bảng person
            if (staffAffectedRows > 0) {
                personStmt.setInt(1, staff.getId());
                return personStmt.executeUpdate(); 
            }
        } catch (SQLException e) {
            LOGGER.severe("Delete staff failed: " + e.getMessage());
        }
        return 0; 
	}
	@Override
	public List<Staff> selectAll() {
		String sql = "SELECT p.PersonID, p.lastName, p.firstName, p.sex, p.phoneNumber, p.citizenNumber, p.address, p.email, "
	               + "a.AccountID, a.UN_UserName, r.Role_ID, r.roleName, s.startDate, s.endDate, s.salary, s.workShift, s.position "
	               + "FROM person p "
	               + "JOIN staff s ON p.PersonID = s.PersonID "
	               + "JOIN role r ON s.Role_ID = r.Role_ID "
	               + "LEFT JOIN account a ON s.AccountID = a.AccountID";
	    return executeQuery(sql);
	}

	@Override
	public Staff selectById(Staff staff) {
		return selectById(staff.getId());
	}

	public Staff selectById(int personID) {
		String sql = "SELECT p.PersonID, p.lastName, p.firstName, p.sex, p.phoneNumber, p.citizenNumber, p.address, p.email, "
	               + "a.AccountID, a.UN_UserName, r.Role_ID, r.roleName, s.startDate, s.endDate, s.salary, s.workShift, s.position "
	               + "FROM person p "
	               + "JOIN staff s ON p.PersonID = s.PersonID "
	               + "JOIN role r ON s.Role_ID = r.Role_ID "
	               + "LEFT JOIN account a ON s.AccountID = a.AccountID "
	               + "WHERE p.PersonID = ?";
	    List<Staff> result = executeQuery(sql, personID);
	    return result.isEmpty() ? null : result.get(0);
	}

	public List<Staff> selectByCondition(String whereClause, Object... params) {
		String sql = "SELECT p.PersonID, p.lastName, p.firstName, p.sex, p.phoneNumber, p.citizenNumber, p.address, p.email, "
	               + "a.AccountID, a.UN_UserName, r.Role_ID, r.roleName, s.startDate, s.endDate, s.salary, s.workShift, s.position "
	               + "FROM person p "
	               + "JOIN staff s ON p.PersonID = s.PersonID "
	               + "JOIN role r ON s.Role_ID = r.Role_ID "
	               + "LEFT JOIN account a ON s.AccountID = a.AccountID";

	    if (whereClause != null && !whereClause.trim().isEmpty()) {
	        sql += " WHERE " + whereClause;
	    }

	    return executeQuery(sql, params);
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
			LOGGER.severe("Query failed: " + e.getMessage());
		}
		return list;
	}
	private Staff mapResultSetToStaff(ResultSet rs) throws SQLException {
	    // Lấy các giá trị từ ResultSet
	    int personID = rs.getInt("PersonID");
	    String lastName = rs.getString("lastName");
	    String firstName = rs.getString("firstName");
	    GenderEnum gender = GenderEnum.fromCode(rs.getInt("sex"));
	    String phoneNumber = rs.getString("phoneNumber");
	    String citizenNumber = rs.getString("citizenNumber");
	    String address = rs.getString("address");
	    String email = rs.getString("email"); // Đảm bảo lấy được email
	    int accountID = rs.getInt("AccountID");
	    Role role = new Role(rs.getInt("Role_ID"), rs.getString("roleName"));
	    
	    // Chuyển đổi các ngày từ java.sql.Date thành LocalDate
	    LocalDate startDate = rs.getDate("startDate") != null ? rs.getDate("startDate").toLocalDate() : null;
	    LocalDate endDate = rs.getDate("endDate") != null ? rs.getDate("endDate").toLocalDate() : null;
	    
	    // Lấy các giá trị khác
	    double salary = rs.getDouble("salary");
	    String workShift = rs.getString("workShift");
	    String position = rs.getString("position");
	    
	    // Kiểm tra tài khoản
	    Account account = null;
	    if (accountID > 0) { // Kiểm tra nếu accountID hợp lệ
	        String userName = rs.getString("UN_Username");
	        String accountEmail = rs.getString("Email");
	        account = new Account(accountID, userName, null, accountEmail, null);
	    }
	    
	    // Trả về đối tượng Staff với tất cả các tham số đã lấy từ ResultSet
	    return new Staff(personID, lastName, firstName, gender, phoneNumber, citizenNumber, address, email, account, role, startDate, endDate, salary, workShift, position);
	}


}