package service;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import controllers.Staff.InvoiceViewController.RevenueReport;
import javafx.collections.ObservableList;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        invoice.setSubtotal(BigDecimal.valueOf(order.getTotalAmount())); // Thiết lập subtotal
        invoice.setDiscountPercent(BigDecimal.ZERO); // Mặc định giảm giá là 0
        invoice.setDiscountAmount(BigDecimal.ZERO); // Mặc định số tiền giảm giá là 0
        invoice.setPointsUsed(0); // Mặc định không sử dụng điểm
        invoice.setPromotionCode(null); // Mặc định không có mã khuyến mãi
        invoice.setTotal(BigDecimal.valueOf(order.getTotalAmount()));
        invoice.setAmountPaid(BigDecimal.valueOf(order.getTotalAmount())); // Giả định số tiền thanh toán bằng tổng tiền
        invoice.setPaymentMethod(paymentMethod);
        invoice.setStatus(StatusEnum.COMPLETED);
        invoice.setStaff(staff);
        invoice.setNote(null); // Mặc định không có ghi chú

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
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            
            // Tiêu đề hóa đơn
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("Hóa đơn #" + orderId, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));
            
            // Lấy thông tin đơn hàng
            Order order = orderRepository.selectById(orderId);
            if (order == null) {
                throw new IllegalArgumentException("Đơn hàng không tồn tại");
            }
            
            // Thông tin khách hàng
            document.add(new Paragraph("Khách hàng: " + 
                    (order.getCustomer() != null ? order.getCustomer().getFullName() : "N/A")));
            document.add(new Paragraph("Số điện thoại: " + 
                    (order.getCustomer() != null ? order.getCustomer().getPhone() : "N/A")));
            document.add(new Paragraph("Ngày: " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
            document.add(new Paragraph("\n"));
            
            // Tạo bảng chi tiết dịch vụ
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{10, 50, 10, 15, 15});
            
            // Tiêu đề bảng
            table.addCell(new PdfPCell(new Phrase("STT")));
            table.addCell(new PdfPCell(new Phrase("Dịch vụ")));
            table.addCell(new PdfPCell(new Phrase("SL")));
            table.addCell(new PdfPCell(new Phrase("Đơn giá")));
            table.addCell(new PdfPCell(new Phrase("Thành tiền")));
            
            // Dữ liệu bảng
            List<OrderDetail> orderDetails = orderDetailRepository.selectByCondition("order_id = ?", orderId);
            int index = 1;
            for (OrderDetail detail : orderDetails) {
                Service service = detail.getService();
                if (service != null) {
                    table.addCell(String.valueOf(index++));
                    table.addCell(service.getName());
                    table.addCell(String.valueOf(detail.getQuantity()));
                    table.addCell(String.format("%,.0f VND", detail.getPrice().doubleValue()));
                    table.addCell(String.format("%,.0f VND", 
                            detail.getPrice().doubleValue() * detail.getQuantity()));
                }
            }
            
            document.add(table);
            
            // Tổng tiền
            Paragraph total = new Paragraph("Tổng tiền: " + String.format("%,.0f VND", order.getTotalAmount()));
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);
            
            document.close();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Lỗi khi tạo PDF hóa đơn: " + e.getMessage());
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

    /**
     * Tạo file PDF báo cáo doanh thu
     */
    public void generateReportPDF(ObservableList<RevenueReport> reports, String reportType, String fileName) throws IOException {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();
            
            // Tiêu đề báo cáo
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("Báo cáo " + reportType, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("Từ ngày: " + LocalDate.now().minusDays(30).format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            document.add(new Paragraph("Đến ngày: " + LocalDate.now().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            document.add(new Paragraph("\n"));
            
            // Tạo bảng báo cáo
            PdfPTable table = new PdfPTable(reportType.equals("Doanh thu theo ngày") ? 8 : 7);
            table.setWidthPercentage(100);
            table.setWidths(reportType.equals("Doanh thu theo ngày") ? 
                    new float[]{15, 10, 15, 15, 15, 15, 15, 15} : 
                    new float[]{20, 10, 15, 15, 15, 15, 15});
            
            // Tiêu đề bảng
            table.addCell(new PdfPCell(new Phrase(reportType.equals("Doanh thu theo ngày") ? "Ngày" : 
                    (reportType.equals("Doanh thu theo dịch vụ") ? "Dịch vụ" : "Phương thức TT"))));
            table.addCell(new PdfPCell(new Phrase("Số hóa đơn")));
            table.addCell(new PdfPCell(new Phrase("Doanh thu")));
            table.addCell(new PdfPCell(new Phrase("Giảm giá")));
            table.addCell(new PdfPCell(new Phrase("Khuyến mãi")));
            table.addCell(new PdfPCell(new Phrase("Điểm tích lũy")));
            table.addCell(new PdfPCell(new Phrase("Doanh thu thuần")));
            if (reportType.equals("Doanh thu theo ngày")) {
                table.addCell(new PdfPCell(new Phrase("Xu hướng")));
            }
            
            // Dữ liệu bảng
            for (RevenueReport report : reports) {
                table.addCell(report.getDate());
                table.addCell(String.valueOf(report.getInvoiceCount()));
                table.addCell(String.format("%,.0f", report.getRevenue()));
                table.addCell(String.format("%,.0f", report.getDiscount()));
                table.addCell(String.format("%,.0f", report.getPromotion()));
                table.addCell(String.format("%,.0f", report.getPoints()));
                table.addCell(String.format("%,.0f", report.getNetRevenue()));
                if (reportType.equals("Doanh thu theo ngày")) {
                    table.addCell(report.getTrend());
                }
            }
            
            document.add(table);
            document.close();
        } catch (DocumentException | IOException e) {
            throw new IOException("Lỗi khi tạo PDF báo cáo: " + e.getMessage());
        }
    }
}