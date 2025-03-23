package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import Enum.RoleType;
import database.DatabaseConnection;
import entity.Account;
import entity.Role;

public class AccountDAO {

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

	// Lấy thông tin tài khoản theo username
	public Account getAccountByUsername(String username) {
		Account account = null;
		String sql = "SELECT a.accountID, a.userName, a.password, a.email, r.roleID, r.roleName "
				+ "FROM Account a JOIN Role r ON a.roleID = r.roleID WHERE a.userName = ?";

		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setString(1, username);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				RoleType roleType = RoleType.valueOf(rs.getString("roleName").toUpperCase());
				Role role = new Role(rs.getInt("roleID"), roleType);
				account = new Account(rs.getInt("accountID"), rs.getString("userName"), rs.getString("password"),
						rs.getString("email"), role);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return account;
	}

	// Lấy danh sách tất cả tài khoản
	public List<Account> getAllAccounts() {
		List<Account> accounts = new ArrayList<>();
		String sql = "SELECT a.accountID, a.userName, a.password, a.email, r.roleID, r.roleName "
				+ "FROM Account a JOIN Role r ON a.roleID = r.roleID";

		try (Connection con = DatabaseConnection.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				int accountID = rs.getInt("accountID");
				String userName = rs.getString("userName");
				String password = rs.getString("password");
				String email = rs.getString("email");
				int roleID = rs.getInt("roleID");
				String roleName = rs.getString("roleName");

				RoleType roleType = RoleType.valueOf(roleName.toUpperCase());
				Role role = new Role(roleID, roleType);
				Account account = new Account(accountID, userName, password, email, role);
				accounts.add(account);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return accounts;
	}

	// Đăng ký tài khoản mới
	public boolean createAccount(Account account) {
		if (isAccountExist(account.getUserName())) {
			System.out.println("Tài khoản đã tồn tại!");
			return false;
		}

		String sql = "INSERT INTO Account (userName, password, email, roleID) VALUES (?, ?, ?, ?)";

		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setString(1, account.getUserName());
			pstmt.setString(2, BCrypt.hashpw(account.getPassword(), BCrypt.gensalt())); // Mã hóa mật khẩu
			pstmt.setString(3, account.getEmail());
			pstmt.setInt(4, account.getRole().getRoleID());

			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// Xác thực đăng nhập
	public boolean authenticate(String username, String password) {
		Account account = getAccountByUsername(username);
		return account != null && BCrypt.checkpw(password, account.getPassword());
	}

	// Đổi mật khẩu tài khoản
	public boolean changePassword(String username, String oldPassword, String newPassword) {
		Account account = getAccountByUsername(username);
		if (account == null) {
			System.out.println("Tài khoản không tồn tại!");
			return false;
		}
		if (!BCrypt.checkpw(oldPassword, account.getPassword())) {
			System.out.println("Mật khẩu cũ không chính xác!");
			return false;
		}

		String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
		String sql = "UPDATE Account SET password = ? WHERE userName = ?";

		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setString(1, hashedPassword);
			pstmt.setString(2, username);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// Cập nhật tài khoản
	public boolean updateAccount(Account account) {
		String sql = "UPDATE Account SET email = ?, roleID = ? WHERE userName = ?";

		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setString(1, account.getEmail());
			pstmt.setInt(2, account.getRole().getRoleID());
			pstmt.setString(3, account.getUserName());

			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// Xóa tài khoản
	public boolean deleteAccount(String username) {
		if (!isAccountExist(username)) {
			System.out.println("Tài khoản không tồn tại!");
			return false;
		}

		String sql = "DELETE FROM Account WHERE userName = ?";

		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setString(1, username);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
}
