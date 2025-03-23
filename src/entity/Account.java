package entity;

import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mindrot.jbcrypt.BCrypt;

public class Account {
	private int accountID;
	private String userName;
	private String password; // Đã mã hóa bằng bcrypt
	private String email;
	private Role role; // CUSTOMER, STAFF
	private int status; // 0 - Bị khóa, 1 - Hoạt động
	private Timestamp createdAt;
	private Timestamp updatedAt;

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

	public Account() {
		this.createdAt = new Timestamp(System.currentTimeMillis());
		this.updatedAt = new Timestamp(System.currentTimeMillis());
		this.status = 1; // Mặc định tài khoản hoạt động
	}

	public Account(int accountID, String userName, String password, String email, Role role, int status) {
		this.accountID = accountID;
		this.userName = userName;
		this.setPassword(password); // Mã hóa mật khẩu
		this.setEmail(email); // Kiểm tra định dạng email
		this.role = role;
		this.status = status;
		this.createdAt = new Timestamp(System.currentTimeMillis());
		this.updatedAt = new Timestamp(System.currentTimeMillis());
	}

	public Account(int accountID, String userName, String password, String email, Role role) {
		this.accountID = accountID;
		this.userName = userName;
		this.password = password; // Đã mã hóa rồi nên không cần gọi setPassword()
		this.email = email;
		this.role = role;
	}

	public int getAccountID() {
		return accountID;
	}

	public void setAccountID(int accountID) {
		this.accountID = accountID;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	// Chỉ mã hóa mật khẩu nếu nó chưa được mã hóa
	public void setPassword(String password) {
		if (!password.startsWith("$2a$")) { // Kiểm tra xem có phải bcrypt hash không
			this.password = BCrypt.hashpw(password, BCrypt.gensalt(12));
		} else {
			this.password = password; // Tránh mã hóa lại mật khẩu đã mã hóa
		}
		this.updatedAt = new Timestamp(System.currentTimeMillis());
	}

	// Kiểm tra mật khẩu nhập vào có đúng không
	public boolean checkPassword(String inputPassword) {
		return BCrypt.checkpw(inputPassword, this.password);
	}

	public String getEmail() {
		return email;
	}

	// Kiểm tra định dạng email hợp lệ bằng Pattern & Matcher
	public void setEmail(String email) {
		Matcher matcher = EMAIL_PATTERN.matcher(email);
		if (matcher.matches()) {
			this.email = email;
		} else {
			throw new IllegalArgumentException("Email không hợp lệ!");
		}
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		if (status == 0 || status == 1) {
			this.status = status;
		} else {
			throw new IllegalArgumentException(
					"Trạng thái không hợp lệ! Chỉ chấp nhận 0 (Bị khóa) hoặc 1 (Hoạt động).");
		}
		this.updatedAt = new Timestamp(System.currentTimeMillis());
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public Timestamp getUpdatedAt() {
		return updatedAt;
	}

	@Override
	public String toString() {
		return "Account: ID: " + accountID + "\n\tUsername: " + userName + "\n\tEmail: " + email + "\n\tRole: "
				+ role.getRoleName() + "\n\tStatus: " + (status == 1 ? "Active" : "Locked") + "\n\tCreated At: "
				+ createdAt + "\n\tUpdated At: " + updatedAt;
	}

	// Thêm phương thức kiểm tra tài khoản có hoạt động không
	public boolean isActive() {
		return this.status == 1;
	}
}
