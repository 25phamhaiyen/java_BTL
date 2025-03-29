package test;

import java.util.List;
import dao.StaffDAO;
import entity.Role;
import entity.Staff;
import Enum.GenderEnum;

public class testStaffDAO {
    public static void main(String[] args) {
        StaffDAO staffDAO = new StaffDAO();

        // 1. INSERT
        System.out.println("INSERT");
        Role role = new Role(1, "Manager"); // Giả định roleID 1 tồn tại
        
        // Tạo citizenNumber ngẫu nhiên để tránh trùng lặp
        String uniqueCitizenNumber = "CIT" + System.currentTimeMillis() % 1000000;
        if (uniqueCitizenNumber.length() > 12) {
            uniqueCitizenNumber = uniqueCitizenNumber.substring(0, 12);
        }
        
        Staff newStaff = new Staff(0, "Nguyen", "Van A", GenderEnum.MALE, 
                                 "0123456789", "123456789012", "Hanoi", role, 1);
        
        try {
            // Validate trước khi insert
            newStaff.validate();
            
            // Kiểm tra trùng số điện thoại
            List<Staff> existingPhone = staffDAO.selectByCondition(
                "phoneNumber = ?", 
                newStaff.getPhoneNumber()
            );

            if (!existingPhone.isEmpty()) {
                System.out.println("Số điện thoại " + newStaff.getPhoneNumber() + " đã tồn tại. Sử dụng bản ghi hiện có.");
                newStaff = existingPhone.get(0);
            } 
            // Kiểm tra trùng CCCD
            else if (!staffDAO.selectByCondition("citizenNumber = ?", newStaff.getCitizenNumber()).isEmpty()) {
                System.out.println("Số CCCD " + newStaff.getCitizenNumber() + " đã tồn tại.");
            }
            else {
                int insertResult = staffDAO.insert(newStaff);
                if (insertResult > 0) {
                    System.out.println("Insert thành công. ID: " + newStaff.getStaffID());
                } else {
                    System.out.println("Insert không thành công");
                }
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Lỗi dữ liệu: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Lỗi hệ thống: " + e.getMessage());
        }
        System.out.println("\n");

        // 2. List all Staff
        System.out.println("SELECT ALL");
        try {
            List<Staff> staffList = staffDAO.selectAll();
            System.out.println("Total staffs: " + staffList.size());
            for (Staff staff : staffList) {
                System.out.println(staff);
            }
            
            // 3. UPDATE Staff (nếu có dữ liệu)
            if (!staffList.isEmpty()) {
                System.out.println("\nUPDATE");
                Staff updateStaff = staffList.get(0);
                System.out.println("Before update:");
                System.out.println(updateStaff);

                String newPhoneNumber = "0987654321"; // Số điện thoại mới hợp lệ

                try {
                    // Kiểm tra số điện thoại mới có trùng không
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
                        System.out.println("Update Result: " + updateResult + " row(s) affected");

                        // In ra thông tin sau khi cập nhật
                        Staff updatedStaff = staffDAO.selectById(updateStaff);
                        System.out.println("After update:");
                        System.out.println(updatedStaff);
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi cập nhật: " + e.getMessage());
                }
                
                // 4. SELECT BY ID
                System.out.println("\nSELECT BY ID");
                int testId = updateStaff.getStaffID();
                Staff foundStaff = staffDAO.selectById(testId);
                System.out.println("Staff found by ID " + testId + ":");
                System.out.println(foundStaff);
                
                // 5. SELECT BY CONDITION
                System.out.println("\nSELECT BY CONDITION");
                String searchLastName = "Nguyen";
                List<Staff> filteredStaffs = staffDAO.selectByCondition(
                    "lastName = ?", 
                    searchLastName
                );
                System.out.println("Found " + filteredStaffs.size() + " staff(s) with last name " + searchLastName + ":");
                for (Staff staff : filteredStaffs) {
                    System.out.println(staff);
                }
                
                // 6. DELETE Staff 
                System.out.println("\nDELETE");
                // Tạo staff tạm với thông tin ngẫu nhiên
                String tempPhone = "09" + (System.currentTimeMillis() % 100000000);
                if (tempPhone.length() > 10) {
                    tempPhone = tempPhone.substring(0, 10);
                }
                String tempCitizen = "TEMP" + (System.currentTimeMillis() % 100000000);
                if (tempCitizen.length() > 12) {
                    tempCitizen = tempCitizen.substring(0, 12);
                }
                
                Staff tempStaff = new Staff(0, "Pham", "Van C", GenderEnum.MALE, 
                                          tempPhone, tempCitizen, "Da Nang", role, 1);

                try {
                    // Thêm mới nếu chưa tồn tại
                    if (staffDAO.selectByCondition("phoneNumber = ?", tempStaff.getPhoneNumber()).isEmpty()) {
                        staffDAO.insert(tempStaff);
                        System.out.println("Đã tạo bản ghi tạm với ID: " + tempStaff.getStaffID());
                    }

                    // Thực hiện xóa
                    int deleteResult = staffDAO.delete(tempStaff);
                    System.out.println("Delete Result: " + deleteResult + " row(s) affected");

                    // Kiểm tra lại sau khi xóa
                    Staff deletedStaff = staffDAO.selectById(tempStaff.getStaffID());
                    System.out.println("Staff after deletion: " + 
                        (deletedStaff == null ? "Not found (deleted successfully)" : "Still exists"));
                } catch (Exception e) {
                    System.err.println("Lỗi trong quá trình xóa: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách nhân viên: " + e.getMessage());
        }
    }
}