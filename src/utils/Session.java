package utils;

import model.Account;
import model.Role;
import model.Staff;
import service.StaffService;

import java.util.HashMap;
import java.util.Map;

public class Session {
	private static Session instance;
	private static Account currentAccount;
	private static Staff currentStaff;
	private Map<String, Object> attributes;

	private Session() {
		attributes = new HashMap<>();
	}

	public static Session getInstance() {
		if (instance == null) {
			instance = new Session();
		}
		return instance;
	}

	public void setAttribute(String key, Object value) {
		attributes.put(key, value);
	}

	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	public void removeAttribute(String key) {
		attributes.remove(key);
	}

	public static void setCurrentAccount(Account account) {
		currentAccount = account;
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
		if (instance != null) {
			instance.attributes.clear();
		}
	}

	public static void logout() {
		clearSession();
	}
}