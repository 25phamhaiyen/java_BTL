package test;

import java.util.List;
import dao.StaffDAO;
import dao.AccountDAO;
import entity.Account;
import entity.Role;
import entity.Staff;
import Enum.GenderEnum;

public class testStaffDAO {
    public static void main(String[] args) {
        StaffDAO staffDAO = new StaffDAO();
        AccountDAO accountDAO = AccountDAO.getInstance();

        // 1. Tạo tài khoản trước khi tạo nhân viên
        System.out.println("TẠO TÀI KHOẢN");
        Role managerRole = new Role(1, "Manager");
        
        Account managerAccount = new Account();
        managerAccount.setUserName("manager_test");
        managerAccount.setPassword("password123");
        managerAccount.setEmail("manager_test@bestpets.com");
        managerAccount.setRole(managerRole);
        
        try {
            // Kiểm tra nếu tài khoản đã tồn tại
            Account existingAccount = accountDAO.getAccountByUsername(managerAccount.getUserName());
            if (existingAccount != null) {
                System.out.println("Tài khoản đã tồn tại, sử dụng AccountID: " + existingAccount.getAccountID());
                managerAccount = existingAccount;
            } else {
                int accountResult = accountDAO.insert(managerAccount);
                if (accountResult > 0) {
                    System.out.println("Tạo tài khoản thành công. AccountID: " + managerAccount.getAccountID());
                } else {
                    System.out.println("Không thể tạo tài khoản");
                    return;
                }
            }

            // 2. INSERT NHÂN VIÊN
            System.out.println("\nTHÊM NHÂN VIÊN");
            String uniqueCitizenNumber = "CIT" + System.currentTimeMillis() % 1000000;
            if (uniqueCitizenNumber.length() > 12) {
                uniqueCitizenNumber = uniqueCitizenNumber.substring(0, 12);
            }
            
            Staff newStaff = new Staff(
                0, "Nguyen", "Van A", GenderEnum.MALE, 
                "0123456789", "123456789012", "Hanoi", 
                managerRole, managerAccount.getAccountID() // Sử dụng AccountID từ tài khoản vừa tạo
            );
            
            try {
                newStaff.validate();
                
             
                List<Staff> existingPhone = staffDAO.selectByCondition(
                    "phoneNumber = ?", 
                    newStaff.getPhoneNumber()
                );

                if (!existingPhone.isEmpty()) {
                    System.out.println("Số điện thoại " + newStaff.getPhoneNumber() + " đã tồn tại. Sử dụng bản ghi hiện có.");
                    newStaff = existingPhone.get(0);
                } 
         
                else if (!staffDAO.selectByCondition("citizenNumber = ?", newStaff.getCitizenNumber()).isEmpty()) {
                    System.out.println("Số CCCD " + newStaff.getCitizenNumber() + " đã tồn tại.");
                }
                else {
                    int insertResult = staffDAO.insert(newStaff);
                    if (insertResult > 0) {
                        System.out.println("Thêm nhân viên thành công. StaffID: " + newStaff.getStaffID());
                        
                
                        Staff addedStaff = staffDAO.selectById(newStaff.getStaffID());
                        System.out.println("Thông tin chi tiết:\n" + addedStaff);
                    } else {
                        System.out.println("Thêm nhân viên không thành công");
                    }
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Lỗi dữ liệu nhân viên: " + e.getMessage());
            }

            // 3. DANH SÁCH NHÂN VIÊN
            System.out.println("\nDANH SÁCH NHÂN VIÊN");
            List<Staff> staffList = staffDAO.selectAll();
            System.out.println("Tổng số nhân viên: " + staffList.size());
            for (Staff staff : staffList) {
                System.out.println(staff);
            }
            
            // 4. CẬP NHẬT NHÂN VIÊN (nếu có dữ liệu)
            if (!staffList.isEmpty()) {
                System.out.println("\nCẬP NHẬT NHÂN VIÊN");
                Staff updateStaff = staffList.get(0);
                System.out.println("Trước khi cập nhật:");
                System.out.println(updateStaff);

                String newPhoneNumber = "0987654321"; 

                try {
                   
                    List<Staff> checkDup = staffDAO.selectByCondition(
                        "phoneNumber = ? AND StaffID != ?", 
                        newPhoneNumber,
                        updateStaff.getStaffID()
                    );

                    if (!checkDup.isEmpty()) {
                        System.out.println("Không thể cập nhật - Số điện thoại " + newPhoneNumber + " đã tồn tại");
                    } else {
                        updateStaff.setLastName("Le");
                        updateStaff.setPhoneNumber(newPhoneNumber);
                        int updateResult = staffDAO.update(updateStaff);
                        System.out.println("Kết quả cập nhật: " + updateResult + " bản ghi bị ảnh hưởng");

                        // In thông tin sau cập nhật
                        Staff updatedStaff = staffDAO.selectById(updateStaff);
                        System.out.println("Sau khi cập nhật:");
                        System.out.println(updatedStaff);
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi cập nhật: " + e.getMessage());
                }
                
                // 5. TÌM NHÂN VIÊN THEO ID
                System.out.println("\nTÌM NHÂN VIÊN THEO ID");
                Staff foundStaff = staffDAO.selectById(updateStaff.getStaffID());
                System.out.println("Nhân viên có ID " + updateStaff.getStaffID() + ":");
                System.out.println(foundStaff);
                
                // 6. TÌM NHÂN VIÊN THEO ĐIỀU KIỆN
                System.out.println("\nTÌM NHÂN VIÊN THEO HỌ");
                String searchLastName = "Nguyen";
                List<Staff> filteredStaffs = staffDAO.selectByCondition(
                    "lastName = ?", 
                    searchLastName
                );
                System.out.println("Tìm thấy " + filteredStaffs.size() + " nhân viên có họ " + searchLastName + ":");
                for (Staff staff : filteredStaffs) {
                    System.out.println(staff);
                }
                
                // 7. XÓA NHÂN VIÊN TẠM
                System.out.println("\nXÓA NHÂN VIÊN TẠM");
                // Tạo nhân viên tạm với thông tin ngẫu nhiên
                String tempPhone = "09" + (System.currentTimeMillis() % 100000000);
                if (tempPhone.length() > 10) {
                    tempPhone = tempPhone.substring(0, 10);
                }
                String tempCitizen = "TEMP" + (System.currentTimeMillis() % 100000000);
                if (tempCitizen.length() > 12) {
                    tempCitizen = tempCitizen.substring(0, 12);
                }
                
                // Tạo tài khoản tạm trước
                Account tempAccount = new Account();
                tempAccount.setUserName("temp_account");
                tempAccount.setPassword("temp123");
                tempAccount.setEmail("temp@bestpets.com");
                tempAccount.setRole(managerRole);
                
                // Thêm tài khoản tạm
                if (accountDAO.getAccountByUsername(tempAccount.getUserName()) == null) {
                    accountDAO.insert(tempAccount);
                } else {
                    tempAccount = accountDAO.getAccountByUsername(tempAccount.getUserName());
                }
                
                Staff tempStaff = new Staff(
                    0, "Pham", "Van C", GenderEnum.MALE, 
                    tempPhone, "321456789023", "Da Nang", 
                    managerRole, tempAccount.getAccountID()
                );

                try {
                    // Thêm nhân viên tạm nếu chưa tồn tại
                    if (staffDAO.selectByCondition("phoneNumber = ?", tempStaff.getPhoneNumber()).isEmpty()) {
                        staffDAO.insert(tempStaff);
                        System.out.println("Đã tạo nhân viên tạm với ID: " + tempStaff.getStaffID());
                    }

                    // Thực hiện xóa
                    int deleteResult = staffDAO.delete(tempStaff);
                    System.out.println("Kết quả xóa: " + deleteResult + " bản ghi bị ảnh hưởng");

                    // Kiểm tra sau khi xóa
                    Staff deletedStaff = staffDAO.selectById(tempStaff.getStaffID());
                    System.out.println("Nhân viên sau khi xóa: " + 
                        (deletedStaff == null ? "Không tìm thấy (xóa thành công)" : "Vẫn tồn tại"));
                } catch (Exception e) {
                    System.err.println("Lỗi khi xóa nhân viên: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi hệ thống: " + e.getMessage());
            e.printStackTrace();
        }
    }
}