package service;


import repository.PermissionRepository;

import java.util.List;

import model.Permission;

public class PermissionService {
    private final PermissionRepository permissionRepository = new PermissionRepository();

    public List<String> getPermissionsByAccountId(int accountId) {
        return permissionRepository.getPermissionsByAccountId(accountId);
    }

    public List<Permission> getAllPermissions() {
        return permissionRepository.selectAll();
    }

    public void assignPermissionToAccount(int accountId, String permissionCode) {
        permissionRepository.assignPermissionToAccount(accountId, permissionCode);
    }

    public void removePermissionFromAccount(int accountId, String permissionCode) {
        permissionRepository.removePermissionFromAccount(accountId, permissionCode);
    }
    public void updatePermissions(int accountId, List<String> permissions) {
        // Xóa quyền cũ
    	permissionRepository.deletePermissionsByAccountId(accountId);
    
        // Thêm quyền mới
        for (String permission : permissions) {
        	permissionRepository.addPermissionToAccount(accountId, permission);
        }
    }
}

