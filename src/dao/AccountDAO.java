package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

import entity.Account;
import entity.Role;
import database.DatabaseConnection;

public class AccountDAO implements DAOInterface<Account> {

	public static AccountDAO getInstance() {
		return new AccountDAO();
	}

	@Override
	public int insert(Account account) {
		String sql = "INSERT INTO account (UN_UserName, Password, Email, Role_ID) VALUES (?, ?, ?, ?)";

		// Mã hóa mật khẩu trước khi lưu
		String hashedPassword = BCrypt.hashpw(account.getPassword(), BCrypt.gensalt(12));

		try (Connection con = DatabaseConnection.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			pstmt.setString(1, account.getUserName());
			pstmt.setString(2, hashedPassword);
			pstmt.setString(3, account.getEmail());
			pstmt.setInt(4, account.getRole().getRoleID());

			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				try (ResultSet rs = pstmt.getGeneratedKeys()) {
					if (rs.next()) {
						account.setAccountID(rs.getInt(1));
					}
				}
			}
			return affectedRows;
		} catch (SQLException e) {
			System.err.println("Lỗi khi thêm tài khoản: " + e.getMessage());
			return 0;
		}
	}

	@Override
	public int update(Account account) {
		String sql = "UPDATE account SET UN_UserName=?, Password=?, Email=?, Role_ID=? WHERE AccountID=?";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setString(1, account.getUserName());

			// Nếu mật khẩu thay đổi, mã hóa trước khi lưu
			String newPassword = account.getPassword();
			if (!newPassword.isEmpty()) {
				newPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
			} else {
				// Nếu không thay đổi, lấy mật khẩu cũ từ DB
				newPassword = getPasswordById(account.getAccountID());
			}
			pstmt.setString(2, newPassword);

			pstmt.setString(3, account.getEmail());
			pstmt.setInt(4, account.getRole().getRoleID());
			pstmt.setInt(5, account.getAccountID());

			return pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Lỗi khi cập nhật tài khoản: " + e.getMessage());
			return 0;
		}
	}

	// Hàm lấy mật khẩu cũ từ DB nếu không cập nhật mật khẩu mới
	private String getPasswordById(int accountID) throws SQLException {
		String sql = "SELECT Password FROM account WHERE AccountID = ?";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setInt(1, accountID);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString("Password");
				}
			}
		}
		return ""; // Trả về chuỗi rỗng nếu không tìm thấy
	}

	@Override
	public int delete(Account account) {
		String sql = "DELETE FROM account WHERE AccountID=?";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setInt(1, account.getAccountID());
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Lỗi khi xóa tài khoản: " + e.getMessage());
			return 0;
		}
	}

	// Kiểm tra tài khoản có tồn tại hay không
	public boolean isAccountExist(String username) {
		String sql = "SELECT COUNT(*) FROM Account WHERE userName = ?";

		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setString(1, username);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next() && rs.getInt(1) > 0) {
				return true; // Tài khoản đã tồn tại
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi kiểm tra tài khoản: " + e.getMessage());
			e.printStackTrace();
		}
		return false; // Tài khoản chưa tồn tại
	}

	@Override
	public List<Account> selectAll() {
		List<Account> list = new ArrayList<>();
		String sql = "SELECT a.AccountID, a.UN_UserName, a.Password, a.Email, r.Role_ID, r.RoleName "
				+ "FROM account a JOIN role r ON a.Role_ID = r.Role_ID";

		try (Connection con = DatabaseConnection.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				list.add(mapResultSetToAccount(rs));
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi lấy danh sách tài khoản: " + e.getMessage());
		}
		return list;
	}

	public Account selectById(int accountID) {
		String sql = "SELECT a.AccountID, a.UN_UserName, a.Password, a.Email, r.Role_ID, r.RoleName "
				+ "FROM account a JOIN role r ON a.Role_ID = r.Role_ID WHERE a.AccountID = ?";

		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setInt(1, accountID);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToAccount(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi tìm tài khoản theo ID: " + e.getMessage());
		}
		return null;
	}

	@Override
	public Account selectById(Account account) {
		return selectById(account.getAccountID());
	}

	public Account getAccountByUsername(String username) {
	    String sql = "SELECT a.AccountID, a.UN_UserName, a.Password, a.Email, r.Role_ID, r.RoleName "
	               + "FROM account a JOIN role r ON a.Role_ID = r.Role_ID WHERE a.UN_UserName = ?";

	    try (Connection con = DatabaseConnection.getConnection();
	         PreparedStatement pstmt = con.prepareStatement(sql)) {

	        pstmt.setString(1, username);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                return mapResultSetToAccount(rs);
	            }
	        }
	    } catch (SQLException e) {
	        System.err.println("Lỗi khi tìm tài khoản theo username: " + e.getMessage());
	    }
	    return null; 
	}


	private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
		String roleName = rs.getString("RoleName").toUpperCase();
		Role role = new Role(rs.getInt("Role_ID"), roleName);

		return new Account(rs.getInt("AccountID"), rs.getString("UN_UserName"), rs.getString("Password"),
				rs.getString("Email"), role);
	}

	public List<Account> selectByCondition(String whereClause, Object... params) {
		List<Account> list = new ArrayList<>();
		String baseQuery = "SELECT a.AccountID, a.UN_UserName, a.Password, a.Email, r.Role_ID, r.RoleName "
				+ "FROM account a JOIN role r ON a.Role_ID = r.Role_ID WHERE a." +  whereClause;

		try (Connection con = DatabaseConnection.getConnection();
				PreparedStatement pstmt = con.prepareStatement(baseQuery)) {

			for (int i = 0; i < params.length; i++) {
				pstmt.setObject(i + 1, params[i]); 
			}

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					list.add(mapResultSetToAccount(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi truy vấn Account theo điều kiện: " + e.getMessage());
		}
		return list;
	}
	

}
