package backend;

import java.util.List;

import java.util.Optional;
import java.util.regex.Pattern;

//import org.mindrot.jbcrypt.BCrypt;

import dao.AccountDAO;
import entity.Account;
import entity.Role;
import exception.AccountException;

public class AccountService {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$"); // Regex kiểm tra email

    private final AccountDAO accountDAO;

    public AccountService() {
        this.accountDAO = AccountDAO.getInstance();
    }

    /**
     * Đăng ký tài khoản mới
     */
    public boolean register(String username, String password, String email, Role role) {
        validateAccountData(username, password, email);

        if (accountDAO.isAccountExist(username)) {
            throw new AccountException("Tên đăng nhập đã tồn tại!");
        }

        Account newAccount = new Account(0, username, password, email, role);
        return accountDAO.insert(newAccount) > 0;
    }

    /**
     * Đăng nhập
     */
    public Optional<Account> login(String username, String password) {
        Account account = accountDAO.getAccountByUsername(username);

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


    /**
     * Cập nhật thông tin tài khoản
     */
    public boolean updateAccount(int accountID, String newUsername, String newPassword, String email, Role role) {
        validateAccountData(newUsername, newPassword, email);

        Account existingAccount = accountDAO.selectById(accountID);
        if (existingAccount == null) {
            throw new AccountException("Tài khoản không tồn tại!");
        }

        if (!existingAccount.getUserName().equals(newUsername) && accountDAO.isAccountExist(newUsername)) {
            throw new AccountException("Tên đăng nhập mới đã được sử dụng!");
        }

        existingAccount.setUserName(newUsername);
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            existingAccount.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt())); // 🔹 Hash mật khẩu mới
        }

        existingAccount.setEmail(email);
        existingAccount.setRole(role);

        return accountDAO.update(existingAccount) > 0;
    }


    /**
     * Xóa tài khoản (Không xóa tài khoản admin mặc định)
     */
    public boolean deleteAccount(int accountID) {
        Account account = accountDAO.selectById(accountID);
        if (account == null) {
            throw new AccountException("Tài khoản không tồn tại!");
        }

        if ("Admin".equalsIgnoreCase(account.getRole().getRoleName())) {
            throw new AccountException("Không thể xóa tài khoản Admin!");
        }

        return accountDAO.delete(account) > 0;
    }


    /**
     * Lấy danh sách tất cả tài khoản
     */
    public List<Account> getAllAccounts() {
        return accountDAO.selectAll();
    }

    /**
     * Kiểm tra dữ liệu tài khoản
     */
    private void validateAccountData(String username, String password, String email) {
        if (username == null || username.trim().isEmpty()) {
            throw new AccountException("Tên đăng nhập không được để trống!");
        }
        if (password != null && !password.isEmpty() && password.length() < 6) {
            throw new AccountException("Mật khẩu phải có ít nhất 6 ký tự!");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new AccountException("Email không hợp lệ!");
        }
    }


}
