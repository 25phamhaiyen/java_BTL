package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Enum.PaymentStatusEnum;
import entity.PaymentStatus;
import utils.DatabaseConnection;

public class PaymentStatusDAO implements DAOInterface<PaymentStatus> {

	public static PaymentStatusDAO getInstance() {
		return new PaymentStatusDAO();
	}

	@Override
	public int insert(PaymentStatus t) {
		String sql = "INSERT INTO PaymentStatus (un_statusCode, StatusName) VALUES (?, ?)";
		try (Connection con = DatabaseConnection.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			// Lưu mã trạng thái vào un_statusCode
			pstmt.setInt(1, t.getStatus().getCode());

			// Lưu tên trạng thái vào StatusName
			pstmt.setString(2, t.getStatus().getName()); // Lưu tên trạng thái

			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						t.setPaymentStatusID(generatedKeys.getInt(1));
					}
				}
			}
			return affectedRows;
		} catch (SQLException e) {
			System.err.println("Lỗi khi chèn PaymentStatus: " + e.getMessage());
		}
		return 0;
	}

	@Override
	public int update(PaymentStatus t) {
		String sql = "UPDATE PaymentStatus SET statusName = ? WHERE paymentStatusID = ?";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setInt(1, t.getStatus().getCode());
			pstmt.setInt(2, t.getPaymentStatusID());

			return pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Lỗi khi cập nhật PaymentStatus: " + e.getMessage());
		}
		return 0;
	}

	@Override
	public int delete(PaymentStatus t) {
		String sql = "DELETE FROM PaymentStatus WHERE paymentStatusID = ?";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setInt(1, t.getPaymentStatusID());
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Lỗi khi xóa PaymentStatus: " + e.getMessage());
		}
		return 0;
	}

	@Override
	public List<PaymentStatus> selectAll() {
		List<PaymentStatus> list = new ArrayList<>();
		String sql = "SELECT * FROM PaymentStatus";
		try (Connection con = DatabaseConnection.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				list.add(mapResultSetToPaymentStatus(rs));
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi lấy danh sách PaymentStatus: " + e.getMessage());
		}
		return list;
	}

	@Override
	public PaymentStatus selectById(PaymentStatus t) {
		return selectById(t.getPaymentStatusID());
	}

	public PaymentStatus selectById(int id) {
		String sql = "SELECT * FROM paymentStatus WHERE PaymentStatusID = ?";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setInt(1, id);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToPaymentStatus(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi tìm PaymentStatus theo ID: " + e.getMessage());
		}
		return null;
	}

	@Override
	public List<PaymentStatus> selectByCondition(String condition, Object... params) {
		List<PaymentStatus> list = new ArrayList<>();

		// Kiểm tra nếu condition rỗng, thay thế bằng truy vấn lấy tất cả
		String sql = "SELECT * FROM PaymentStatus";
		if (condition != null && !condition.trim().isEmpty()) {
			sql += " WHERE " + condition;
		}

		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			// Gán tham số vào câu SQL
			for (int i = 0; i < params.length; i++) {
				pstmt.setObject(i + 1, params[i]);
			}

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					list.add(mapResultSetToPaymentStatus(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi tìm PaymentStatus theo điều kiện: " + e.getMessage());
		}
		return list;
	}

	// Phương thức private để map ResultSet thành PaymentStatus
	private PaymentStatus mapResultSetToPaymentStatus(ResultSet rs) throws SQLException {
		int statusCode = rs.getInt("UN_StatusCode");

		// Kiểm tra xem statusCode có hợp lệ không
		PaymentStatusEnum statusEnum = PaymentStatusEnum.fromCode(statusCode);

		return new PaymentStatus(rs.getInt("paymentStatusID"), statusEnum);
	}

}
