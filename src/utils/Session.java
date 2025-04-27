package utils;

import model.Account;
import model.Staff;
import service.StaffService;

public class Session {
    private static volatile Session instance; // Thread-safe Singleton
    private Account currentAccount;
    private Staff currentStaff;

    // Private constructor for Singleton
    private Session() {}

    // Thread-safe Singleton instance retrieval
    public static Session getInstance() {
        if (instance == null) {
            synchronized (Session.class) {
                if (instance == null) {
                    instance = new Session();
                }
            }
        }
        return instance;
    }

    // Set the current account and update the associated staff
    public void setCurrentAccount(Account account) {
        this.currentAccount = account;

        if (account != null) {
            StaffService staffService = new StaffService();
            this.currentStaff = staffService.getStaffByAccountID(account.getAccountID());
        } else {
            this.currentStaff = null;
        }
    }

    // Get the current account
    public static Account getCurrentAccount() {
        return getInstance().currentAccount;
    }

    // Get the current staff
    public static Staff getCurrentStaff() {
        return getInstance().currentStaff;
    }

    // Clear the session (logout)
    public static void clearSession() {
        getInstance().currentAccount = null;
        getInstance().currentStaff = null;
    }

    // Alias for clearSession
    public static void logout() {
        clearSession();
    }
}
