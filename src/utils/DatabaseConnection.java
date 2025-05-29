package utils;

import java.io.FileInputStream;
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
        try (FileInputStream fis = new FileInputStream("resources\\database.properties")) {
            properties.load(fis);
            URL = properties.getProperty("url");
            USERNAME = properties.getProperty("username");
            PASSWORD = properties.getProperty("password");
            DRIVER = properties.getProperty("driver");
            
            // Load driver
            Class.forName(DRIVER);
            PaymentLogger.info("Database driver loaded successfully");
            
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file cấu hình database: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Lỗi: Không tìm thấy MySQL JDBC Driver!");
            e.printStackTrace();
        }
    }

    // Kết nối đến database
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Tạo kết nối
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            return conn;
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối DB: " + e.getMessage());
            
            // Nếu database không tồn tại, thử tạo database
            if (e.getMessage().contains("Unknown database")) {
                if (createDatabase()) {
                    // Thử kết nối lại sau khi tạo database
                    try {
                        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
                    } catch (SQLException e2) {
                        System.err.println("Vẫn không thể kết nối sau khi tạo database: " + e2.getMessage());
                    }
                }
            }
            return null;
        }
    }
    
    /**
     * Tạo cơ sở dữ liệu nếu chưa tồn tại
     */
    private static boolean createDatabase() {
        // Trích xuất tên database từ URL
        String dbName = URL.substring(URL.lastIndexOf("/") + 1);
        String rootUrl = URL.substring(0, URL.lastIndexOf("/")) + "/";
        
        try (Connection rootConn = DriverManager.getConnection(rootUrl, USERNAME, PASSWORD);
             java.sql.Statement stmt = rootConn.createStatement()) {
            
            // Tạo database
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
            System.out.println("Database '" + dbName + "' đã được tạo thành công!");
            return true;
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi tạo database: " + e.getMessage());
            return false;
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