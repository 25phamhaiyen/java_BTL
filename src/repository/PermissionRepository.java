package repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import model.Permission;
import utils.DatabaseConnection;

public class PermissionRepository implements IRepository<Permission> {

	private static PermissionRepository instance;

	public static PermissionRepository getInstance() {
	    if (instance == null) {
	        synchronized (PermissionRepository.class) {
	            if (instance == null) {
	                instance = new PermissionRepository();
	            }
	        }
	    }
	    return instance;
	}


    @Override
    public int insert(Permission t) {
        int result = 0;
        String sql = "INSERT INTO permission (permission_code, description) VALUES (?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, t.getPermissionCode());
            pstmt.setString(2, t.getDescription());
            result = pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int update(Permission t) {
        int result = 0;
        String sql = "UPDATE permission SET permission_code = ?, description = ? WHERE permission_code = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, t.getPermissionCode());
            pstmt.setString(2, t.getDescription());
            pstmt.setString(3, t.getPermissionCode());

            result = pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int delete(Permission t) {
        int result = 0;
        String sql = "DELETE FROM permission WHERE permission_code = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, t.getPermissionCode());
            result = pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<Permission> selectAll() {
        List<Permission> list = new ArrayList<>();
        String sql = "SELECT * FROM permission";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Permission(rs.getString("permission_code"), rs.getString("description")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Permission selectById(String permission_code) {
    	Permission per = null;
        String sql = "SELECT * FROM permission WHERE permission_code = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, permission_code);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
            	per = new Permission(rs.getString("permission_code"), rs.getString("description"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return per;
    }
    
    @Override
    public Permission selectById(Permission t) {
    	return selectById(t.getPermissionCode()); // Gọi lại phương thức nhận int
    }

    @Override
    public List<Permission> selectByCondition(String condition, Object... params) {
        List<Permission> list = new ArrayList<>();
        
        // Tránh nối chuỗi trực tiếp, sử dụng tham số hóa
        String sql = "SELECT * FROM permission WHERE " + condition;
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            // Truyền tham số vào câu lệnh SQL
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Permission(rs.getString("permission_code"), rs.getString("description")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn permission: " + e.getMessage());
        }
        
        return list;
    }
    

	public List<String> getPermissionsByAccountId(int accountId) {
		List<String> permissions = new ArrayList<>();
		String query = "SELECT p.permission_code FROM permission p " +
					   "JOIN account_permission ap ON p.permission_code = ap.permission_code " +
					   "WHERE ap.account_id = ?";
		try (Connection connection = DatabaseConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setInt(1, accountId);
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				permissions.add(resultSet.getString("permission_code"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return permissions;
	}
	public void assignPermissionToAccount(int accountId, String permissionCode) {
	    String query = "INSERT INTO account_permission (account_id, permission_code) " +
	                   "SELECT ?, permission_code FROM permission WHERE permission_code = ?";
	    try (Connection connection = DatabaseConnection.getConnection();
	         PreparedStatement statement = connection.prepareStatement(query)) {
	        statement.setInt(1, accountId);
	        statement.setString(2, permissionCode);
	        int rowsAffected = statement.executeUpdate();
	        if (rowsAffected == 0) {
	            System.err.println("Không thể gán quyền. Mã quyền không tồn tại: " + permissionCode);
	        }
	    } catch (SQLException e) {
	        System.err.println("Lỗi khi gán quyền cho tài khoản ID: " + accountId + ", Mã quyền: " + permissionCode);
	        e.printStackTrace();
	    }
	}

	public void removePermissionFromAccount(int accountId, String permissionCode) {
	    String query = "DELETE FROM account_permission WHERE account_id = ? " +
	                   "AND permission_code = (SELECT permission_code FROM permission WHERE permission_code = ?)";
	    try (Connection connection = DatabaseConnection.getConnection();
	         PreparedStatement statement = connection.prepareStatement(query)) {
	        statement.setInt(1, accountId);
	        statement.setString(2, permissionCode);
	        int rowsAffected = statement.executeUpdate();
	        if (rowsAffected == 0) {
	            System.err.println("Không thể xóa quyền. Mã quyền không tồn tại hoặc không được gán: " + permissionCode);
	        }
	    } catch (SQLException e) {
	        System.err.println("Lỗi khi xóa quyền cho tài khoản ID: " + accountId + ", Mã quyền: " + permissionCode);
	        e.printStackTrace();
	    }
	}
    public void deletePermissionsByAccountId(int accountId) {
        String sql = "DELETE FROM account_permission WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addPermissionToAccount(int accountId, String permissionCode) {
        String sql = "INSERT INTO account_permission (account_id, permission_code) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setString(2, permissionCode);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}

