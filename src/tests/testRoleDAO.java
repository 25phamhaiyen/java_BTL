package tests;

import java.util.List;

import dao.RoleDAO;
import entity.Role;

public class testRoleDAO {
    public static void main(String[] args) {
        RoleDAO roleDAO = RoleDAO.getInstance();

        // 1. Thêm mới Role
        Role newRole = new Role(0, "Manager");
        int insertResult = roleDAO.insert(newRole);
        System.out.println("Insert Result: " + insertResult);
        System.out.println("Inserted Role ID: " + newRole.getRoleID());

        // 2. Lấy danh sách tất cả Role
        System.out.println("\nDanh sách tất cả Role:");
        List<Role> roleList = roleDAO.selectAll();
        for (Role role : roleList) {
            System.out.println(role);
        }

        // 3. Cập nhật Role
        if (!roleList.isEmpty()) {
            Role updateRole = roleList.get(0);
            updateRole.setRoleName("Updated Manager");
            int updateResult = roleDAO.update(updateRole);
            System.out.println("\nUpdate Result: " + updateResult);
        }

        // 4. Tìm Role theo ID
        if (!roleList.isEmpty()) {
            int roleId = roleList.get(0).getRoleID();
            Role foundRole = roleDAO.selectById(roleId);
            System.out.println("\nRole found by ID: " + foundRole);
        }

        // 5. Tìm Role theo điều kiện
        System.out.println("\nDanh sách Role có tên chứa 'Manager':");
        List<Role> filteredRoles = roleDAO.selectByCondition("roleName LIKE ?", "%Manager%");
        for (Role role : filteredRoles) {
            System.out.println(role);
        }

        // 6. Xóa Role
        if (!roleList.isEmpty()) {
            Role deleteRole = roleList.get(roleList.size() - 1);
            int deleteResult = roleDAO.delete(deleteRole);
            System.out.println("\nDelete Result: " + deleteResult);
        }
    }
}
