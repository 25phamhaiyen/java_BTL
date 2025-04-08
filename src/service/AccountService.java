package service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.mindrot.jbcrypt.BCrypt;

import exception.AccountException;
import model.Account;
import model.Role;
import repository.AccountRepository;
import utils.EmailUtil;

public class AccountService {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$"); // Regex kiểm tra email

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Z])(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    private final AccountRepository accountRepository;

    public AccountService() {
        this.accountRepository = AccountRepository.getInstance();
    }

    /**
     * Đăng ký tài khoản mới
     */
    public boolean register(String username, String password, String email, Role role) {
        validateAccountData(username, password, email);

        if (accountRepository.isAccountExist(username)) {
            throw new AccountException("Tên đăng nhập đã tồn tại!");
        }

        Account newAccount = new Account(0, username, password, email, role);
        return accountRepository.insert(newAccount) > 0;
    }

    /**
     * Đăng nhập
     */
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


    /**
     * Cập nhật thông tin tài khoản
     */
    public boolean updateAccount(int accountID, String newUsername, String newPassword, String email, Role role) {
        validateAccountData(newUsername, newPassword, email);

        Account existingAccount = accountRepository.selectById(accountID);
        if (existingAccount == null) {
            throw new AccountException("Tài khoản không tồn tại!");
        }

        if (!existingAccount.getUserName().equals(newUsername) && accountRepository.isAccountExist(newUsername)) {
            throw new AccountException("Tên đăng nhập mới đã được sử dụng!");
        }

        existingAccount.setUserName(newUsername);
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            existingAccount.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt())); // 🔹 Hash mật khẩu mới
        }

        existingAccount.setEmail(email);
        existingAccount.setRole(role);

        return accountRepository.update(existingAccount) > 0;
    }

    public boolean updatePassword(int accountID, String newPassword) {
        try {
            return accountRepository.updatePassword(accountID, newPassword);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa tài khoản (Không xóa tài khoản admin mặc định)
     */
    public boolean deleteAccount(int accountID) {
        Account account = accountRepository.selectById(accountID);
        if (account == null) {
            throw new AccountException("Tài khoản không tồn tại!");
        }

        if ("Admin".equalsIgnoreCase(account.getRole().getRoleName())) {
            throw new AccountException("Không thể xóa tài khoản Admin!");
        }

        return accountRepository.delete(account) > 0;
    }


    /**
     * Lấy danh sách tất cả tài khoản
     */
    public List<Account> getAllAccounts() {
        return accountRepository.selectAll();
    }

    /**
     * Kiểm tra dữ liệu tài khoản
     */
    public void validateAccountData(String username, String password, String email) {
        if (username == null || username.trim().isEmpty()) {
            throw new AccountException("Tên đăng nhập không được để trống!");
        }
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new AccountException("Mật khẩu phải có ít nhất 8 ký tự, chứa ít nhất 1 chữ hoa và 1 ký tự đặc biệt!");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new AccountException("Email không hợp lệ!");
        }
    }
    
    public void validateUsernameAndEmail(String username, String email, int excludeAccountID) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống!");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email không hợp lệ!");
        }

        boolean usernameExists = accountRepository.selectByCondition(
            "UN_UserName = ? AND AccountID != ?", username, excludeAccountID
        ).size() > 0;
        if (usernameExists) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại!");
        }

        // Kiểm tra trùng email
        boolean emailExists = accountRepository.selectByCondition(
            "Email = ? AND AccountID != ?", email, excludeAccountID
        ).size() > 0;
        if (emailExists) {
            throw new IllegalArgumentException("Email đã được sử dụng!");
        }
    }



    private final Map<String, String> verificationCodes = new HashMap<>(); // Lưu mã xác nhận tạm thời

    public void forgotPassword(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new AccountException("Email không hợp lệ!");
        }

        List<Account> accounts = accountRepository.selectByCondition("Email = ?", email);
        if (accounts.isEmpty()) {
            throw new AccountException("Không tìm thấy tài khoản nào với email này!");
        }

        String code = EmailUtil.sendVerificationCode(email);
        verificationCodes.put(email, code);
    }

    public boolean verifyCode(String email, String code) {
        return verificationCodes.containsKey(email) && verificationCodes.get(email).equals(code);
    }

    public boolean resetPassword(String email, String newPassword, String code) {
        if (!verifyCode(email, code)) {
            throw new AccountException("Mã xác nhận không đúng hoặc đã hết hạn!");
        }

        if (newPassword.length() < 8 || !PASSWORD_PATTERN.matcher(newPassword).matches()) {
            throw new AccountException("Mật khẩu phải có ít nhất 8 ký tự, chứa ít nhất 1 chữ hoa và 1 ký tự đặc biệt!");
        }

        List<Account> accounts = accountRepository.selectByCondition("Email = ?", email);
        for (Account acc : accounts) {
            acc.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            accountRepository.update(acc);
        }

        verificationCodes.remove(email); // Xóa mã sau khi đặt lại mật khẩu
        return true;
    }
}
