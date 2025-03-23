package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseConnection;
import entity.TypePet;

public class TypePetDAO implements DAOInterface<TypePet> {

    public static TypePetDAO getInstance() {
        return new TypePetDAO();
    }

    @Override
    public int insert(TypePet t) {
        int result = 0;
        String sql = "INSERT INTO typepet (UN_TypeName) VALUES (?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, t.getTypeName());
            result = pstmt.executeUpdate();

            // Lấy ID tự động tăng
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                t.setTypePetID(rs.getInt(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int update(TypePet t) {
        int result = 0;
        String sql = "UPDATE typepet SET UN_TypeName = ? WHERE TypePetID = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, t.getTypeName());
            pstmt.setInt(2, t.getTypePetID());

            result = pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int delete(TypePet t) {
        int result = 0;
        String sql = "DELETE FROM typepet WHERE TypePetID = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, t.getTypePetID());
            result = pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<TypePet> selectAll() {
        List<TypePet> list = new ArrayList<>();
        String sql = "SELECT * FROM typepet";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new TypePet(rs.getInt("TypePetID"), rs.getString("UN_TypeName")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public TypePet selectById(int typePetID) {
        TypePet typePet = null;
        String sql = "SELECT * FROM typepet WHERE TypePetID = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, typePetID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                typePet = new TypePet(rs.getInt("TypePetID"), rs.getString("UN_TypeName"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return typePet;
    }
    
    @Override
    public TypePet selectById(TypePet t) {
    	return selectById(t.getTypePetID()); // Gọi lại phương thức nhận int
    }

    @Override
    public List<TypePet> selectByCondition(String condition, Object... params) {
        List<TypePet> list = new ArrayList<>();
        
        // Tránh nối chuỗi trực tiếp, sử dụng tham số hóa
        String sql = "SELECT * FROM TypePet WHERE " + condition;
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            // Truyền tham số vào câu lệnh SQL
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new TypePet(
                        rs.getInt("TypePetID"),
                        rs.getString("TypePetName")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn TypePet: " + e.getMessage());
        }
        
        return list;
    }

}

