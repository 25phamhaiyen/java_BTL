package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import datatbase.JDBC_Util;
import entity.Pet;

public class PetDAO implements DAOInterface<Pet>{

	public static PetDAO getInstance() {
		return new PetDAO();
	}
	
	@Override
	public int insert (Pet t) {
		int ketQua = 0; 
		try {
			// Tạo kết nối đến CSDL
			Connection con = JDBC_Util.getConnection();

			// tạo ra đối tượng statement
			Statement st = con.createStatement();

			// thực thi câu lệnh SQL
			String sql = "INSERT INTO pet (PetID, PetName, age, Customer_ID, TypePetID) "
					+ "VALUES (?, ?, ?, ?, ?)";

			// Dùng PreparedStatement để truyền tham số vào câu SQL
			try (PreparedStatement pstmt = con.prepareStatement(sql)) {
				pstmt.setInt(1, t.getPetID());
				pstmt.setString(2, t.getPetName());
				pstmt.setInt(3, t.getAge());
				pstmt.setInt(4, t.getCustomerID());
				pstmt.setInt(5, t.getTypePetID());

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
	public int update(Pet t) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete(Pet t) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ArrayList<Pet> selectAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Pet selectById(Pet t) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Pet> selectByCondition(String condition) {
		// TODO Auto-generated method stub
		return null;
	}

}
