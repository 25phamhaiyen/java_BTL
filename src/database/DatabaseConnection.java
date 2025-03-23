package database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/bestpets";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "25122005yen";

    // Kết nối đến database
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Đăng ký MySQL Driver (chỉ cần gọi Class.forName)
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Tạo kết nối
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("Lỗi: Không tìm thấy MySQL JDBC Driver!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối DB: " + e.getMessage());
        }
        return conn;
    }

    // Đóng kết nối
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Đã đóng kết nối thành công.");
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng kết nối: " + e.getMessage());
            }
        }
    }

    // In thông tin database
    public static void printInfo(Connection conn) {
        if (conn != null) {
            try {
                DatabaseMetaData metaData = conn.getMetaData();
                System.out.println("Database: " + metaData.getDatabaseProductName());
                System.out.println("Version: " + metaData.getDatabaseProductVersion());
            } catch (SQLException e) {
                System.err.println("Lỗi khi lấy thông tin DB: " + e.getMessage());
            }
        }
    }
}
