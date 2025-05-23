package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Account;
import model.AccountPermission;
import model.Permission;
import utils.DatabaseConnection;

public class AccountPermissionRepository implements IRepository<AccountPermission> {

	private static AccountPermissionRepository instance;

	public static AccountPermissionRepository getInstance() {
		if (instance == null) {
			synchronized (AccountPermissionRepository.class) {
				if (instance == null) {
					instance = new AccountPermissionRepository();
				}
			}
		}
		return instance;
	}

	@Override
	public int insert(AccountPermission t) {
		String sql = "INSERT INTO account_permission (account_id, permission_code) VALUES (?, ?)";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setInt(1, t.getAccount().getAccountID());
			pstmt.setString(2, t.getPermission().getPermissionCode());

			return pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Lỗi khi thêm quyền cho tài khoản: " + e.getMessage());
			return 0;
		}
	}

	@Override
	public int update(AccountPermission t) {
		// Không cần update vì chỉ có account_id và permission_code
		return 0;
	}

	@Override
	public int delete(AccountPermission t) {
		String sql = "DELETE FROM account_permission WHERE account_id = ? AND permission_code = ?";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setInt(1, t.getAccount().getAccountID());
			pstmt.setString(2, t.getPermission().getPermissionCode());

			return pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Lỗi khi xóa quyền của tài khoản: " + e.getMessage());
			return 0;
		}
	}

	@Override
	public List<AccountPermission> selectAll() {
		List<AccountPermission> list = new ArrayList<>();
		String sql = "SELECT ap.*, a.username, p.description FROM account_permission ap "
				+ "JOIN account a ON ap.account_id = a.account_id "
				+ "JOIN permission p ON ap.permission_code = p.permission_code";

		try (Connection con = DatabaseConnection.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				Account account = new Account();
				account.setAccountID(rs.getInt("account_id"));
				account.setUserName(rs.getString("username"));

				Permission permission = new Permission(rs.getString("permission_code"), rs.getString("description"));

				AccountPermission accountPermission = new AccountPermission(rs.getInt("id"), account, permission);

				list.add(accountPermission);
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi lấy danh sách quyền tài khoản: " + e.getMessage());
		}
		return list;
	}

	@Override
	public AccountPermission selectById(AccountPermission t) {
		// Không cần phương thức này
		return null;
	}

	@Override
	public List<AccountPermission> selectByCondition(String condition, Object... params) {
		List<AccountPermission> list = new ArrayList<>();
		String sql = "SELECT ap.*, a.username, p.description FROM account_permission ap "
				+ "JOIN account a ON ap.account_id = a.account_id "
				+ "JOIN permission p ON ap.permission_code = p.permission_code " + "WHERE " + condition;

		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			for (int i = 0; i < params.length; i++) {
				pstmt.setObject(i + 1, params[i]);
			}

			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				Account account = new Account();
				account.setAccountID(rs.getInt("account_id"));
				account.setUserName(rs.getString("username"));

				Permission permission = new Permission(rs.getString("permission_code"), rs.getString("description"));

				AccountPermission accountPermission = new AccountPermission(rs.getInt("id"), account, permission);

				list.add(accountPermission);
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi truy vấn quyền tài khoản: " + e.getMessage());
		}
		return list;
	}

	/**
	 * Kiểm tra xem một tài khoản có quyền cụ thể hay không
	 * 
	 * @param accountId      ID của tài khoản
	 * @param permissionCode Mã quyền cần kiểm tra
	 * @return true nếu có quyền, false nếu không có
	 */
	public boolean checkPermission(int accountId, String permissionCode) {
		String sql = "SELECT COUNT(*) FROM account_permission " + "WHERE account_id = ? AND permission_code = ?";

		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setInt(1, accountId);
			pstmt.setString(2, permissionCode);

			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi kiểm tra quyền tài khoản: " + e.getMessage());
		}
		return false;
	}

	/**
	 * Lấy danh sách quyền của một tài khoản
	 * 
	 * @param accountId ID của tài khoản
	 * @return Danh sách các Permission
	 */
	public List<Permission> getPermissionsByAccountId(int accountId) {
		List<Permission> permissions = new ArrayList<>();
		String sql = "SELECT p.permission_code, p.description FROM account_permission ap "
				+ "JOIN permission p ON ap.permission_code = p.permission_code " + "WHERE ap.account_id = ?";

		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setInt(1, accountId);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				Permission permission = new Permission(rs.getString("permission_code"), rs.getString("description"));
				permissions.add(permission);
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi lấy danh sách quyền của tài khoản: " + e.getMessage());
		}
		return permissions;
	}
}