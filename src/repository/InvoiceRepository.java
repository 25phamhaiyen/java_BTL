package repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import enums.PaymentMethodEnum;
import enums.StatusEnum;
import model.Invoice;
import model.Order;
import model.Staff;
import utils.DatabaseConnection;

public class InvoiceRepository implements IRepository<Invoice> {

	private static InvoiceRepository instance;

	public static InvoiceRepository getInstance() {
		if (instance == null) {
			instance = new InvoiceRepository();
		}
		return instance;
	}

	@Override
	public int insert(Invoice invoice) {
		String sql = "INSERT INTO invoice (order_id, payment_date, subtotal, discount_percent, discount_amount, points_used, promotion_code, total, amount_paid, payment_method, status, staff_id, note) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection con = DatabaseConnection.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			pstmt.setInt(1, invoice.getOrder().getOrderId());
			pstmt.setTimestamp(2, invoice.getPaymentDate());
			pstmt.setBigDecimal(3, invoice.getSubtotal());
			pstmt.setBigDecimal(4, invoice.getDiscountPercent());
			pstmt.setBigDecimal(5, invoice.getDiscountAmount());
			pstmt.setObject(6, invoice.getPointsUsed(), java.sql.Types.INTEGER);
			pstmt.setString(7, invoice.getPromotionCode());
			pstmt.setBigDecimal(8, invoice.getTotal());
			pstmt.setBigDecimal(9, invoice.getAmountPaid());
			pstmt.setString(10, invoice.getPaymentMethod() != null ? invoice.getPaymentMethod().name() : null);
			pstmt.setString(11, invoice.getStatus().name());
			pstmt.setInt(12, invoice.getStaff() != null ? invoice.getStaff().getId() : 0);
			pstmt.setString(13, invoice.getNote());

			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				try (ResultSet rs = pstmt.getGeneratedKeys()) {
					if (rs.next()) {
						invoice.setInvoiceId(rs.getInt(1));
					}
				}
			}
			return affectedRows;
		} catch (SQLException e) {
			System.err.println("Lỗi khi thêm hóa đơn: " + e.getMessage());
			return 0;
		}
	}

	@Override
	public int update(Invoice invoice) {
		String sql = "UPDATE invoice SET order_id = ?, payment_date = ?, subtotal = ?, discount_percent = ?, discount_amount = ?, points_used = ?, promotion_code = ?, total = ?, amount_paid = ?, payment_method = ?, status = ?, staff_id = ?, note = ? WHERE invoice_id = ?";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setInt(1, invoice.getOrder().getOrderId());
			pstmt.setTimestamp(2, invoice.getPaymentDate());
			pstmt.setBigDecimal(3, invoice.getSubtotal());
			pstmt.setBigDecimal(4, invoice.getDiscountPercent());
			pstmt.setBigDecimal(5, invoice.getDiscountAmount());
			pstmt.setObject(6, invoice.getPointsUsed(), java.sql.Types.INTEGER);
			pstmt.setString(7, invoice.getPromotionCode());
			pstmt.setBigDecimal(8, invoice.getTotal());
			pstmt.setBigDecimal(9, invoice.getAmountPaid());
			pstmt.setString(10, invoice.getPaymentMethod() != null ? invoice.getPaymentMethod().name() : null);
			pstmt.setString(11, invoice.getStatus().name());
			pstmt.setInt(12, invoice.getStaff() != null ? invoice.getStaff().getId() : 0);
			pstmt.setString(13, invoice.getNote());
			pstmt.setInt(14, invoice.getInvoiceId());

			return pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Lỗi khi cập nhật hóa đơn: " + e.getMessage());
			return 0;
		}
	}

	@Override
	public int delete(Invoice invoice) {
		String sql = "DELETE FROM invoice WHERE invoice_id = ?";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setInt(1, invoice.getInvoiceId());
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Lỗi khi xóa hóa đơn: " + e.getMessage());
			return 0;
		}
	}

	@Override
	public List<Invoice> selectAll() {
		List<Invoice> list = new ArrayList<>();
		String sql = "SELECT * FROM invoice";

		try (Connection con = DatabaseConnection.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				list.add(mapResultSetToInvoice(rs));
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi lấy danh sách hóa đơn: " + e.getMessage());
		}
		return list;
	}

	public Invoice selectById(int invoiceId) {
		String sql = "SELECT * FROM invoice WHERE invoice_id = ?";

		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setInt(1, invoiceId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToInvoice(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi tìm hóa đơn theo ID: " + e.getMessage());
		}
		return null;
	}

	@Override
	public Invoice selectById(Invoice invoice) {
		return selectById(invoice.getInvoiceId());
	}

	@Override
	public List<Invoice> selectByCondition(String whereClause, Object... params) {
		List<Invoice> list = new ArrayList<>();
		String sql = "SELECT * FROM invoice WHERE " + whereClause;

		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			for (int i = 0; i < params.length; i++) {
				pstmt.setObject(i + 1, params[i]);
			}

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					list.add(mapResultSetToInvoice(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi truy vấn hóa đơn theo điều kiện: " + e.getMessage());
			e.printStackTrace(); // Ghi lại chi tiết lỗi
		}
		return list;
	}

	/**
	 * Cập nhật thông tin khuyến mãi cho hóa đơn
	 */
	public void updateInvoiceDiscount(int invoiceId, String promotionCode, double discountPercent,
			double discountAmount, double newTotal) throws SQLException {
		String sql = "UPDATE invoice SET promotion_code = ?, discount_percent = ?, "
				+ "discount_amount = ?, total = ? WHERE invoice_id = ?";

		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, promotionCode);
			stmt.setDouble(2, discountPercent);
			stmt.setDouble(3, discountAmount);
			stmt.setDouble(4, newTotal);
			stmt.setInt(5, invoiceId);

			int result = stmt.executeUpdate();
			if (result != 1) {
				throw new SQLException("Không thể cập nhật thông tin khuyến mãi cho hóa đơn #" + invoiceId);
			}
		}
	}

	/**
	 * Cập nhật thông tin sử dụng điểm cho hóa đơn
	 */
	public void updateInvoicePoints(int invoiceId, int pointsUsed, double newTotal) throws SQLException {
		String sql = "UPDATE invoice SET points_used = ?, total = ? WHERE invoice_id = ?";

		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, pointsUsed);
			stmt.setDouble(2, newTotal);
			stmt.setInt(3, invoiceId);

			int result = stmt.executeUpdate();
			if (result != 1) {
				throw new SQLException("Không thể cập nhật thông tin điểm cho hóa đơn #" + invoiceId);
			}
		}
	}

	private Invoice mapResultSetToInvoice(ResultSet rs) throws SQLException {
		Order order = new Order();
		order.setOrderId(rs.getInt("order_id"));

		Staff staff = new Staff();
		staff.setId(rs.getInt("staff_id"));

		String paymentMethodStr = rs.getString("payment_method");
		PaymentMethodEnum paymentMethod = paymentMethodStr != null ? PaymentMethodEnum.valueOf(paymentMethodStr) : null;

		String statusStr = rs.getString("status");
		StatusEnum status = statusStr != null ? StatusEnum.valueOf(statusStr) : null;

		Integer pointsUsed;
		try {
			pointsUsed = rs.getInt("points_used");
			if (rs.wasNull()) {
				pointsUsed = null; // Đảm bảo pointsUsed là null nếu giá trị trong DB là null
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi truy cập cột points_used: " + e.getMessage());
			e.printStackTrace(); // Ghi lại chi tiết lỗi
			pointsUsed = null; // Gán giá trị mặc định nếu có lỗi
		}

		return new Invoice(rs.getInt("invoice_id"), order, rs.getTimestamp("payment_date"),
				rs.getBigDecimal("subtotal"), rs.getBigDecimal("discount_percent"), rs.getBigDecimal("discount_amount"),
				pointsUsed, rs.getString("promotion_code"), rs.getBigDecimal("total"), rs.getBigDecimal("amount_paid"),
				paymentMethod, status, staff, rs.getString("note"));
	}
}