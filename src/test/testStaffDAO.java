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
        Staff newStaff = new Staff(0, "Nguyen", "Van A", GenderEnum.MALE, "123456789", "CIT001", "Hanoi", role);

        try {
           
            List<Staff> existing = staffDAO.selectByCondition(
                "phoneNumber = ?", 
                newStaff.getPhoneNumber()
            );

            if (!existing.isEmpty()) {
                System.out.println("Số điện thoại " + newStaff.getPhoneNumber() + " đã tồn tại. Sử dụng bản ghi hiện có.");
                newStaff = existing.get(0);
            } else {
                int insertResult = staffDAO.insert(newStaff);
                System.out.println("Insert Result: " + insertResult + " row(s) affected");
                System.out.println("Inserted Staff ID: " + newStaff.getStaffID());
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi thêm mới: " + e.getMessage());
        }
        System.out.println("\n");

        // 2. List all Staff
        System.out.println("SELECT ALL");
        List<Staff> staffList = staffDAO.selectAll();
        System.out.println("Total staffs: " + staffList.size());
        for (Staff staff : staffList) {
            System.out.println(staff);
        }
        System.out.println("\n");

        // 3. UPDATE Staff 
        if (!staffList.isEmpty()) {
            System.out.println("UPDATE");
            Staff updateStaff = staffList.get(0);
            System.out.println("Before update:");
            System.out.println(updateStaff);

            String newPhoneNumber = "555555555";

            try {
    
                List<Staff> checkDup = staffDAO.selectByCondition(
                    "phoneNumber = ? AND staffID != ?", 
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
            System.out.println("\n");
        }

        // 4. SELECT Staff BY ID
        if (!staffList.isEmpty()) {
            System.out.println("SELECT BY ID");
            int testId = staffList.get(0).getStaffID();
            Staff foundStaff = staffDAO.selectById(new Staff(testId, null, null, null, null, null, null, null));
            System.out.println("Staff found by ID " + testId + ":");
            System.out.println(foundStaff);
            System.out.println("\n");
        }

        // 5. SELECT Staff  BY CONDITIO
        System.out.println("SELECT BY CONDITION");
        String searchLastName = "Nguyen";
        List<Staff> filteredStaffs = staffDAO.selectByCondition(
            "lastName = ?", 
            searchLastName
        );
        System.out.println("Found " + filteredStaffs.size() + " staff(s) with last name " + searchLastName + ":");
        for (Staff staff : filteredStaffs) {
            System.out.println(staff);
        }
        System.out.println("\n");

        // 6.Delete Staff 
        System.out.println("DELETE");
        Role tempRole = new Role(1, "Manager");
        Staff tempStaff = new Staff(0, "Pham", "Van C", GenderEnum.MALE, "111222333", "CIT003", "Da Nang", tempRole);

        try {
       
            List<Staff> checkTemp = staffDAO.selectByCondition(
                "phoneNumber = ?", 
                tempStaff.getPhoneNumber()
            );

            if (!checkTemp.isEmpty()) {
                System.out.println("Sử dụng bản ghi có sẵn để test xóa");
                tempStaff = checkTemp.get(0);
            } else {
                staffDAO.insert(tempStaff);
                System.out.println("Đã tạo bản ghi tạm với ID: " + tempStaff.getStaffID());
            }

            int deleteResult = staffDAO.delete(tempStaff);
            System.out.println("Delete Result: " + deleteResult + " row(s) affected");

            // Kiểm tra lại sau khi xóa
            Staff deletedStaff = staffDAO.selectById(tempStaff);
            System.out.println("Staff after deletion: " + (deletedStaff == null ? "Not found (deleted successfully)" : "Still exists"));
        } catch (Exception e) {
            System.err.println("Lỗi trong quá trình xóa: " + e.getMessage());
        }
        System.out.println("\n");
    }
}