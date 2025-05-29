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
import utils.PaymentLogger;

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
        String sql = "INSERT INTO invoice (order_id, payment_date, subtotal, discount_percent, discount_amount, points_used, promotion_code, total, amount_paid, payment_method, status, staff_id, note, transaction_id, provider_transaction_id, payment_provider) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            pstmt.setString(14, invoice.getTransactionId());
            pstmt.setString(15, invoice.getProviderTransactionId());
            pstmt.setString(16, invoice.getPaymentProvider());

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
            PaymentLogger.error("Lỗi khi thêm hóa đơn: " + e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public int update(Invoice invoice) {
        String sql = "UPDATE invoice SET payment_date = ?, subtotal = ?, discount_percent = ?, discount_amount = ?, points_used = ?, promotion_code = ?, total = ?, amount_paid = ?, payment_method = ?, status = ?, staff_id = ?, note = ?, transaction_id = ?, provider_transaction_id = ?, payment_provider = ? WHERE invoice_id = ?";
        
        try (Connection con = DatabaseConnection.getConnection(); 
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setTimestamp(1, invoice.getPaymentDate());
            pstmt.setBigDecimal(2, invoice.getSubtotal());
            pstmt.setBigDecimal(3, invoice.getDiscountPercent());
            pstmt.setBigDecimal(4, invoice.getDiscountAmount());
            pstmt.setObject(5, invoice.getPointsUsed(), java.sql.Types.INTEGER);
            pstmt.setString(6, invoice.getPromotionCode());
            pstmt.setBigDecimal(7, invoice.getTotal());
            pstmt.setBigDecimal(8, invoice.getAmountPaid());
            pstmt.setString(9, invoice.getPaymentMethod() != null ? invoice.getPaymentMethod().name() : null);
            pstmt.setString(10, invoice.getStatus().name());
            pstmt.setInt(11, invoice.getStaff() != null ? invoice.getStaff().getId() : 0);
            pstmt.setString(12, invoice.getNote());
            pstmt.setString(13, invoice.getTransactionId());
            pstmt.setString(14, invoice.getProviderTransactionId());
            pstmt.setString(15, invoice.getPaymentProvider());
            pstmt.setInt(16, invoice.getInvoiceId());

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            PaymentLogger.error("Lỗi khi cập nhật hóa đơn: " + e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public int delete(Invoice invoice) {
        String sql = "DELETE FROM invoice WHERE invoice_id = ?";
        try (Connection con = DatabaseConnection.getConnection(); 
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, invoice.getInvoiceId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            PaymentLogger.error("Lỗi khi xóa hóa đơn: " + e.getMessage(), e);
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
            PaymentLogger.error("Lỗi khi lấy danh sách hóa đơn: " + e.getMessage(), e);
        }
        return list;
    }

    public Invoice selectById(int invoiceId) {
        String sql = "SELECT * FROM invoice WHERE invoice_id = ?";

        try (Connection con = DatabaseConnection.getConnection(); 
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, invoiceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInvoice(rs);
                }
            }
        } catch (SQLException e) {
            PaymentLogger.error("Lỗi khi tìm hóa đơn theo ID: " + e.getMessage(), e);
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

        try (Connection con = DatabaseConnection.getConnection(); 
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToInvoice(rs));
                }
            }
        } catch (SQLException e) {
            PaymentLogger.error("Lỗi khi truy vấn hóa đơn theo điều kiện: " + e.getMessage(), e);
        }
        return list;
    }
    
    /**
     * Lấy danh sách hóa đơn theo mã giao dịch PayOS
     */
    public List<Invoice> getInvoicesByProviderTransaction(String providerTransactionId) {
        return selectByCondition("provider_transaction_id = ?", providerTransactionId);
    }
    
    /**
     * Lấy danh sách hóa đơn QR đang chờ xử lý
     */
    public List<Invoice> getPendingQRInvoices() {
        return selectByCondition("payment_method = 'QR' AND status = 'PENDING' ORDER BY payment_date DESC");
    }
    
    /**
     * Cập nhật trạng thái thanh toán QR
     */
    public boolean updateQRPaymentStatus(String transactionId, StatusEnum status, String providerTransactionId, String note) {
        String sql = "UPDATE invoice SET status = ?, provider_transaction_id = ?, note = ?, " +
                     "payment_date = NOW(), amount_paid = CASE WHEN ? = 'COMPLETED' THEN total ELSE amount_paid END " +
                     "WHERE transaction_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            stmt.setString(2, providerTransactionId);
            stmt.setString(3, note);
            stmt.setString(4, status.name());
            stmt.setString(5, transactionId);
            
            int result = stmt.executeUpdate();
            
            // Nếu cập nhật thành công và trạng thái là COMPLETED, cập nhật order thành COMPLETED
            if (result > 0 && status == StatusEnum.COMPLETED) {
                updateOrderStatusByInvoiceTransaction(transactionId, StatusEnum.COMPLETED);
            }
            
            return result > 0;
        } catch (SQLException e) {
            PaymentLogger.error("Lỗi cập nhật trạng thái thanh toán QR: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Cập nhật thông tin khuyến mãi cho hóa đơn
     */
    public boolean updateInvoiceDiscount(int invoiceId, String promotionCode, double discountPercent, double discountAmount, double newTotal) {
        String sql = "UPDATE invoice SET promotion_code = ?, discount_percent = ?, discount_amount = ?, total = ? WHERE invoice_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, promotionCode);
            stmt.setDouble(2, discountPercent);
            stmt.setDouble(3, discountAmount);
            stmt.setDouble(4, newTotal);
            stmt.setInt(5, invoiceId);

            int result = stmt.executeUpdate();
            if (result > 0) {
                PaymentLogger.info("Cập nhật thông tin khuyến mãi thành công cho hóa đơn #" + invoiceId);
                return true;
            } else {
                PaymentLogger.error("Không thể cập nhật thông tin khuyến mãi cho hóa đơn #" + invoiceId);
                return false;
            }
        } catch (Exception e) {
            PaymentLogger.error("Lỗi khi cập nhật thông tin khuyến mãi cho hóa đơn #" + invoiceId + ": " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Cập nhật thông tin sử dụng điểm cho hóa đơn
     */
    public boolean updateInvoicePoints(int invoiceId, int pointsUsed, double newTotal) {
        String sql = "UPDATE invoice SET points_used = ?, total = ? WHERE invoice_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pointsUsed);
            stmt.setDouble(2, newTotal);
            stmt.setInt(3, invoiceId);

            int result = stmt.executeUpdate();
            if (result > 0) {
                PaymentLogger.info("Cập nhật thông tin điểm thành công cho hóa đơn #" + invoiceId);
                return true;
            } else {
                PaymentLogger.error("Không thể cập nhật thông tin điểm cho hóa đơn #" + invoiceId);
                return false;
            }
        } catch (Exception e) {
            PaymentLogger.error("Lỗi khi cập nhật thông tin điểm cho hóa đơn #" + invoiceId + ": " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Cập nhật trạng thái đơn hàng theo transaction ID của hóa đơn
     */
    private boolean updateOrderStatusByInvoiceTransaction(String transactionId, StatusEnum status) {
        String sql = "UPDATE `order` o JOIN invoice i ON o.order_id = i.order_id " +
                     "SET o.status = ? WHERE i.transaction_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            stmt.setString(2, transactionId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            PaymentLogger.error("Lỗi cập nhật trạng thái đơn hàng: " + e.getMessage(), e);
            return false;
        }
    }

    private Invoice mapResultSetToInvoice(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));

        Staff staff = new Staff();
        staff.setId(rs.getInt("staff_id"));

        PaymentMethodEnum paymentMethod = null;
        try {
            String paymentMethodStr = rs.getString("payment_method");
            paymentMethod = paymentMethodStr != null ? PaymentMethodEnum.valueOf(paymentMethodStr) : null;
        } catch (IllegalArgumentException e) {
            PaymentLogger.error("Lỗi khi truy cập payment_method: " + e.getMessage());
        }

        StatusEnum status = null;
        try {
            String statusStr = rs.getString("status");
            status = statusStr != null ? StatusEnum.valueOf(statusStr) : null;
        } catch (IllegalArgumentException e) {
            PaymentLogger.error("Lỗi khi truy cập status: " + e.getMessage());
        }

        Integer pointsUsed = null;
        try {
            pointsUsed = rs.getInt("points_used");
            if (rs.wasNull()) {
                pointsUsed = null;
            }
        } catch (SQLException e) {
            PaymentLogger.error("Lỗi khi truy cập points_used: " + e.getMessage());
        }

        Invoice invoice = new Invoice(
            rs.getInt("invoice_id"),
            order,
            rs.getTimestamp("payment_date"),
            rs.getBigDecimal("subtotal"),
            rs.getBigDecimal("discount_percent"),
            rs.getBigDecimal("discount_amount"),
            pointsUsed,
            rs.getString("promotion_code"),
            rs.getBigDecimal("total"),
            rs.getBigDecimal("amount_paid"),
            paymentMethod,
            status,
            staff,
            rs.getString("note"),
            rs.getString("transaction_id"),
            rs.getString("provider_transaction_id"),
            rs.getString("payment_provider")
        );
        
        return invoice;
    }
}