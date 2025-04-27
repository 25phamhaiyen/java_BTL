package service;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;

import enums.PaymentMethodEnum;
import enums.StatusEnum;
import model.Booking;
import model.Invoice;
import model.Order;
import model.OrderDetail;
import model.Staff;
import repository.BookingRepository;
import repository.InvoiceRepository;
import repository.OrderDetailRepository;
import repository.OrderRepository;

import utils.Session;

public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final BookingRepository bookingRepository;

    public InvoiceService() {
        this.invoiceRepository = InvoiceRepository.getInstance();
        this.orderRepository = OrderRepository.getInstance();
        this.orderDetailRepository = OrderDetailRepository.getInstance();
        this.bookingRepository = BookingRepository.getInstance();
    }

    /**
     * Lấy tất cả hóa đơn
     */
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.selectAll();
    }

    /**
     * Lấy hóa đơn theo ID
     */
    public Invoice getInvoiceById(int invoiceId) {
        return invoiceRepository.selectById(invoiceId);
    }

    /**
     * Lấy hóa đơn theo khoảng thời gian
     */
    public List<Invoice> getInvoicesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        String condition = "payment_date BETWEEN ? AND ?";
        return invoiceRepository.selectByCondition(condition, java.sql.Timestamp.valueOf(startDate), 
                                                   java.sql.Timestamp.valueOf(endDate));
    }

    /**
     * Lấy hóa đơn gần đây
     */
    public List<Invoice> getRecentInvoices(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        String condition = "payment_date >= ?";
        return invoiceRepository.selectByCondition(condition, java.sql.Timestamp.valueOf(startDate));
    }

    /**
     * Tạo hóa đơn mới từ đơn hàng
     */
    public boolean createInvoice(int orderId, PaymentMethodEnum paymentMethod) {
        // Lấy thông tin đơn hàng
        Order order = orderRepository.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Đơn hàng không tồn tại");
        }

        // Lấy thông tin nhân viên hiện tại
        Staff staff = Session.getCurrentStaff();
        if (staff == null) {
            throw new IllegalArgumentException("Không có thông tin nhân viên");
        }

        // Tạo hóa đơn mới
        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setPaymentDate(new java.sql.Timestamp(System.currentTimeMillis()));
        invoice.setTotal(BigDecimal.valueOf(order.getTotalAmount()));
        invoice.setPaymentMethod(paymentMethod);
        invoice.setStatus(StatusEnum.COMPLETED);
        invoice.setStaff(staff);

        // Lưu hóa đơn
        return invoiceRepository.insert(invoice) > 0;
    }

    /**
     * In hóa đơn
     */
    public void printInvoice(int invoiceId) {
        Invoice invoice = invoiceRepository.selectById(invoiceId);
        if (invoice == null) {
            throw new IllegalArgumentException("Hóa đơn không tồn tại");
        }

        // Tạo file PDF
        String filePath = "invoice_" + invoiceId + ".pdf";
        generateInvoicePDF(invoice.getOrder().getOrderId(), filePath);
        
        // Gửi đến máy in (mô phỏng)
        System.out.println("Đã gửi hóa đơn " + invoiceId + " đến máy in");
    }

    /**
     * Gửi hóa đơn qua email
     */
    public void sendInvoiceByEmail(int invoiceId) throws Exception {
        Invoice invoice = invoiceRepository.selectById(invoiceId);
        if (invoice == null) {
            throw new IllegalArgumentException("Hóa đơn không tồn tại");
        }

        // Tạo file PDF
        String filePath = "invoice_" + invoiceId + ".pdf";
        generateInvoicePDF(invoice.getOrder().getOrderId(), filePath);

        // Lấy email khách hàng
        String customerEmail = invoice.getOrder().getCustomer().getEmail();
        if (customerEmail == null || customerEmail.isEmpty()) {
            throw new IllegalArgumentException("Email khách hàng không có");
        }

        // Gửi email
        sendEmail(customerEmail, "Hóa đơn #" + invoiceId, 
                 "Xin chào,\n\nVui lòng xem hóa đơn đính kèm.\n\nTrân trọng,\nPet Service", 
                 filePath);
    }

    /**
     * Tạo file PDF hóa đơn
     */
    private void generateInvoicePDF(int orderId, String filePath) {
        // Đây là nơi bạn sẽ implement logic tạo PDF
        // Có thể sử dụng thư viện như iText, Apache PDFBox, etc.
        // Ví dụ code giả:
        System.out.println("Đã tạo file PDF hóa đơn tại: " + filePath);
        
        // TODO: Implement chi tiết tạo PDF với thông tin từ order
        // Lấy thông tin order
        Order order = orderRepository.selectById(orderId);
        List<OrderDetail> orderDetails = orderDetailRepository.selectByCondition("order_id = ?", orderId);
        
        // Tạo PDF với thông tin order và chi tiết
        // ...
    }

    /**
     * Gửi email
     */
    private void sendEmail(String to, String subject, String text, String attachmentPath) throws Exception {
        // Cấu hình email
        final String from = "your-email@example.com"; // Email của bạn
        final String password = "your-password"; // Mật khẩu email

        // Cấu hình properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Tạo session
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            // Tạo message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            // Tạo phần text
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(text);

            // Tạo phần đính kèm
            MimeBodyPart attachmentPart = new MimeBodyPart();
            DataSource source = new FileDataSource(attachmentPath);
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName(new File(attachmentPath).getName());

            // Kết hợp các phần
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            // Gửi email
            Transport.send(message);
            System.out.println("Email đã được gửi thành công!");

        } catch (MessagingException e) {
            throw new RuntimeException("Lỗi khi gửi email: " + e.getMessage());
        }
    }

    /**
     * Cập nhật trạng thái hóa đơn
     */
    public boolean updateInvoiceStatus(int invoiceId, StatusEnum status) {
        Invoice invoice = invoiceRepository.selectById(invoiceId);
        if (invoice == null) {
            throw new IllegalArgumentException("Hóa đơn không tồn tại");
        }

        invoice.setStatus(status);
        return invoiceRepository.update(invoice) > 0;
    }

    /**
     * Cập nhật phương thức thanh toán
     */
    public boolean updatePaymentMethod(int invoiceId, PaymentMethodEnum paymentMethod) {
        Invoice invoice = invoiceRepository.selectById(invoiceId);
        if (invoice == null) {
            throw new IllegalArgumentException("Hóa đơn không tồn tại");
        }

        invoice.setPaymentMethod(paymentMethod);
        return invoiceRepository.update(invoice) > 0;
    }

    /**
     * Lấy danh sách hóa đơn theo khách hàng
     */
    public List<Invoice> getInvoicesByCustomer(int customerId) {
        String condition = "o.customer_id = ?";
        return invoiceRepository.selectByCondition(condition, customerId);
    }

    /**
     * Lấy danh sách hóa đơn theo nhân viên
     */
    public List<Invoice> getInvoicesByStaff(int staffId) {
        String condition = "staff_id = ?";
        return invoiceRepository.selectByCondition(condition, staffId);
    }

    /**
     * Tính tổng doanh thu theo khoảng thời gian
     */
    public double getTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        List<Invoice> invoices = getInvoicesByDateRange(startDate, endDate);
        return invoices.stream()
                      .filter(i -> i.getStatus() == StatusEnum.COMPLETED)
                      .mapToDouble(i -> i.getTotal().doubleValue())
                      .sum();
    }

    /**
     * Lấy danh sách hóa đơn chưa thanh toán
     */
    public List<Invoice> getUnpaidInvoices() {
        String condition = "status != ?";
        return invoiceRepository.selectByCondition(condition, StatusEnum.COMPLETED.name());
    }
}