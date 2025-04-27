package utils;

import java.util.HashMap;
import java.util.Map;
import model.Account;
import model.Staff;
import repository.AccountPermissionRepository;
import service.StaffService;

public class RoleChecker {
    
    private static Map<String, Boolean> staffPermissions = new HashMap<>();
    
    /**
     * Kiểm tra xem người dùng hiện tại có quyền cụ thể hay không
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
        
        // Nếu chưa có trong cache, truy vấn từ database
        Account account = currentStaff.getAccount();
        if (account == null) {
            return false;
        }
        
        boolean hasPermission = AccountPermissionRepository.getInstance()
                .checkPermission(account.getAccountID(), permissionCode);
        
        // Lưu vào cache
        staffPermissions.put(key, hasPermission);
        
        return hasPermission;
    }
    
    /**
     * Xóa cache quyền của một nhân viên cụ thể
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