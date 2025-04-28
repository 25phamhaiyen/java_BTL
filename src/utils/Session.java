package utils;

import model.Account;
import model.Role;
import model.Staff;
import service.StaffService;

public class Session {
    private static Account currentAccount;
    private static Staff currentStaff;
    
    private Session() {
        // Private constructor for singleton
    }
    
    
    public static void setCurrentAccount(Account account) {
        currentAccount = account;
        
        // If an account is set, also try to find the associated staff
        if (account != null) {
            StaffService staffService = new StaffService();
            currentStaff = staffService.getStaffByAccountID(account.getAccountID());
        } else {
            currentStaff = null;
        }
    }

    public static Account getCurrentAccount() {
        return currentAccount;
    }
    public static Role getUserRole() {
        if (currentAccount != null) {
            return currentAccount.getRole(); 
        }
        return null;
    }

    public static Staff getCurrentStaff() {
        return currentStaff;
    }

    public static void clearSession() {
        currentAccount = null;
        currentStaff = null;
    }
 // Đăng xuất
    public static void logout() {
    	try {
			currentAccount = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    // Add the logout method as an alias for clearSession
    public static void logout() {
        clearSession();
    }
}