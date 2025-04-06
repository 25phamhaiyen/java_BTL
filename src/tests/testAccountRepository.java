package tests;

import model.Account;
import model.Role;
import repository.AccountRepository;

import java.util.List;


public class testAccountRepository {
    public static void main(String[] args) {
        AccountRepository accountRepository = AccountRepository.getInstance();

        // 1️⃣ Thêm tài khoản mới
        Role role=new Role(2, "Admin");
        Account newAccount = new Account(0, "admin", "password123", "admin@example.com", role);
        accountRepository.insert(newAccount);
        int insertResult = accountRepository.insert(newAccount);
        System.out.println("Insert Result: " + insertResult);
        System.out.println("Inserted Account ID: " + newAccount.getAccountID());

        // 2️⃣ Lấy danh sách tất cả tài khoản
        System.out.println("\nDanh sách tài khoản:");
        List<Account> accountList = accountRepository.selectAll();
        for (Account acc : accountList) {
            System.out.println(acc);
        }

        // 3️⃣ Tìm tài khoản theo ID
        int searchID = 2;
        Account foundAccount = accountRepository.selectById(searchID);
        System.out.println("\nTài khoản tìm thấy: " + foundAccount);

        // 4️⃣ Cập nhật thông tin tài khoản
//        if (foundAccount != null) {
//            foundAccount.setPassword("newPassword456");
//            foundAccount.setEmail("newemail@gmail.com");
//            int updateResult = accountDAO.update(foundAccount);
//            System.out.println("\nUpdate Result: " + updateResult);
//
//            // Kiểm tra lại sau khi cập nhật
//            Account updatedAccount = accountDAO.selectById(searchID);
//            System.out.println("Tài khoản sau cập nhật: " + updatedAccount);
//        }
         	
        // 5️⃣ Truy vấn tài khoản theo điều kiện (Role = ADMIN)
        System.out.println("\nDanh sách tài khoản ADMIN:");
        List<Account> customerAccounts = accountRepository.selectByCondition("Role_ID=?", 2);
        for (Account acc : customerAccounts) {
            System.out.println(acc);
        }

        // 6️⃣ Xóa tài khoản
//        if (foundAccount != null) {
//            int deleteResult = accountDAO.delete(foundAccount);
//            System.out.println("\nDelete Result: " + deleteResult);
//
//            // Kiểm tra danh sách sau khi xóa
//            System.out.println("\nDanh sách tài khoản sau khi xóa:");
//            List<Account> accountListAfterDelete = accountDAO.selectAll();
//            for (Account acc : accountListAfterDelete) {
//                System.out.println(acc);
//            }
//        }
    }
}
