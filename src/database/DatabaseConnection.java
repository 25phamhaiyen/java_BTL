package database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
	private static final String URL = "jdbc:mysql://localhost:3306/bestpets";
	private static final String USER = "root";
	private static final String PASSWORD = "Kythanh10062005@";

	// Phương thức lấy kết nối
	public static Connection getConnection() {
		try {
			return DriverManager.getConnection(URL, USER, PASSWORD);
		} catch (SQLException e) {
			System.err.println("Lỗi kết nối Database: " + e.getMessage());
			return null;
		}
	}

	// Phương thức đóng kết nối
	public static void closeConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				System.err.println("Không thể đóng kết nối: " + e.getMessage());
			}
		}
	}

	// In thông tin Database
	public static void printInfo() {
		try (Connection conn = getConnection()) {
			if (conn != null) {
				DatabaseMetaData metaData = conn.getMetaData();
				System.out.println("Database: " + metaData.getDatabaseProductName());
				System.out.println("Version: " + metaData.getDatabaseProductVersion());
			}
		} catch (SQLException e) {
			System.err.println("Lỗi lấy thông tin DB: " + e.getMessage());
		}
	}
}
