package repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt;

import model.Account;
import model.Role;
import utils.DatabaseConnection;

public class AccountRepository implements IRepository<Account> {

	private static AccountRepository instance;

	public static AccountRepository getInstance() {
		if (instance == null) {
			synchronized (AccountRepository.class) {
				if (instance == null) {
					instance = new AccountRepository();
				}
			}
		}
		return instance;
	}

	@Override
	public int insert(Account account) {
		String sql = "INSERT INTO account (username, `password`, role_id) VALUES (?, ?, ?)";

		try (Connection con = DatabaseConnection.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			pstmt.setString(1, account.getUserName());
			String hashedPassword = BCrypt.hashpw(account.getPassword(), BCrypt.gensalt());
			pstmt.setString(2, hashedPassword);
			pstmt.setInt(3, account.getRole().getRoleID());

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
		String sql = "UPDATE account SET username=?, password=?, role_id=?, active=? WHERE account_id=?";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setString(1, account.getUserName());

			String newPassword = account.getPassword();
			if (newPassword == null || newPassword.trim().isEmpty()) {
				newPassword = getPasswordById(account.getAccountID());
			}
			pstmt.setString(2, newPassword);
			pstmt.setInt(3, account.getRole().getRoleID());
			pstmt.setInt(4, account.getAccountID());
			pstmt.setBoolean(5, account.isActive());

			return pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Lỗi khi cập nhật tài khoản: " + e.getMessage());
			return 0;
		}
	}

	public int updateRoleAndActive(Account account) {
		String sql = "UPDATE account SET role_id=?, active=? WHERE account_id=?";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setInt(1, account.getRole().getRoleID());
			pstmt.setBoolean(2, account.isActive());
			pstmt.setInt(3, account.getAccountID());

			return pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Lỗi khi cập nhật tài khoản: " + e.getMessage());
			return 0;
		}
	}

	// Hàm lấy mật khẩu cũ từ DB nếu không cập nhật mật khẩu mới
	private String getPasswordById(int accountID) throws SQLException {
		String sql = "SELECT password FROM account WHERE account_id = ?";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setInt(1, accountID);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString("password");
				}
			}
		}
		return ""; // Trả về chuỗi rỗng nếu không tìm thấy
	}

	@Override
	public int delete(Account account) {
		String sql = "DELETE FROM account WHERE account_id=?";
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
		String sql = "SELECT COUNT(*) " + "FROM account " + "WHERE username = ?";

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
		String sql = "SELECT a.account_id, a.username, a.password, a.active, r.role_id, r.role_name "
				+ "FROM account a " + "JOIN role r ON a.role_id = r.role_id";

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
		String sql = "SELECT a.account_id, a.username, a.password, a.active, r.role_id, r.role_name "
				+ "FROM account a " + "JOIN role r " + "ON a.role_id = r.role_id WHERE a.account_id = ?";

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
		String sql = "SELECT a.account_id, a.username, a.password, a.active, r.role_id, r.role_name "
				+ "FROM account a " + "JOIN role r " + "ON a.role_id = r.role_id WHERE a.username = ?";

		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
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
		String roleName = rs.getString("role_name").toUpperCase();
		Role role = new Role(rs.getInt("role_id"), roleName);

		Account acc = new Account(rs.getInt("account_id"), rs.getString("username"), rs.getString("password"), role);
		acc.setActive(rs.getInt("active") == 1);

		return acc;
	}

	public List<Account> selectByCondition(String whereClause, Object... params) {
		List<Account> list = new ArrayList<>();
		String baseQuery = "SELECT a.account_id, a.username, a.password, r.role_id, r.role_name " + "FROM account a "
				+ "JOIN role r " + "ON a.role_id = r.role_id " + "WHERE a." + whereClause;

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

	public boolean updatePassword(int accountID, String newPassword) {
		String query = "UPDATE account SET password = ? WHERE account_id = ?";
		try (PreparedStatement stmt = DatabaseConnection.getConnection().prepareStatement(query)) {
			stmt.setString(1, newPassword);
			stmt.setInt(2, accountID);
			int rowsUpdated = stmt.executeUpdate();

			return rowsUpdated > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public Map<Account, String> getAllAccountsWithPermissions() {
		Map<Account, String> accountPermissionsMap = new LinkedHashMap<>();
		String query = "SELECT a.account_id, a.username, r.role_id, r.role_name, a.active, "
				+ "GROUP_CONCAT(p.permission_code) AS permissions " + "FROM account a "
				+ "JOIN role r ON r.role_id = a.role_id "
				+ "LEFT JOIN account_permission ap ON a.account_id = ap.account_id "
				+ "LEFT JOIN permission p ON ap.permission_code = p.permission_code " + "GROUP BY a.account_id";

		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(query);
				ResultSet resultSet = statement.executeQuery()) {

			while (resultSet.next()) {
				// Tạo đối tượng Account
				Account account = new Account();
				account.setAccountID(resultSet.getInt("account_id"));
				account.setUserName(resultSet.getString("username"));

				// Tạo đối tượng Role và gán vào Account
				Role role = new Role(resultSet.getInt("role_id"), resultSet.getString("role_name"));
				account.setRole(role);

				// Gán trạng thái hoạt động
				account.setActive(resultSet.getBoolean("active"));

				// Lấy danh sách quyền dưới dạng chuỗi
				String permissions = resultSet.getString("permissions");

				// Thêm vào Map
				accountPermissionsMap.put(account, permissions != null ? permissions : "");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return accountPermissionsMap;
	}

	public boolean resetPassword(int accountID, String newPassword) {
		String query = "UPDATE account SET password = ? WHERE account_id = ?";
		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(query)) {
			if (newPassword == null || newPassword.trim().isEmpty()) {
				newPassword = getPasswordById(accountID);
			}
			String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
			statement.setString(1, hashedPassword);
			statement.setInt(2, accountID);
			return statement.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public int getAccountIdByUsername(String username) {
		String sql = "SELECT account_id FROM account WHERE username = ?";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setString(1, username);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				return rs.getInt("account_id");
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi lấy accountId theo username: " + e.getMessage());
		}
		return 0;
	}

}
