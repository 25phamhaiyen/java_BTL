package datatbase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.mysql.cj.jdbc.Driver;

public class JDBC_Util {
	public static Connection getConnection() {
		Connection c = null;
		try {
			// đăng kí MySql Driver với DriverManager
			DriverManager.registerDriver(new com.mysql.jdbc.Driver());
			// các thông số
			String url = "jdbc:mySQL://localhost:3306/bestpets";
			String username = "root";
			String password = "25122005yen";
			//tạo kết nối
			c = DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return c;
	}
	public static void closeConnection(Connection c) {
		try {
			if(c!=null) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void printInfo(Connection c) {
		try {
			if(c != null) {
				DatabaseMetaData mtdt = c.getMetaData();
				System.out.println(mtdt.getDatabaseProductName());
				System.out.println(mtdt.getDatabaseProductVersion());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
