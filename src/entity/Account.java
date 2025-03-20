package entity;

import org.mindrot.jbcrypt.BCrypt;

public class Account {
    private int accountID;
    private String userName;
    private String password; // Đã mã hóa bằng bcrypt
    private String email;
    private Role role; // CUSTOMER, STAFF

    public Account() {
        super();
    }

    public Account(int accountID, String userName, String password, String email, Role role) {
        this.accountID = accountID;
        this.userName = userName;
        this.setPassword(password); // Mã hóa mật khẩu
        this.setEmail(email); // Kiểm tra định dạng email
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

    // Mã hóa mật khẩu bằng bcrypt
    public void setPassword(String password) {
        this.password = BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    // Kiểm tra mật khẩu nhập vào có đúng không
    public boolean checkPassword(String inputPassword) {
        return BCrypt.checkpw(inputPassword, this.password);
    }

    public String getEmail() {
        return email;
    }

    // Kiểm tra định dạng email hợp lệ
    public void setEmail(String email) {
        if (email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
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

    @Override
    public String toString() {
        return "Account: ID: " + accountID + 
               "\n\tUsername: " + userName + 
               "\n\tEmail: " + email + 
               "\n\tRole: " + role.getRoleName();
    }
}
