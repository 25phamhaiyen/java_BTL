package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import enums.TypeServiceEnum;
import model.TypeService;
import utils.DatabaseConnection;

public class TypeServiceRepository implements IRepository<TypeService> {

	public static TypeServiceRepository getInstance() {
		return new TypeServiceRepository();
	}

	@Override
	public int insert(TypeService typeService) {
		String sql = "INSERT INTO typeservice (UN_TypeName) VALUES (?)";
		try (Connection con = DatabaseConnection.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			pstmt.setString(1, typeService.getTypeServiceName().getDescription());

			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				try (ResultSet rs = pstmt.getGeneratedKeys()) {
					if (rs.next()) {
						typeService.setTypeServiceID(rs.getInt(1));
					}
				}
			}
			return affectedRows;
		} catch (SQLException e) {
			System.err.println("Lỗi khi thêm loại dịch vụ: " + e.getMessage());
			return 0;
		}
	}

	@Override
	public int update(TypeService typeService) {
		String sql = "UPDATE typeservice SET UN_TypeName=? WHERE TypeServiceID=?";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setString(1, typeService.getTypeServiceName().getDescription());
			pstmt.setInt(2, typeService.getTypeServiceID());

			return pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Lỗi khi cập nhật loại dịch vụ: " + e.getMessage());
			return 0;
		}
	}

	@Override
	public int delete(TypeService typeService) {
		String sql = "DELETE FROM typeservice WHERE TypeServiceID=?";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setInt(1, typeService.getTypeServiceID());
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Lỗi khi xóa loại dịch vụ: " + e.getMessage());
			return 0;
		}
	}

	@Override
	public List<TypeService> selectAll() {
		List<TypeService> list = new ArrayList<>();
		String sql = "SELECT * FROM typeservice";
		try (Connection con = DatabaseConnection.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				list.add(mapResultSetToTypeService(rs));
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi lấy danh sách loại dịch vụ: " + e.getMessage());
		}
		return list;
	}

	@Override
	public TypeService selectById(TypeService typeService) {
		return selectById(typeService.getTypeServiceID());
	}

	public TypeService selectById(int typeServiceID) {
		String sql = "SELECT * FROM typeservice WHERE TypeServiceID = ?";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setInt(1, typeServiceID);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToTypeService(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi tìm loại dịch vụ theo ID: " + e.getMessage());
		}
		return null;
	}

	@Override
	public List<TypeService> selectByCondition(String condition, Object... params) {
		List<TypeService> list = new ArrayList<>();
		String sql = "SELECT * FROM typeservice WHERE " + condition;
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			for (int i = 0; i < params.length; i++) {
				pstmt.setObject(i + 1, params[i]);
			}

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					list.add(mapResultSetToTypeService(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi tìm loại dịch vụ theo điều kiện: " + e.getMessage());
		}
		return list;
	}

	private TypeService mapResultSetToTypeService(ResultSet rs) throws SQLException {
		int typeServiceID = rs.getInt("TypeServiceID");
		String typeName = rs.getString("UN_TypeName");

		// Đổi từ `valueOf()` sang `fromDescription()`
		TypeServiceEnum typeServiceEnum = TypeServiceEnum.fromDescription(typeName);

		return new TypeService(typeServiceID, typeServiceEnum);
	}

}