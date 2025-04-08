package repository;

import model.InvoicePromotion;
import model.Order;
import model.PaymentStatus;
import model.Invoice;
import model.Promotion;
import utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import enums.PaymentStatusEnum;

public class InvoicePromotionRepository implements IRepository<InvoicePromotion> {
    private static final Logger LOGGER = Logger.getLogger(InvoicePromotionRepository.class.getName());

    @Override
    public int insert(InvoicePromotion invoicePromotion) {
        String sql = "INSERT INTO invoice_promotion (invoiceID, promotionID, discountApplied) VALUES (?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, invoicePromotion.getInvoice().getInvoiceId());
            pstmt.setInt(2, invoicePromotion.getPromotion().getPromotionID());
            pstmt.setDouble(3, invoicePromotion.getDiscountApplied());

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.severe("Insert InvoicePromotion failed: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public int update(InvoicePromotion invoicePromotion) {
        String sql = "UPDATE invoice_promotion SET discountApplied = ? WHERE invoiceID = ? AND promotionID = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setDouble(1, invoicePromotion.getDiscountApplied());
            pstmt.setInt(2, invoicePromotion.getInvoice().getInvoiceId());
            pstmt.setInt(3, invoicePromotion.getPromotion().getPromotionID());

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.severe("Update InvoicePromotion failed: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public int delete(InvoicePromotion invoicePromotion) {
        String sql = "DELETE FROM invoice_promotion WHERE invoiceID = ? AND promotionID = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, invoicePromotion.getInvoice().getInvoiceId());
            pstmt.setInt(2, invoicePromotion.getPromotion().getPromotionID());

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.severe("Delete InvoicePromotion failed: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public List<InvoicePromotion> selectAll() {
        String sql = "SELECT ip.invoiceID, ip.promotionID, ip.discountApplied, " +
                     "i.CreatedAt AS invoiceCreatedAt, " +
                     "p.name AS promotionName, p.startDate AS promotionStartDate, p.endDate AS promotionEndDate " +
                     "FROM invoice_promotion ip " +
                     "JOIN invoice i ON ip.invoiceID = i.InvoiceID " +
                     "JOIN promotion p ON ip.promotionID = p.promotionID";

        List<InvoicePromotion> invoicePromotions = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int invoiceID = rs.getInt("invoiceID");
                int promotionID = rs.getInt("promotionID");
                double discountApplied = rs.getDouble("discountApplied");

                // Giả sử bạn có thể lấy được các giá trị cần thiết cho Promotion
                // Để tránh lỗi constructor thiếu tham số, giả sử bạn đã có các giá trị này.
                String promotionName = rs.getString("name");
                Date startDate = rs.getDate("startDate");
                Date endDate = rs.getDate("endDate");

                // Khởi tạo Promotion với đầy đủ tham số
                Promotion promotion = new Promotion(
                    promotionID, 
                    promotionName, 
                    "Default description",  // Dùng giá trị mặc định nếu không có sẵn
                    0,                      // Dùng giá trị mặc định nếu không có sẵn
                    0.0,                    // Dùng giá trị mặc định nếu không có sẵn
                    startDate, 
                    endDate
                );

                // Mapping to Invoice object
                Invoice invoice = new Invoice(invoiceID); 

                invoicePromotions.add(new InvoicePromotion(invoice, promotion, discountApplied));
            }
        } catch (SQLException e) {
            LOGGER.severe("Select all InvoicePromotions failed: " + e.getMessage());
        }
        return invoicePromotions;
    }

    @Override
    public InvoicePromotion selectById(InvoicePromotion invoicePromotion) {
        String sql = "SELECT ip.invoiceID, ip.promotionID, ip.discountApplied, " +
                     "i.CreatedAt AS invoiceCreatedAt, " +
                     "i.totalAmount, i.orderID, " +
                     "p.name AS promotionName, p.startDate AS promotionStartDate, p.endDate AS promotionEndDate, " +
                     "ps.PaymentStatusID, ps.StatusName AS paymentstatus " + // Lấy thêm paymentStatusID
                     "FROM invoice_promotion ip " +
                     "JOIN invoice i ON ip.invoiceID = i.InvoiceID " +
                     "JOIN promotion p ON ip.promotionID = p.promotionID " +
                     "JOIN paymentstatus ps ON i.PaymentStatusID = ps.PaymentStatusID " +
                     "WHERE ip.invoiceID = ? AND ip.promotionID = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, invoicePromotion.getInvoice().getInvoiceId());
            pstmt.setInt(2, invoicePromotion.getPromotion().getPromotionID());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int invoiceID = rs.getInt("invoiceID");
                    int promotionID = rs.getInt("promotionID");
                    double discountApplied = rs.getDouble("discountApplied");

                    // Lấy thông tin từ các bảng khác để tạo Invoice
                    BigDecimal totalAmount = rs.getBigDecimal("totalAmount");
                    int orderID = rs.getInt("orderID");
                    Timestamp createdAt = rs.getTimestamp("CreatedAt");

                    // Lấy paymentStatusID và paymentStatus từ kết quả truy vấn
                    int paymentStatusID = rs.getInt("paymentStatusID");
                    String paymentStatusStr = rs.getString("StatusName");

                    // Tạo đối tượng Order và PaymentStatus từ dữ liệu lấy được
                    Order order = new Order(orderID); 
                    PaymentStatusEnum paymentStatusEnum = PaymentStatusEnum.valueOf(paymentStatusStr.toUpperCase());
                    PaymentStatus paymentStatus = new PaymentStatus(paymentStatusID, paymentStatusEnum);

                    // Tạo đối tượng Invoice với đầy đủ các tham số
                    Invoice invoice = new Invoice(invoiceID, order, totalAmount, createdAt, paymentStatus);

                    Promotion promotion = new Promotion(
                            promotionID,
                            rs.getString("promotionName"),
                            "Default description",  // Giá trị mặc định
                            0,                      // Giá trị mặc định
                            0.0,                    // Giá trị mặc định
                            rs.getDate("startDate"),
                            rs.getDate("endDate")
                        );
                    return new InvoicePromotion(invoice, promotion, discountApplied);
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Select InvoicePromotion by ID failed: " + e.getMessage());
        }
        return null;
    }


    @Override
    public List<InvoicePromotion> selectByCondition(String condition, Object... params) {
        String sql = "SELECT i.InvoiceID, p.PromotionID, ip.discountApplied, " +
                     "p.name AS promotionName, p.startDate AS promotionStartDate, p.endDate AS promotionEndDate " +
                     "FROM invoice_promotion ip " +
                     "JOIN invoice i ON ip.InvoiceID = i.InvoiceID " +
                     "JOIN promotion p ON ip.PromotionID = p.PromotionID";

        if (condition != null && !condition.trim().isEmpty()) {
            sql += " WHERE " + condition;
        }

        List<InvoicePromotion> invoicePromotions = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            // Set parameters for the query
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Create Invoice object from InvoiceID
                    Invoice invoice = new Invoice(rs.getInt("InvoiceID"));

                    // Create Promotion object from PromotionID, Name, Start Date, End Date
                    int promotionID = rs.getInt("PromotionID");
                    String promotionName = rs.getString("promotionName");
                    Date startDate = rs.getDate("promotionStartDate");
                    Date endDate = rs.getDate("promotionEndDate");

                    // Instantiate the Promotion object fully
                    Promotion promotion = new Promotion(
                        promotionID,
                        promotionName,
                        "Default description",  // Default value if not available
                        0,                      // Default value if not available
                        0.0,                    // Default value if not available
                        startDate, 
                        endDate
                    );

                    // Get discountApplied
                    double discountApplied = rs.getDouble("discountApplied");

                    // Create InvoicePromotion object
                    InvoicePromotion invoicePromotion = new InvoicePromotion(invoice, promotion, discountApplied);
                    invoicePromotions.add(invoicePromotion);
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error querying invoice promotions: " + e.getMessage());
        }

        return invoicePromotions;
    }


}
