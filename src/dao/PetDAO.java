package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Enum.GenderEnum;
import entity.Customer;
import entity.Pet;
import entity.TypePet;
import utils.DBUtil;
import utils.DatabaseConnection;

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
            pstmt.setInt(4, t.getCustomer().getCustomerID());
            pstmt.setInt(5, t.getTypePet().getTypePetID());

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
            pstmt.setInt(3, t.getCustomer().getCustomerID());
            pstmt.setInt(4, t.getTypePet().getTypePetID());
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
    public List<Pet> selectAll() {
        List<Pet> list = new ArrayList<>();
        String sql = "SELECT * FROM pet";

        Connection con = null;
        PreparedStatement pstmt = null;
        java.sql.ResultSet rs = null;

        try {
            con = DatabaseConnection.getConnection();
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                // Lấy customer từ database dựa vào customerID
                CustomerDAO customerDAO = new CustomerDAO();
                Customer customer = customerDAO.selectById(rs.getInt("Customer_ID"));

                // Lấy typePet từ database dựa vào typePetID
                TypePetDAO typePetDAO = new TypePetDAO();
                TypePet typePet = typePetDAO.selectById(rs.getInt("TypePetID"));

                // Tạo đối tượng Pet với đúng constructor
                list.add(new Pet(
                    rs.getInt("PetID"),
                    rs.getString("PetName"),
                    rs.getInt("age"),
                    customer, // Truyền Customer thay vì int
                    typePet   // Truyền TypePet thay vì int
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
            
         // Lấy customer từ database dựa vào customerID
            CustomerDAO customerDAO = new CustomerDAO();
            Customer customer = customerDAO.selectById(rs.getInt("Customer_ID"));

            // Lấy typePet từ database dựa vào typePetID
            TypePetDAO typePetDAO = new TypePetDAO();
            TypePet typePet = typePetDAO.selectById(rs.getInt("TypePetID"));

            // Tạo đối tượng Pet với đúng constructor
            if (rs.next()) {
                pet = new Pet(
                        rs.getInt("PetID"),
                        rs.getString("PetName"),
                        rs.getInt("age"),
                        customer, // Truyền Customer thay vì int
                        typePet   // Truyền TypePet thay vì int
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
    public List<Pet> selectByCondition(String condition, Object... params) {
        List<Pet> list = new ArrayList<>();
        
        // Sử dụng JOIN để lấy thông tin Customer và TypePet trực tiếp
        String sql = "SELECT p.*, c.*, t.* " +
                     "FROM pet p " +
                     "JOIN customer c ON p.Customer_ID = c.customer_ID " +
                     "JOIN typepet t ON p.TypePetID = t.TypePetID " +
                     "WHERE " + condition;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            // Truyền tham số vào PreparedStatement
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Lấy thông tin Customer
                    Customer customer = new Customer(
                        rs.getInt("customer_ID"),
                        rs.getString("lastName"),
                        rs.getString("firstName"),
                        rs.getString("phoneNumber"),
                        GenderEnum.fromCode(rs.getInt("sex")), // Chuyển từ int -> Enum
                        rs.getString("citizenNumber"),
                        rs.getString("address"),
                        null // Bỏ qua Account để bảo mật
                    );

                    // Lấy thông tin TypePet
                    TypePet typePet = new TypePet(
                        rs.getInt("TypePetID"),
                        rs.getString("TypePetName")
                    );

                    // Tạo đối tượng Pet
                    list.add(new Pet(
                        rs.getInt("PetID"),
                        rs.getString("PetName"),
                        rs.getInt("age"),
                        customer,
                        typePet
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm thú cưng: " + e.getMessage());
        }
        
        return list;
    }

}
