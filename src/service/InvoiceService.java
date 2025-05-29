package service;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import utils.DatabaseConnection;
import enums.PaymentMethodEnum;
import enums.StatusEnum;
import model.Invoice;
import model.Order;
import model.OrderDetail;
import model.Service;
import model.Staff;
import repository.BookingRepository;
import repository.InvoiceRepository;
import repository.OrderDetailRepository;
import repository.OrderRepository;
import utils.Session;
import utils.PaymentLogger;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import javax.imageio.ImageIO;
import java.io.File;

/**
 * Service xử lý hóa đơn, tích hợp với PayOS API cho thanh toán QR
 */
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final BookingRepository bookingRepository;
    private QRPaymentService qrPaymentService;
    
    // Task kiểm tra trạng thái thanh toán định kỳ
    private Timer paymentStatusTimer;
    
    private static InvoiceService instance;

    public InvoiceService() {
        this.invoiceRepository = InvoiceRepository.getInstance();
        this.orderRepository = OrderRepository.getInstance();
        this.orderDetailRepository = OrderDetailRepository.getInstance();
        this.bookingRepository = BookingRepository.getInstance();
        initQRPaymentService();
        startPaymentStatusChecker();
    }

    private void initQRPaymentService() {
        this.qrPaymentService = QRPaymentService.getInstance();
    }

    public QRPaymentService getQRPaymentService() {
        if (this.qrPaymentService == null) {
            initQRPaymentService();
        }
        return this.qrPaymentService;
    }
    
    private void startPaymentStatusChecker() {
        if (paymentStatusTimer != null) {
            paymentStatusTimer.cancel();
        }
        
        paymentStatusTimer = new Timer(true);
        paymentStatusTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkPendingQRPayments();
            }
        }, 10000, 30000); // Kiểm tra mỗi 30 giây
    }
    
    private void checkPendingQRPayments() {
        try {
            List<Invoice> pendingInvoices = invoiceRepository.getPendingQRInvoices();
            PaymentLogger.info("Found " + pendingInvoices.size() + " pending QR payments");
            
            for (Invoice invoice : pendingInvoices) {
                if (invoice.getProviderTransactionId() != null) {
                    PayOSService.PayOSPaymentStatus status = PayOSService.getInstance()
                            .checkPaymentStatus(invoice.getProviderTransactionId());
                    
                    if (status.isPaid()) {
                        // Cập nhật trạng thái thanh toán thành công
                        updateQRPaymentStatus(invoice.getTransactionId(), StatusEnum.COMPLETED, 
                                status.getTransactionId(), "Auto-updated by payment checker");
                        PaymentLogger.info("Payment completed: " + invoice.getTransactionId());
                    } else if (status.isCancelled()) {
                        // Cập nhật trạng thái hủy
                        updateQRPaymentStatus(invoice.getTransactionId(), StatusEnum.CANCELLED, 
                                null, "Payment cancelled");
                        PaymentLogger.info("Payment cancelled: " + invoice.getTransactionId());
                    }
                }
            }
        } catch (Exception e) {
            PaymentLogger.error("Error checking pending payments: " + e.getMessage(), e);
        }
    }

    public static InvoiceService getInstance() {
        if (instance == null) {
            synchronized (InvoiceService.class) {
                if (instance == null) {
                    instance = new InvoiceService();
                }
            }
        }
        return instance;
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.selectAll();
    }

    public Invoice getInvoiceById(int invoiceId) {
        return invoiceRepository.selectById(invoiceId);
    }

    public List<Invoice> getInvoicesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        String condition = "payment_date BETWEEN ? AND ?";
        return invoiceRepository.selectByCondition(condition, 
                Timestamp.valueOf(startDate),
                Timestamp.valueOf(endDate));
    }

    public List<Invoice> getRecentInvoices(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        String condition = "payment_date >= ?";
        return invoiceRepository.selectByCondition(condition, Timestamp.valueOf(startDate));
    }

    /**
     * Tạo hóa đơn mới
     */
    public boolean createInvoice(int orderId, PaymentMethodEnum paymentMethod) {
        Order order = orderRepository.selectById(orderId);
        if (order == null) {
            PaymentLogger.error("Đơn hàng không tồn tại: " + orderId);
            throw new IllegalArgumentException("Đơn hàng không tồn tại");
        }

        Staff staff = Session.getCurrentStaff();
        if (staff == null) {
            PaymentLogger.error("Không có thông tin nhân viên");
            throw new IllegalArgumentException("Không có thông tin nhân viên");
        }

        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setPaymentDate(new Timestamp(System.currentTimeMillis()));
        invoice.setSubtotal(BigDecimal.valueOf(order.getTotalAmount()));
        invoice.setDiscountPercent(BigDecimal.ZERO);
        invoice.setDiscountAmount(BigDecimal.ZERO);
        invoice.setPointsUsed(0);
        invoice.setPromotionCode(null);
        invoice.setTotal(BigDecimal.valueOf(order.getTotalAmount()));
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setPaymentMethod(paymentMethod);
        invoice.setStatus(paymentMethod == PaymentMethodEnum.QR ? StatusEnum.PENDING : StatusEnum.COMPLETED);
        invoice.setStaff(staff);
        invoice.setNote(null);
        invoice.setPaymentProvider(paymentMethod == PaymentMethodEnum.QR ? "PAYOS" : null);

        // Generate transaction ID for QR payments
        if (paymentMethod == PaymentMethodEnum.QR) {
            String transactionId = generateTransactionId(order.getOrderId());
            invoice.setTransactionId(transactionId);
            PaymentLogger.info("Tạo invoice với transaction_id cho hóa đơn QR: " + transactionId);
        }

        boolean success = invoiceRepository.insert(invoice) > 0;
        if (success) {
            PaymentLogger.info("Tạo hóa đơn thành công: " + invoice.getInvoiceId());
            
            // Nếu là thanh toán QR, tạo payment link
            if (paymentMethod == PaymentMethodEnum.QR) {
                qrPaymentService.createQRPayment(invoice);
            }
        } else {
            PaymentLogger.error("Tạo hóa đơn thất bại: " + orderId);
        }
        
        return success;
    }

    /**
     * Generate a unique transaction ID for invoices
     */
    private String generateTransactionId(int orderId) {
        String prefix = "PCC";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = (int) (Math.random() * 999);
        return String.format("%s%d%s%03d", prefix, orderId, timestamp, random);
    }

    public void printInvoice(int invoiceId) {
        Invoice invoice = invoiceRepository.selectById(invoiceId);
        if (invoice == null) {
            PaymentLogger.error("Hóa đơn không tồn tại: " + invoiceId);
            throw new IllegalArgumentException("Hóa đơn không tồn tại");
        }

        String filePath = "invoice_" + invoiceId + ".pdf";
        generateInvoicePDF(invoice.getOrder().getOrderId(), filePath);

        PaymentLogger.info("Đã gửi hóa đơn " + invoiceId + " đến máy in");
        System.out.println("Đã gửi hóa đơn " + invoiceId + " đến máy in");
    }

    /**
     * Xử lý thanh toán QR
     */
    public QRPaymentService.QRPaymentResult processQRPayment(Invoice invoice) {
        if (invoice.getStatus() != StatusEnum.PENDING) {
            PaymentLogger.error("Hóa đơn không ở trạng thái PENDING: " + invoice.getInvoiceId());
            return QRPaymentService.QRPaymentResult.error("Hóa đơn không ở trạng thái chờ thanh toán");
        }
        
        // Đảm bảo phương thức thanh toán là QR
        if (invoice.getPaymentMethod() != PaymentMethodEnum.QR) {
            invoice.setPaymentMethod(PaymentMethodEnum.QR);
            invoice.setPaymentProvider("PAYOS");
            
            if (invoice.getTransactionId() == null) {
                String transactionId = generateTransactionId(invoice.getInvoiceId());
                invoice.setTransactionId(transactionId);
                PaymentLogger.info("Tạo transaction_id mới cho hóa đơn QR: " + transactionId);
            }
            
            invoiceRepository.update(invoice);
        }
        
        // Tạo QR Payment
        return qrPaymentService.createQRPayment(invoice);
    }

    /**
     * Kiểm tra trạng thái thanh toán QR
     */
    public QRPaymentService.PaymentStatusResult checkQRPaymentStatus(int invoiceId) {
        Invoice invoice = invoiceRepository.selectById(invoiceId);
        if (invoice == null || invoice.getTransactionId() == null) {
            PaymentLogger.error("Hóa đơn không tồn tại hoặc thiếu transactionId: " + invoiceId);
            return QRPaymentService.PaymentStatusResult.error("Hóa đơn không tồn tại");
        }

        return qrPaymentService.checkPaymentStatus(invoice.getTransactionId());
    }

    /**
     * Cập nhật trạng thái thanh toán QR
     */
    public boolean updateQRPaymentStatus(String transactionId, StatusEnum status, String providerTransactionId, String note) {
        try {
            Invoice invoice = getInvoiceByTransactionId(transactionId);
            if (invoice == null) {
                PaymentLogger.error("Không tìm thấy hóa đơn với transactionId: " + transactionId);
                return false;
            }

            // Cập nhật trạng thái hóa đơn
            invoice.setStatus(status);
            if (status == StatusEnum.COMPLETED) {
                invoice.setAmountPaid(invoice.getTotal());
                invoice.setPaymentDate(new Timestamp(System.currentTimeMillis()));
            }
            if (note != null) {
                invoice.setNote((invoice.getNote() != null ? invoice.getNote() + " - " : "") + note);
            }
            if (providerTransactionId != null) {
                invoice.setProviderTransactionId(providerTransactionId);
            }

            boolean success = invoiceRepository.update(invoice) > 0;
            if (success) {
                if (status == StatusEnum.COMPLETED || status == StatusEnum.CANCELLED) {
                    Order order = invoice.getOrder();
                    if (order != null) {
                        order.setStatus(status);
                        orderRepository.update(order);
                        PaymentLogger.info("Cập nhật trạng thái đơn hàng " + status + ": " + order.getOrderId());
                    }
                }
                PaymentLogger.info("Cập nhật trạng thái thanh toán QR " + status + " cho transactionId: " + transactionId);
            } else {
                PaymentLogger.error("Cập nhật trạng thái thanh toán QR thất bại cho transactionId: " + transactionId);
            }
            return success;
        } catch (Exception e) {
            PaymentLogger.error("Lỗi cập nhật trạng thái thanh toán QR: " + e.getMessage(), e);
            return false;
        }
    }

    public void generateInvoicePDF(int orderId, String filePath) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            BaseFont baseFont = BaseFont.createFont("C:/Windows/Fonts/times.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font vietnameseFont = new Font(baseFont, 12);
            Font titleFont = new Font(baseFont, 16, Font.BOLD);
            Font headerFont = new Font(baseFont, 12, Font.BOLD);

            String sql = "SELECT i.invoice_id, i.payment_date, i.subtotal, i.discount_amount, " +
                    "i.points_used, i.total, i.amount_paid, i.payment_method, i.note, i.transaction_id, " +
                    "i.status, c.customer_id, p.full_name AS customer_name, p.phone, " +
                    "s.staff_id, sp.full_name AS staff_name " +
                    "FROM invoice i " +
                    "JOIN `order` o ON i.order_id = o.order_id " +
                    "JOIN customer c ON o.customer_id = c.customer_id " +
                    "JOIN person p ON c.customer_id = p.person_id " +
                    "JOIN staff s ON i.staff_id = s.staff_id " +
                    "JOIN person sp ON s.staff_id = sp.person_id " +
                    "WHERE o.order_id = ?";

            String customerName = "Không có";
            String customerPhone = "Không có";
            String customerCode = "KH-00000";
            String staffName = "Không có";
            String paymentMethod = "CASH";
            LocalDateTime paymentDate = LocalDateTime.now();
            int invoiceId = 0;
            double totalAmount = 0.0;
            String note = null;
            String transactionId = null;
            StatusEnum status = StatusEnum.PENDING;

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, orderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        invoiceId = rs.getInt("invoice_id");
                        customerName = rs.getString("customer_name") != null ? rs.getString("customer_name") : "Không có";
                        customerPhone = rs.getString("phone") != null ? rs.getString("phone") : "Không có";
                        customerCode = "KH-" + String.format("%05d", rs.getInt("customer_id"));
                        staffName = rs.getString("staff_name") != null ? rs.getString("staff_name") : "Không có";
                        paymentDate = rs.getTimestamp("payment_date").toLocalDateTime();
                        paymentMethod = rs.getString("payment_method") != null ? rs.getString("payment_method") : "CASH";
                        totalAmount = rs.getBigDecimal("total").doubleValue();
                        note = rs.getString("note");
                        transactionId = rs.getString("transaction_id");
                        status = StatusEnum.valueOf(rs.getString("status"));
                    }
                }
            }

            Paragraph storeTitle = new Paragraph("PET CARE CENTER", titleFont);
            storeTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(storeTitle);

            Paragraph storeAddress = new Paragraph("Địa chỉ: 123 Đường ABC, Quận XYZ, TP. HCM", vietnameseFont);
            storeAddress.setAlignment(Element.ALIGN_CENTER);
            document.add(storeAddress);

            Paragraph guarantee = new Paragraph("Cam kết dịch vụ tốt nhất!", vietnameseFont);
            guarantee.setAlignment(Element.ALIGN_CENTER);
            document.add(guarantee);

            Paragraph storePhone = new Paragraph("Điện thoại: (028) 1234 5678", vietnameseFont);
            storePhone.setAlignment(Element.ALIGN_CENTER);
            document.add(storePhone);

            document.add(new Paragraph("\n", vietnameseFont));

            Paragraph title = new Paragraph("HÓA ĐƠN BÁN HÀNG", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n", vietnameseFont));

            document.add(new Paragraph("Số hóa đơn: #" + invoiceId, vietnameseFont));
            document.add(new Paragraph("Khách hàng: " + customerName, vietnameseFont));
            document.add(new Paragraph("Số điện thoại: " + customerPhone, vietnameseFont));
            document.add(new Paragraph("Mã KH: " + customerCode, vietnameseFont));
            document.add(new Paragraph("Thu ngân: " + staffName, vietnameseFont));
            document.add(new Paragraph("Ngày: " + paymentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), vietnameseFont));
            document.add(new Paragraph("\n", vietnameseFont));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 10, 50, 10, 15, 15 });

            PdfPCell cell;
            cell = new PdfPCell(new Phrase("STT", headerFont));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("Dịch vụ", headerFont));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("SL", headerFont));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("Đơn giá", headerFont));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("Thành tiền", headerFont));
            table.addCell(cell);

            List<OrderDetail> orderDetails = orderDetailRepository.selectByCondition("order_id = ?", orderId);
            int index = 1;
            double subtotal = 0;
            for (OrderDetail detail : orderDetails) {
                if (detail.getService() != null) {
                    Service service = detail.getService();
                    double lineTotal = detail.getPrice().doubleValue() * detail.getQuantity();
                    subtotal += lineTotal;

                    table.addCell(new PdfPCell(new Phrase(String.valueOf(index++), vietnameseFont)));
                    table.addCell(new PdfPCell(new Phrase(service.getName(), vietnameseFont)));
                    table.addCell(new PdfPCell(new Phrase(String.valueOf(detail.getQuantity()), vietnameseFont)));
                    table.addCell(new PdfPCell(new Phrase(String.format("%,.0f VND", detail.getPrice().doubleValue()), vietnameseFont)));
                    table.addCell(new PdfPCell(new Phrase(String.format("%,.0f VND", lineTotal), vietnameseFont)));
                }
            }

            document.add(table);
            document.add(new Paragraph("\n", vietnameseFont));

            Paragraph subtotalText = new Paragraph("Tổng tiền hàng: " + String.format("%,.0f VND", subtotal), vietnameseFont);
            subtotalText.setAlignment(Element.ALIGN_RIGHT);
            document.add(subtotalText);

            double discount = 0;
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT discount_amount FROM invoice WHERE order_id = ?")) {
                stmt.setInt(1, orderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getBigDecimal("discount_amount") != null) {
                        discount = rs.getBigDecimal("discount_amount").doubleValue();
                    }
                }
            }
            Paragraph discountText = new Paragraph("Giảm giá: " + String.format("%,.0f VND", discount), vietnameseFont);
            discountText.setAlignment(Element.ALIGN_RIGHT);
            document.add(discountText);

            int points = 0;
            double pointValue = 0;
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT points_used FROM invoice WHERE order_id = ?")) {
                stmt.setInt(1, orderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getObject("points_used") != null) {
                        points = rs.getInt("points_used");
                        pointValue = points * 1000;
                    }
                }
            }
            Paragraph pointsText = new Paragraph("Điểm quy đổi: " + String.format("%,d điểm (%,.0f VND)", points, pointValue), vietnameseFont);
            pointsText.setAlignment(Element.ALIGN_RIGHT);
            document.add(pointsText);

            double grandTotal = subtotal - discount - pointValue;
            if (grandTotal < 0) grandTotal = 0;
            Paragraph totalText = new Paragraph("Tổng cộng: " + String.format("%,.0f VND", grandTotal), headerFont);
            totalText.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalText);

            Paragraph paymentMethodText = new Paragraph("Phương thức thanh toán: " + paymentMethod, vietnameseFont);
            paymentMethodText.setAlignment(Element.ALIGN_RIGHT);
            document.add(paymentMethodText);

            document.add(new Paragraph("\n", vietnameseFont));

            if (paymentMethod.equals(PaymentMethodEnum.QR.name()) && transactionId != null && status == StatusEnum.PENDING) {
                Invoice invoice = invoiceRepository.selectById(invoiceId);
                QRPaymentService.QRPaymentResult qrPaymentResult = qrPaymentService.createQRPayment(invoice);

                if (qrPaymentResult != null && qrPaymentResult.isSuccess()) {
                    document.add(new Paragraph("\n", vietnameseFont));

                    Paragraph qrTitle = new Paragraph("Mã QR thanh toán PayOS:", headerFont);
                    qrTitle.setAlignment(Element.ALIGN_CENTER);
                    document.add(qrTitle);

                    String qrContent = qrPaymentResult.getPaymentUrl();

                    QRCodeWriter qrCodeWriter = new QRCodeWriter();
                    BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 200, 200);

                    BufferedImage qrImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
                    Graphics2D graphics = (Graphics2D) qrImage.getGraphics();
                    graphics.setColor(java.awt.Color.WHITE);
                    graphics.fillRect(0, 0, 200, 200);
                    graphics.setColor(java.awt.Color.BLACK);

                    for (int i = 0; i < 200; i++) {
                        for (int j = 0; j < 200; j++) {
                            if (bitMatrix.get(i, j)) {
                                graphics.fillRect(i, j, 1, 1);
                            }
                        }
                    }

                    File tempFile = File.createTempFile("qr_code_", ".png");
                    ImageIO.write(qrImage, "png", tempFile);

                    com.itextpdf.text.Image pdfQrImage = com.itextpdf.text.Image.getInstance(tempFile.getAbsolutePath());
                    pdfQrImage.setAlignment(Element.ALIGN_CENTER);
                    document.add(pdfQrImage);

                    tempFile.delete();

                    Paragraph qrNote = new Paragraph("Quét mã QR trên hoặc truy cập link PayOS để thanh toán.", vietnameseFont);
                    qrNote.setAlignment(Element.ALIGN_CENTER);
                    document.add(qrNote);

                    Paragraph paymentUrl = new Paragraph("Link thanh toán: " + qrContent, vietnameseFont);
                    paymentUrl.setAlignment(Element.ALIGN_CENTER);
                    document.add(paymentUrl);

                    PaymentLogger.info("Tạo mã QR cho hóa đơn: " + invoiceId);
                } else {
                    PaymentLogger.error("Tạo mã QR thất bại cho hóa đơn: " + invoiceId);
                }
            }

            Paragraph thanksText = new Paragraph("Cảm ơn quý khách đã sử dụng dịch vụ!", vietnameseFont);
            thanksText.setAlignment(Element.ALIGN_CENTER);
            document.add(thanksText);

            document.close();
            PaymentLogger.info("Tạo PDF hóa đơn thành công: " + filePath);
        } catch (DocumentException | IOException e) {
            PaymentLogger.error("Lỗi khi tạo PDF hóa đơn: " + e.getMessage(), e);
            throw new RuntimeException("Lỗi khi tạo PDF hóa đơn: " + e.getMessage());
        } catch (Exception e) {
            PaymentLogger.error("Lỗi không xác định khi tạo PDF: " + e.getMessage(), e);
            throw new RuntimeException("Lỗi không xác định khi tạo PDF: " + e.getMessage());
        }
    }

    public boolean updateInvoiceStatus(int invoiceId, StatusEnum status) {
        Invoice invoice = invoiceRepository.selectById(invoiceId);
        if (invoice == null) {
            PaymentLogger.error("Hóa đơn không tồn tại: " + invoiceId);
            throw new IllegalArgumentException("Hóa đơn không tồn tại");
        }

        invoice.setStatus(status);
        boolean success = invoiceRepository.update(invoice) > 0;
        if (success) {
            PaymentLogger.info("Cập nhật trạng thái hóa đơn " + invoiceId + " thành: " + status);
        } else {
            PaymentLogger.error("Cập nhật trạng thái hóa đơn thất bại: " + invoiceId);
        }
        return success;
    }

    /**
     * Cập nhật phương thức thanh toán
     */
    public boolean updatePaymentMethod(int invoiceId, PaymentMethodEnum paymentMethod) {
        Invoice invoice = invoiceRepository.selectById(invoiceId);
        if (invoice == null) {
            PaymentLogger.error("Hóa đơn không tồn tại: " + invoiceId);
            throw new IllegalArgumentException("Hóa đơn không tồn tại");
        }

        invoice.setPaymentMethod(paymentMethod);
        invoice.setPaymentProvider(paymentMethod == PaymentMethodEnum.QR ? "PAYOS" : null);
        
        if (paymentMethod == PaymentMethodEnum.QR && invoice.getTransactionId() == null) {
            String transactionId = generateTransactionId(invoice.getInvoiceId());
            invoice.setTransactionId(transactionId);
            PaymentLogger.info("Tạo transaction_id khi cập nhật phương thức QR: " + transactionId);
        }

        boolean success = invoiceRepository.update(invoice) > 0;
        if (success) {
            PaymentLogger.info("Cập nhật phương thức thanh toán hóa đơn " + invoiceId + " thành: " + paymentMethod);
            
            // Nếu đổi sang thanh toán QR, tạo payment link
            if (paymentMethod == PaymentMethodEnum.QR) {
                qrPaymentService.createQRPayment(invoice);
            }
        } else {
            PaymentLogger.error("Cập nhật phương thức thanh toán thất bại: " + invoiceId);
        }
        return success;
    }

    public boolean updateInvoiceNote(int invoiceId, String note) {
        try {
            String sql = "UPDATE invoice SET note = ? WHERE invoice_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, note);
                stmt.setInt(2, invoiceId);
                int result = stmt.executeUpdate();
                if (result >0) {
                    PaymentLogger.info("Cập nhật ghi chú hóa đơn " + invoiceId + " thành công");
                } else {
                    PaymentLogger.error("Cập nhật ghi chú hóa đơn thất bại: " + invoiceId);
                }
                return result > 0;
            }
        } catch (SQLException e) {
            PaymentLogger.error("Lỗi khi cập nhật ghi chú hóa đơn: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean updateOrderStatus(int orderId, StatusEnum status) {
        try {
            String sql = "UPDATE `order` SET status = ? WHERE order_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status.name());
                stmt.setInt(2, orderId);
                int result = stmt.executeUpdate();
                if (result > 0) {
                    PaymentLogger.info("Cập nhật trạng thái đơn hàng " + orderId + " thành: " + status);
                } else {
                    PaymentLogger.error("Cập nhật trạng thái đơn hàng thất bại: " + orderId);
                }
                return result > 0;
            }
        } catch (SQLException e) {
            PaymentLogger.error("Lỗi khi cập nhật trạng thái đơn hàng: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean processPayment(int invoiceId, PaymentMethodEnum paymentMethod, double amountPaid) {
        Invoice invoice = invoiceRepository.selectById(invoiceId);
        if (invoice == null) {
            PaymentLogger.error("Hóa đơn không tồn tại: " + invoiceId);
            throw new IllegalArgumentException("Hóa đơn không tồn tại");
        }

        Staff staff = Session.getCurrentStaff();
        if (staff == null) {
            PaymentLogger.error("Không có thông tin nhân viên");
            throw new IllegalArgumentException("Không có thông tin nhân viên");
        }

        invoice.setPaymentDate(new Timestamp(System.currentTimeMillis()));
        invoice.setStatus(StatusEnum.COMPLETED);
        invoice.setPaymentMethod(paymentMethod);
        invoice.setAmountPaid(BigDecimal.valueOf(amountPaid));
        invoice.setStaff(staff);
        invoice.setPaymentProvider(paymentMethod == PaymentMethodEnum.QR ? "PAYOS" : null);

        if (paymentMethod == PaymentMethodEnum.QR && invoice.getTransactionId() == null) {
            String transactionId = generateTransactionId(invoice.getInvoiceId());
            invoice.setTransactionId(transactionId);
            PaymentLogger.info("Tạo transaction_id cho hóa đơn QR: " + transactionId);
        }

        boolean success = invoiceRepository.update(invoice) > 0;
        
        if (success) {
            Order order = invoice.getOrder();
            if (order != null) {
                order.setStatus(StatusEnum.COMPLETED);
                orderRepository.update(order);
                PaymentLogger.info("Cập nhật trạng thái đơn hàng COMPLETED: " + order.getOrderId());
            }
        } else {
            PaymentLogger.error("Xử lý thanh toán thất bại: " + invoiceId);
        }
        
        return success;
    }

    /**
     * Lấy các hóa đơn QR đang chờ thanh toán
     */
    public List<Invoice> getPendingQRInvoices() {
        return invoiceRepository.getPendingQRInvoices();
    }
    
    /**
     * Lấy hóa đơn theo mã giao dịch
     */
    public Invoice getInvoiceByTransactionId(String transactionId) {
        List<Invoice> invoices = invoiceRepository.selectByCondition("transaction_id = ?", transactionId);
        return invoices.isEmpty() ? null : invoices.get(0);
    }
    
    /**
     * Lấy hóa đơn theo mã giao dịch nhà cung cấp
     */
    public List<Invoice> getInvoicesByProviderTransaction(String providerTransactionId) {
        return invoiceRepository.selectByCondition("provider_transaction_id = ?", providerTransactionId);
    }
    
    /**
     * Hủy thanh toán QR
     */
    public boolean cancelQRPayment(int invoiceId, String reason) {
        Invoice invoice = invoiceRepository.selectById(invoiceId);
        if (invoice == null) {
            PaymentLogger.error("Hóa đơn không tồn tại: " + invoiceId);
            return false;
        }
        
        if (invoice.getStatus() != StatusEnum.PENDING) {
            PaymentLogger.error("Chỉ có thể hủy hóa đơn đang chờ thanh toán: " + invoiceId);
            return false;
        }
        
        if (invoice.getPaymentMethod() != PaymentMethodEnum.QR) {
            PaymentLogger.error("Hóa đơn không phải thanh toán QR: " + invoiceId);
            return false;
        }
        
        // Hủy thanh toán trên PayOS nếu có provider_transaction_id
        if (invoice.getProviderTransactionId() != null) {
            qrPaymentService.cancelQRPayment(invoice.getTransactionId(), reason);
        }
        
        // Cập nhật trạng thái hóa đơn
        invoice.setStatus(StatusEnum.CANCELLED);
        invoice.setNote((invoice.getNote() != null ? invoice.getNote() + " - " : "") + "Đã hủy: " + reason);
        
        boolean success = invoiceRepository.update(invoice) > 0;
        if (success) {
            PaymentLogger.info("Hủy thanh toán QR thành công cho hóa đơn: " + invoiceId);
        } else {
            PaymentLogger.error("Hủy thanh toán QR thất bại cho hóa đơn: " + invoiceId);
        }
        return success;
    }

    public List<Invoice> getInvoicesByCustomer(int customerId) {
        String condition = "o.customer_id = ?";
        List<Invoice> invoices = invoiceRepository.selectByCondition(condition, customerId);
        PaymentLogger.info("Lấy danh sách hóa đơn cho khách hàng: " + customerId);
        return invoices;
    }

    public List<Invoice> getInvoicesByStaff(int staffId) {
        String condition = "staff_id = ?";
        List<Invoice> invoices = invoiceRepository.selectByCondition(condition, staffId);
        PaymentLogger.info("Lấy danh sách hóa đơn cho nhân viên: " + staffId);
        return invoices;
    }

    /**
     * Cập nhật thông tin hóa đơn
     */
    public boolean update(Invoice invoice) {
        if (invoice == null) {
            PaymentLogger.error("Không thể cập nhật hóa đơn null");
            return false;
        }
        
        try {
            int result = invoiceRepository.update(invoice);
            
            if (result > 0) {
                PaymentLogger.info("Cập nhật hóa đơn thành công: #" + invoice.getInvoiceId() + 
                                  ", Trạng thái: " + invoice.getStatus());
                
                // Nếu hóa đơn đã hoàn thành, cập nhật trạng thái đơn hàng
                if (invoice.getStatus() == StatusEnum.COMPLETED && invoice.getOrder() != null) {
                    invoice.getOrder().setStatus(StatusEnum.COMPLETED);
                    orderRepository.update(invoice.getOrder());
                    PaymentLogger.info("Cập nhật trạng thái đơn hàng thành COMPLETED: #" + 
                                      invoice.getOrder().getOrderId());
                }
                
                return true;
            } else {
                PaymentLogger.error("Cập nhật hóa đơn thất bại: #" + invoice.getInvoiceId());
                return false;
            }
        } catch (Exception e) {
            PaymentLogger.error("Lỗi cập nhật hóa đơn: " + e.getMessage(), e);
            return false;
        }
    }
    
    public double getTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        List<Invoice> invoices = getInvoicesByDateRange(startDate, endDate);
        double totalRevenue = invoices.stream()
                .filter(i -> i.getStatus() == StatusEnum.COMPLETED)
                .mapToDouble(i -> i.getTotal().doubleValue())
                .sum();
        PaymentLogger.info("Tổng doanh thu từ " + startDate + " đến " + endDate + ": " + totalRevenue);
        return totalRevenue;
    }

    public List<Invoice> getUnpaidInvoices() {
        String condition = "status != ?";
        List<Invoice> invoices = invoiceRepository.selectByCondition(condition, StatusEnum.COMPLETED.name());
        PaymentLogger.info("Lấy danh sách hóa đơn chưa thanh toán");
        return invoices;
    }
}