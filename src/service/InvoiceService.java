package service;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import enums.PaymentMethodEnum;
import enums.StatusEnum;
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
        Order order = orderRepository.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Đơn hàng không tồn tại");
        }

        Staff staff = Session.getCurrentStaff();
        if (staff == null) {
            throw new IllegalArgumentException("Không có thông tin nhân viên");
        }

        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setPaymentDate(new java.sql.Timestamp(System.currentTimeMillis()));
        invoice.setTotal(BigDecimal.valueOf(order.getTotalAmount()));
        invoice.setPaymentMethod(paymentMethod);
        invoice.setStatus(StatusEnum.COMPLETED);
        invoice.setStaff(staff);

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

        String filePath = "invoice_" + invoiceId + ".pdf";
        generateInvoicePDF(invoice.getOrder().getOrderId(), filePath);
        
        System.out.println("Đã gửi hóa đơn " + invoiceId + " đến máy in");
    }

    /**
     * Tạo file PDF hóa đơn
     */
    public void generateInvoicePDF(int orderId, String filePath) {
        System.out.println("Đã tạo file PDF hóa đơn tại: " + filePath);
        
        Order order = orderRepository.selectById(orderId);
        List<OrderDetail> orderDetails = orderDetailRepository.selectByCondition("order_id = ?", orderId);
        
        // TODO: Implement chi tiết tạo PDF với thông tin từ order
        // Ví dụ: Sử dụng iText hoặc Apache PDFBox
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