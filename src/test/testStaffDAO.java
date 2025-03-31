package test;

import java.util.List;

import Enum.GenderEnum;
import dao.StaffDAO;
import entity.Role;
import entity.Staff;

public class testStaffDAO {
	public static void main(String[] args) {
		// Khởi tạo đối tượng StaffDAO
		StaffDAO staffDAO = new StaffDAO();

		// 1️ Thêm nhân viên mới
		Role role = new Role(1, "Admin"); // Giả sử Role ID = 1 là Admin
		Staff newStaff = new Staff(0, "Nguyen", "Vuong Khang", GenderEnum.MALE, "0963234819", "123456789012", "Long An",
				role, 3);
		int insertResult = staffDAO.insert(newStaff);
		System.out.println("Insert Result: " + insertResult);
		System.out.println("Inserted Staff ID: " + newStaff.getStaffID());

		// 2️ Lấy danh sách tất cả nhân viên
		System.out.println("\nDanh sách nhân viên:");
		List<Staff> staffList = staffDAO.selectAll();
		for (Staff staff : staffList) {
			System.out.println(staff);
		}

		// 3️ Tìm nhân viên theo ID
		int searchID = newStaff.getStaffID(); // Sử dụng ID vừa chèn
		Staff foundStaff = staffDAO.selectById(searchID);
		System.out.println("\nNhân viên tìm thấy: " + foundStaff);

		// 4️ Cập nhật thông tin nhân viên
		if (foundStaff != null) {
			foundStaff.setPhoneNumber("0989999999");
			foundStaff.setAddress("Hồ Chí Minh");
			int updateResult = staffDAO.update(foundStaff);
			System.out.println("\nUpdate Result: " + updateResult);

			// Kiểm tra lại sau khi cập nhật
			Staff updatedStaff = staffDAO.selectById(searchID);
			System.out.println("Nhân viên sau cập nhật: " + updatedStaff);
		}

		// 5️ Truy vấn nhân viên theo điều kiện (Giới tính = MALE)
		System.out.println("\nDanh sách nhân viên nam:");
		List<Staff> maleStaff = staffDAO.selectByCondition("sex=?", GenderEnum.MALE.name());
		for (Staff staff : maleStaff) {
			System.out.println(staff);
		}

		// 6️ Xóa nhân viên
		if (foundStaff != null) {
			int deleteResult = staffDAO.delete(foundStaff);
			System.out.println("\nDelete Result: " + deleteResult);

			// Kiểm tra danh sách sau khi xóa
			System.out.println("\nDanh sách nhân viên sau khi xóa:");
			List<Staff> staffListAfterDelete = staffDAO.selectAll();
			for (Staff staff : staffListAfterDelete) {
				System.out.println(staff);
			}
		}
	}
}
