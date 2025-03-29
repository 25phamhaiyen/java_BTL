package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import entity.Invoice;
import entity.Order;
import entity.PaymentStatus;
import database.DatabaseConnection;

public class InvoiceDAO implements DAOInterface<Invoice> {

    public static InvoiceDAO getInstance() {
        return new InvoiceDAO();
    }

    @Override
    public int insert(Invoice invoice) {
        String sql = "INSERT INTO invoice (OrderID, TotalAmount, CreatedAt, PaymentStatusID) VALUES (?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, invoice.getOrder().getOrderId());
            pstmt.setBigDecimal(2, invoice.getTotalAmount());
            pstmt.setTimestamp(3, invoice.getCreatedAt());
            pstmt.setInt(4, invoice.getPaymentStatus().getPaymentStatusID());

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
        String sql = "UPDATE invoice SET OrderID=?, TotalAmount=?, CreatedAt=?, PaymentStatusID=? WHERE InvoiceID=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, invoice.getOrder().getOrderId());
            pstmt.setBigDecimal(2, invoice.getTotalAmount());
            pstmt.setTimestamp(3, invoice.getCreatedAt());
            pstmt.setInt(4, invoice.getPaymentStatus().getPaymentStatusID());
            pstmt.setInt(5, invoice.getInvoiceId());

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật hóa đơn: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int delete(Invoice invoice) {
        String sql = "DELETE FROM invoice WHERE InvoiceID=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

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
        String sql = "SELECT InvoiceID, OrderID, TotalAmount, CreatedAt, PaymentStatusID FROM invoice";

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
        String sql = "SELECT InvoiceID, OrderID, TotalAmount, CreatedAt, PaymentStatusID FROM invoice WHERE InvoiceID = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

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
        String sql = "SELECT InvoiceID, OrderID, TotalAmount, CreatedAt, PaymentStatusID FROM invoice WHERE " + whereClause;

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
            System.err.println("Lỗi khi truy vấn hóa đơn theo điều kiện: " + e.getMessage());
        }
        return list;
    }

    private Invoice mapResultSetToInvoice(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("OrderID"));
        
        PaymentStatus paymentStatus = new PaymentStatus();
        paymentStatus.setPaymentStatusID(rs.getInt("PaymentStatusID"));
        
        return new Invoice(
                rs.getInt("InvoiceID"),
                order,
                rs.getBigDecimal("TotalAmount"),
                rs.getTimestamp("CreatedAt"),
                paymentStatus
                
        );
    }
}
