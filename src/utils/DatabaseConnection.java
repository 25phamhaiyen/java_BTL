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
        try (FileInputStream fis = new FileInputStream("resource/database.properties")) {
            properties.load(fis);
            URL = properties.getProperty("url");
            USERNAME = properties.getProperty("username");
            PASSWORD = properties.getProperty("password");
            DRIVER = properties.getProperty("driver");
            Class.forName(DRIVER);
            System.out.println("Database driver loaded successfully.");
        } catch (IOException e) {
            System.err.println("Error reading database configuration file: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Error: MySQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }

    // Get database connection
    public static Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connected to database successfully!");
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
        return conn;
    }

    // Close connection
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Connection closed successfully.");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    // Print database info
    public static void printInfo(Connection conn) {
        if (conn != null) {
            try {
                DatabaseMetaData metaData = conn.getMetaData();
                System.out.println("Database: " + metaData.getDatabaseProductName());
                System.out.println("Version: " + metaData.getDatabaseProductVersion());
            } catch (SQLException e) {
                System.err.println("Error getting database info: " + e.getMessage());
            }
        }
    }
}