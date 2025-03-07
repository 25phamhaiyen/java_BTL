package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
			String sql = "INSERT INTO customer (Customer_ID, lastName, firstName, phoneNumber, Sex, CitizenNumber, Address) "
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

			// tạo ra đối tượng statement
			Statement st = con.createStatement();

			// thực thi câu lệnh SQL
			String sql = "UPDATE customer SET lastName = ?, firstName = ?, phoneNumber = ?, Sex = ?, CitizenNumber = ?, Address = ? WHERE Customer_ID = ?";


			// Dùng PreparedStatement để truyền tham số vào câu SQL
			try (PreparedStatement pstmt = con.prepareStatement(sql)) {
				pstmt.setString(1, t.getLastName());
				pstmt.setString(2, t.getFirstName());
				pstmt.setString(3, t.getPhoneNumber());
				pstmt.setInt(4, t.getSex());
				pstmt.setString(5, t.getCitizenNumber());
				pstmt.setString(6, t.getAddress());
				pstmt.setInt(7, t.getCustomer_ID()); // Đặt ID cuối cùng


				// Thực thi lệnh INSERT
				 ketQua = pstmt.executeUpdate();
			}

			// In thông tin số dòng bị thay đổi
			System.out.println("Bạn đã thực thi UPDATE, có " + ketQua + " dòng bị thay đổi.");

			// ngắt kết nối
			JDBC_Util.closeConnection(con);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
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

			// tạo ra đối tượng statement
			Statement st = con.createStatement();

			// thực thi câu lệnh SQL
			 String sql = "DELETE FROM Customer WHERE Customer_ID = ?";
	         PreparedStatement pstmt = con.prepareStatement(sql);
	         pstmt.setInt(1, t.getCustomer_ID());

			// Thực thi lệnh INSERT
			 ketQua = pstmt.executeUpdate();
			// In thông tin số dòng bị thay đổi
			System.out.println("Bạn đã thực thi DELETE, có " + ketQua + " dòng bị thay đổi.");

			// ngắt kết nối
			JDBC_Util.closeConnection(con);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ketQua;
	}

	@Override
	public ArrayList<Customer> selectAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Customer selectById(Customer t) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Customer> selectByCondition(String condition) {
		// TODO Auto-generated method stub
		return null;
	}

}
