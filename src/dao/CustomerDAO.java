package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import datatbase.JDBC_Util;
import entity.Customer;

public class CustomerDAO implements DAOInterface<Customer> {

	public static CustomerDAO getInstance() {
		return new CustomerDAO();
	}

	@Override
	public int insert(Customer t) {
		int ketQua = 0; 
		try {
			// Tạo kết nối đến CSDL
			Connection con = JDBC_Util.getConnection();

			// tạo ra đối tượng statement
			Statement st = con.createStatement();

			// thực thi câu lệnh SQL
			String sql = "INSERT INTO customer (customer_ID, lastName, firstName, phoneNumber, sex, citizenNumber, address) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

			// Dùng PreparedStatement để truyền tham số vào câu SQL
			try (PreparedStatement pstmt = con.prepareStatement(sql)) {
				pstmt.setInt(1, t.getCustomer_ID());
				pstmt.setString(2, t.getLastName());
				pstmt.setString(3, t.getFirstName());
				pstmt.setString(4, t.getPhoneNumber());
				pstmt.setInt(5, t.getSex());
				pstmt.setString(6, t.getCitizenNumber());
				pstmt.setString(7, t.getAddress());

				// Thực thi lệnh INSERT
				 ketQua = pstmt.executeUpdate();
			}

			// In thông tin số dòng bị thay đổi
			System.out.println("Bạn đã thực thi INSERT, có " + ketQua + " dòng bị thay đổi.");

			// ngắt kết nối
			JDBC_Util.closeConnection(con);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ketQua;
	}

	@Override
	public int update(Customer t) {
		int ketQua = 0; 
		try {
			// Tạo kết nối đến CSDL
			Connection con = JDBC_Util.getConnection();

			// thực thi câu lệnh SQL
			String sql = "UPDATE customer SET lastName = ?, firstName = ?, phoneNumber = ?, sex = ?, citizenNumber = ?, address = ? WHERE customer_ID = ?";


			// Dùng PreparedStatement để truyền tham số vào câu SQL
			try (PreparedStatement pstmt = con.prepareStatement(sql)) {
				pstmt.setString(1, t.getLastName());
				pstmt.setString(2, t.getFirstName());
				pstmt.setString(3, t.getPhoneNumber());
				pstmt.setInt(4, t.getSex());
				pstmt.setString(5, t.getCitizenNumber());
				pstmt.setString(6, t.getAddress());
				pstmt.setInt(7, t.getCustomer_ID()); // Đặt ID cuối cùng


				ketQua = pstmt.executeUpdate();
			}

			// In thông tin số dòng bị thay đổi
			System.out.println("Bạn đã thực thi UPDATE, có " + ketQua + " dòng bị thay đổi.");

			// ngắt kết nối
			JDBC_Util.closeConnection(con);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ketQua;
	}

	@Override
	public int delete(Customer t) {
		int ketQua = 0; 
		try {
			// Tạo kết nối đến CSDL
			Connection con = JDBC_Util.getConnection();

			// thực thi câu lệnh SQL
			 String sql = "DELETE FROM Customer WHERE customer_ID = ?";
	         PreparedStatement pstmt = con.prepareStatement(sql);
	         pstmt.setInt(1, t.getCustomer_ID());

	         
			 ketQua = pstmt.executeUpdate();
			// In thông tin số dòng bị thay đổi
			System.out.println("Bạn đã thực thi DELETE, có " + ketQua + " dòng bị thay đổi.");

			// ngắt kết nối
			JDBC_Util.closeConnection(con);
 
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ketQua;
	}

	@Override
	public ArrayList<Customer> selectAll() {
		ArrayList<Customer> ketQua = new ArrayList<Customer>();
		try {
			// Tạo kết nối đến CSDL
			Connection con = JDBC_Util.getConnection();

			// thực thi câu lệnh SQL
			 String sql = "SELECT * FROM customer";
	         PreparedStatement pstmt = con.prepareStatement(sql);
	         
	         ResultSet rs = pstmt.executeQuery(sql);

	         while (rs.next()) {
	        	 int id = rs.getInt("customer_ID");
	        	 String lName = rs.getString("lastName");
	        	 String fName = rs.getString("firstName");
	        	 String phone = rs.getString("phoneNumber");
	        	 int sex = rs.getInt("sex");
	        	 String citizenNumber = rs.getString("citizenNumber");
	        	 String address = rs.getString("address");
	        	 
	        	 Customer cus = new Customer(id, lName, fName, phone, sex, citizenNumber, address);
	        	 ketQua.add(cus);
	         }

			// ngắt kết nối
			JDBC_Util.closeConnection(con);
 
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ketQua;
	}

	@Override
	public Customer selectById(Customer t) {
		Customer ketQua = null;
		try {
			// Tạo kết nối đến CSDL
			Connection con = JDBC_Util.getConnection();

			// thực thi câu lệnh SQL
			 String sql = "SELECT * FROM customer where customer_ID=?";
	         PreparedStatement pstmt = con.prepareStatement(sql);
	         pstmt.setInt(1, t.getCustomer_ID());
	         
	         ResultSet rs = pstmt.executeQuery();

	         while (rs.next()) {
	        	 int id = rs.getInt("customer_ID");
	        	 String lName = rs.getString("lastName");
	        	 String fName = rs.getString("firstName");
	        	 String phone = rs.getString("phoneNumber");
	        	 int sex = rs.getInt("sex");
	        	 String citizenNumber = rs.getString("citizenNumber");
	        	 String address = rs.getString("address");
	        	 
	        	 ketQua = new Customer(id, lName, fName, phone, sex, citizenNumber, address);
	        	 
	         }

			// ngắt kết nối
			JDBC_Util.closeConnection(con);
 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ketQua;
	}

	@Override
	public ArrayList<Customer> selectByCondition(String condition) {
		ArrayList<Customer> ketQua = new ArrayList<Customer>();
		try {
			// Tạo kết nối đến CSDL
			Connection con = JDBC_Util.getConnection();

			// thực thi câu lệnh SQL
			 String sql = "SELECT * FROM customer where " + condition;
	         PreparedStatement pstmt = con.prepareStatement(sql);
	         
	         ResultSet rs = pstmt.executeQuery(sql);

	         while (rs.next()) {
	        	 int id = rs.getInt("customer_ID");
	        	 String lName = rs.getString("lastName");
	        	 String fName = rs.getString("firstName");
	        	 String phone = rs.getString("phoneNumber");
	        	 int sex = rs.getInt("sex");
	        	 String citizenNumber = rs.getString("citizenNumber");
	        	 String address = rs.getString("address");
	        	 
	        	 Customer cus = new Customer(id, lName, fName, phone, sex, citizenNumber, address);
	        	 ketQua.add(cus);
	         }

			// ngắt kết nối
			JDBC_Util.closeConnection(con);
 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ketQua;
	}

}
