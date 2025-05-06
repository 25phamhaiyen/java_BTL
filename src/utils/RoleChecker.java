package utils;

import java.util.HashMap;
import java.util.Map;
import model.Staff;
import utils.Session;

public class RoleChecker {

	private static Map<String, Boolean> staffPermissions = new HashMap<>();

	/**
	 * Kiểm tra xem người dùng hiện tại có quyền cụ thể hay không
	 * 
	 * @param permissionCode Mã quyền cần kiểm tra
	 * @return true nếu có quyền, false nếu không có
	 */
	public static boolean hasPermission(String permissionCode) {
		// Lấy thông tin nhân viên hiện tại từ session
		Staff currentStaff = Session.getCurrentStaff();
		if (currentStaff == null) {
			return false;
		}

		// Nếu là admin thì có tất cả quyền
		if ("ADMIN".equalsIgnoreCase(currentStaff.getRole().getRoleName())) {
			return true;
		}

		// Kiểm tra cache trước
		String key = currentStaff.getId() + "_" + permissionCode;
		if (staffPermissions.containsKey(key)) {
			return staffPermissions.get(key);
		}

		// Lấy vai trò của nhân viên
		String roleName = currentStaff.getRole().getRoleName();

		// Phân quyền dựa trên vai trò
		boolean hasPermission = false;
		switch (roleName.toUpperCase()) {
		case "STAFF_CARE":
			hasPermission = permissionCode.equals("VIEW_SCHEDULE") || permissionCode.equals("REGISTER_SHIFT")
					|| permissionCode.equals("REQUEST_LEAVE") || permissionCode.equals("VIEW_BOOKING_ASSIGNED")
					|| permissionCode.equals("MARK_SERVICE_DONE");
			break;
		case "STAFF_CASHIER":
			hasPermission = permissionCode.equals("VIEW_SCHEDULE") || permissionCode.equals("REGISTER_SHIFT")
					|| permissionCode.equals("REQUEST_LEAVE") || permissionCode.equals("VIEW_INVOICE")
					|| permissionCode.equals("VIEW_BOOKING_ASSIGNED") || permissionCode.equals("MANAGE_PAYMENT")
					|| permissionCode.equals("PRINT_RECEIPT") || permissionCode.equals("CREATE_BOOKING")
					|| permissionCode.equals("APPLY_PROMOTION");
			break;
		case "STAFF_RECEPTION":
			hasPermission = permissionCode.equals("VIEW_SCHEDULE") || permissionCode.equals("REGISTER_SHIFT")
					|| permissionCode.equals("REQUEST_LEAVE") || permissionCode.equals("VIEW_BOOKING_ASSIGNED")
					|| permissionCode.equals("CREATE_BOOKING") || permissionCode.equals("MARK_SERVICE_DONE")
					|| permissionCode.equals("PRINT_RECEIPT");
			break;
		default:
			hasPermission = false;
			break;
		}

		// Lưu vào cache
		staffPermissions.put(key, hasPermission);
		return hasPermission;
	}

	/**
	 * Xóa cache quyền của một nhân viên cụ thể
	 * 
	 * @param staffId ID của nhân viên
	 */
	public static void clearPermissionCache(int staffId) {
		staffPermissions.entrySet().removeIf(entry -> entry.getKey().startsWith(staffId + "_"));
	}

	/**
	 * Xóa toàn bộ cache quyền
	 */
	public static void clearAllPermissionCache() {
		staffPermissions.clear();
	}
}