package service;

import model.Account;
import repository.AccountRepository;
import utils.DatabaseConnection;

import org.mindrot.jbcrypt.BCrypt;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AuthService {
	private final AccountRepository accountRepository;

	public AuthService() {
		this.accountRepository = AccountRepository.getInstance();
	}

	public Optional<Account> login(String username, String password) {
		Account account = accountRepository.getAccountByUsername(username);

		if (account == null) {
			System.out.println("Không tìm thấy tài khoản!");
			return Optional.empty();
		}

		boolean passwordMatch = BCrypt.checkpw(password, account.getPassword());

		if (!passwordMatch) {
			System.out.println("Mật khẩu không đúng!");
			return Optional.empty();
		}

		System.out.println("Đăng nhập thành công!");
		return Optional.of(account);
	}

	public boolean changePassword(int accountId, String newPassword) {
		// Validate password (could be more complex)
		if (newPassword == null || newPassword.trim().isEmpty()) {
			throw new IllegalArgumentException("Mật khẩu không được để trống");
		}

		// Hash the password before storing
		String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

		// Update the password
		return accountRepository.updatePassword(accountId, hashedPassword);
	}

	public boolean logout() {
		// Any logout-specific operations
		return true;
	}

	public boolean verifyPassword(int accountID, String password) {
		try {
			// Thực hiện truy vấn lấy mật khẩu đã hash từ database
			String sql = "SELECT password FROM account WHERE account_id = ?";
			try (Connection con = DatabaseConnection.getConnection();
					PreparedStatement pstmt = con.prepareStatement(sql)) {

				pstmt.setInt(1, accountID);
				try (ResultSet rs = pstmt.executeQuery()) {
					if (rs.next()) {
						String storedHashedPassword = rs.getString("password");
						// Sử dụng BCrypt để kiểm tra mật khẩu
						return BCrypt.checkpw(password, storedHashedPassword);
					}
				}
			}
		} catch (SQLException e) {
			showAlert(AlertType.ERROR, "Lỗi", "Không kiểm tra được mật khẩu:", e.getMessage());
		}
		return false;
	}

	private void showAlert(AlertType alertType, String title, String header, String content) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}
}