package tests;

import database.DatabaseConnection;

public class testConnection {
	public static void main(String[] args) {
		
		DatabaseConnection.printInfo(DatabaseConnection.getConnection());
	}
}
