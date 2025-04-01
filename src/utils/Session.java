package utils;

import model.Account;
import model.Role;

public class Session {
    private static Account currentUser;

    // Lấy thông tin người dùng đang đăng nhập
    public static Account getCurrentUser() {
        return currentUser;
    }
    
    public static Role getUserRole() {
        if (currentUser != null) {
            return currentUser.getRole(); 
        }
        return null;
    }

    // Đăng xuất
    public static void logout() {
        currentUser = null;
    }

    // Thiết lập người dùng hiện tại
    public static void setCurrentUser(Account user) {
        currentUser = user;
    }

}
