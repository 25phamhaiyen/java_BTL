package test;

import java.sql.Connection;

import datatbase.JDBC_Util;

public class testJDBC {
	public static void main(String[] args) {
		Connection connection = JDBC_Util.getConnection();
		JDBC_Util.printInfo(connection);
		
		JDBC_Util.closeConnection(connection);
		System.out.println(connection);
	}
}
