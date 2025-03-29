package test;

import dao.AccountDAO;
import entity.Account;
import entity.Role;

import java.util.List;


public class testAccountDAO {
    public static void main(String[] args) {
        AccountDAO accountDAO = AccountDAO.getInstance();

        // 1️⃣ Thêm tài khoản mới
        Role role=new Role(4, "Manager");
        Account newAccount = new Account(0, "user12", "password12", "user12@gmail.com", role);
        int insertResult = accountDAO.insert(newAccount);
        System.out.println("Insert Result: " + insertResult);
        System.out.println("Inserted Account ID: " + newAccount.getAccountID());

        // 2️⃣ Lấy danh sách tất cả tài khoản
        System.out.println("\nDanh sách tài khoản:");
        List<Account> accountList = accountDAO.selectAll();
        for (Account acc : accountList) {
            System.out.println(acc);
        }

        // 3️⃣ Tìm tài khoản theo ID
        int searchID = 2;
        Account foundAccount = accountDAO.selectById(searchID);
        System.out.println("\nTài khoản tìm thấy: " + foundAccount);

        // 4️⃣ Cập nhật thông tin tài khoản
        if (foundAccount != null) {
            foundAccount.setPassword("newPassword456");
            foundAccount.setEmail("newemail@gmail.com");
            int updateResult = accountDAO.update(foundAccount);
            System.out.println("\nUpdate Result: " + updateResult);

            // Kiểm tra lại sau khi cập nhật
            Account updatedAccount = accountDAO.selectById(searchID);
            System.out.println("Tài khoản sau cập nhật: " + updatedAccount);
        }

        // 5️⃣ Truy vấn tài khoản theo điều kiện (Role = CUSTOMER)
        System.out.println("\nDanh sách tài khoản CUSTOMER:");
        List<Account> customerAccounts = accountDAO.selectByCondition("Role_ID=?", "1");
        for (Account acc : customerAccounts) {
            System.out.println(acc);
        }

        // 6️⃣ Xóa tài khoản
        if (foundAccount != null) {
            int deleteResult = accountDAO.delete(foundAccount);
            System.out.println("\nDelete Result: " + deleteResult);

            // Kiểm tra danh sách sau khi xóa
            System.out.println("\nDanh sách tài khoản sau khi xóa:");
            List<Account> accountListAfterDelete = accountDAO.selectAll();
            for (Account acc : accountListAfterDelete) {
                System.out.println(acc);
            }
        }
    }
}
