package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import datatbase.DatabaseConnection;
import entity.Pet;
import utils.DBUtil;

public class PetDAO implements DAOInterface<Pet> {

    public static PetDAO getInstance() {
        return new PetDAO();
    }

    @Override
    public int insert(Pet t) {
        int ketQua = 0;
        String sql = "INSERT INTO pet (PetID, PetName, age, Customer_ID, TypePetID) VALUES (?, ?, ?, ?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = DatabaseConnection.getConnection();
            pstmt = con.prepareStatement(sql);
            
            pstmt.setInt(1, t.getPetID());
            pstmt.setString(2, t.getPetName());
            pstmt.setInt(3, t.getAge());
            pstmt.setInt(4, t.getCustomerID());
            pstmt.setInt(5, t.getTypePetID());

            ketQua = pstmt.executeUpdate();
            System.out.println("INSERT thành công, " + ketQua + " dòng bị thay đổi.");

        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm thú cưng: " + e.getMessage());
        } finally {
            DBUtil.closeResources(con, pstmt);
        }
        return ketQua;
    }

    @Override
    public int update(Pet t) {
        int ketQua = 0;
        String sql = "UPDATE pet SET PetName = ?, age = ?, Customer_ID = ?, TypePetID = ? WHERE PetID = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = DatabaseConnection.getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, t.getPetName());
            pstmt.setInt(2, t.getAge());
            pstmt.setInt(3, t.getCustomerID());
            pstmt.setInt(4, t.getTypePetID());
            pstmt.setInt(5, t.getPetID());

            ketQua = pstmt.executeUpdate();
            System.out.println("UPDATE thành công, " + ketQua + " dòng bị thay đổi.");

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật thú cưng: " + e.getMessage());
        } finally {
            DBUtil.closeResources(con, pstmt);
        }
        return ketQua;
    }

    @Override
    public int delete(Pet t) {
        int ketQua = 0;
        String sql = "DELETE FROM pet WHERE PetID = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = DatabaseConnection.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, t.getPetID());

            ketQua = pstmt.executeUpdate();
            System.out.println("DELETE thành công, " + ketQua + " dòng bị thay đổi.");

        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa thú cưng: " + e.getMessage());
        } finally {
            DBUtil.closeResources(con, pstmt);
        }
        return ketQua;
    }

    @Override
    public ArrayList<Pet> selectAll() {
        ArrayList<Pet> list = new ArrayList<>();
        String sql = "SELECT * FROM pet";

        Connection con = null;
        PreparedStatement pstmt = null;
        java.sql.ResultSet rs = null;

        try {
            con = DatabaseConnection.getConnection();
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(new Pet(
                        rs.getInt("PetID"),
                        rs.getString("PetName"),
                        rs.getInt("age"),
                        rs.getInt("Customer_ID"),
                        rs.getInt("TypePetID")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách thú cưng: " + e.getMessage());
        } finally {
            DBUtil.closeResources(con, pstmt, rs);
        }
        return list;
    }

    @Override
    public Pet selectById(Pet t) {
        Pet pet = null;
        String sql = "SELECT * FROM pet WHERE PetID = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        java.sql.ResultSet rs = null;

        try {
            con = DatabaseConnection.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, t.getPetID());

            rs = pstmt.executeQuery();
            if (rs.next()) {
                pet = new Pet(
                        rs.getInt("PetID"),
                        rs.getString("PetName"),
                        rs.getInt("age"),
                        rs.getInt("Customer_ID"),
                        rs.getInt("TypePetID")
                );
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm thú cưng: " + e.getMessage());
        } finally {
            DBUtil.closeResources(con, pstmt, rs);
        }
        return pet;
    }

    @Override
    public ArrayList<Pet> selectByCondition(String condition) {
        ArrayList<Pet> list = new ArrayList<>();
        String sql = "SELECT * FROM pet WHERE " + condition;

        Connection con = null;
        PreparedStatement pstmt = null;
        java.sql.ResultSet rs = null;

        try {
            con = DatabaseConnection.getConnection();
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(new Pet(
                        rs.getInt("PetID"),
                        rs.getString("PetName"),
                        rs.getInt("age"),
                        rs.getInt("Customer_ID"),
                        rs.getInt("TypePetID")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm thú cưng: " + e.getMessage());
        } finally {
            DBUtil.closeResources(con, pstmt, rs);
        }
        return list;
    }
}
