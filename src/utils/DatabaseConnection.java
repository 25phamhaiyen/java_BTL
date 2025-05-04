package utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final Properties properties = new Properties();
    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;
    private static String DRIVER;

    static {
        try {
            // Tải tệp database.properties từ thư mục resources
            properties.load(DatabaseConnection.class.getClassLoader().getResourceAsStream("database.properties"));
            URL = properties.getProperty("url");
            USERNAME = properties.getProperty("username");
            PASSWORD = properties.getProperty("password");
            DRIVER = properties.getProperty("driver");

            if (URL == null || USERNAME == null || PASSWORD == null || DRIVER == null) {
                throw new IOException("Thiếu thông tin cấu hình trong database.properties");
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file cấu hình database: " + e.getMessage());
            throw new RuntimeException("Không thể khởi tạo DatabaseConnection", e);
        }
    }

    // Kết nối đến database
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DRIVER);
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("Lỗi: Không tìm thấy MySQL JDBC Driver!");
            throw new SQLException("Không tìm thấy MySQL JDBC Driver", e);
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối DB: " + e.getMessage());
            throw e;
        }
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