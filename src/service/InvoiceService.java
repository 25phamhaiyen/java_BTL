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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

			// Tải font hỗ trợ tiếng Việt
			BaseFont baseFont = BaseFont.createFont("C:/Windows/Fonts/times.ttf", BaseFont.IDENTITY_H,
					BaseFont.EMBEDDED);
			Font vietnameseFont = new Font(baseFont, 12);
			Font titleFont = new Font(baseFont, 16, Font.BOLD);
			Font headerFont = new Font(baseFont, 12, Font.BOLD);

			// Lấy đầy đủ thông tin từ database
			String sql = "SELECT i.invoice_id, i.payment_date, i.subtotal, i.discount_amount, "
					+ "i.points_used, i.total, i.amount_paid, i.payment_method, "
					+ "c.customer_id, p.full_name AS customer_name, p.phone, "
					+ "s.staff_id, sp.full_name AS staff_name " + "FROM invoice i "
					+ "JOIN `order` o ON i.order_id = o.order_id " + "JOIN customer c ON o.customer_id = c.customer_id "
					+ "JOIN person p ON c.customer_id = p.person_id " + "JOIN staff s ON i.staff_id = s.staff_id "
					+ "JOIN person sp ON s.staff_id = sp.person_id " + "WHERE o.order_id = ?";

			String customerName = "Không có";
			String customerPhone = "Không có";
			String customerCode = "KH-00000";
			String staffName = "Không có";
			LocalDateTime paymentDate = LocalDateTime.now();
			int invoiceId = 0;

			try (Connection conn = DatabaseConnection.getConnection();
					PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setInt(1, orderId);
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						invoiceId = rs.getInt("invoice_id");
						customerName = rs.getString("customer_name") != null ? rs.getString("customer_name")
								: "Không có";
						customerPhone = rs.getString("phone") != null ? rs.getString("phone") : "Không có";
						customerCode = "KH-" + String.format("%05d", rs.getInt("customer_id"));
						staffName = rs.getString("staff_name") != null ? rs.getString("staff_name") : "Không có";
						paymentDate = rs.getTimestamp("payment_date").toLocalDateTime();
					}
				}
			}

			// Tiêu đề hóa đơn
			Paragraph storeTitle = new Paragraph("PET CARE CENTER", titleFont);
			storeTitle.setAlignment(Element.ALIGN_CENTER);
			document.add(storeTitle);

			Paragraph storeAddress = new Paragraph("Địa chỉ: 123 Đường ABC, Quận XYZ, TP. HCM", vietnameseFont);
			storeAddress.setAlignment(Element.ALIGN_CENTER);
			document.add(storeAddress);

			Paragraph storePhone = new Paragraph("Điện thoại: (028) 1234 5678", vietnameseFont);
			storePhone.setAlignment(Element.ALIGN_CENTER);
			document.add(storePhone);

			document.add(new Paragraph("\n", vietnameseFont));

			Paragraph title = new Paragraph("HÓA ĐƠN BÁN HÀNG", titleFont);
			title.setAlignment(Element.ALIGN_CENTER);
			document.add(title);
			document.add(new Paragraph("\n", vietnameseFont));

			// Thông tin khách hàng và nhân viên
			document.add(new Paragraph("Số hóa đơn: #" + invoiceId, vietnameseFont));
			document.add(new Paragraph("Khách hàng: " + customerName, vietnameseFont));
			document.add(new Paragraph("Số điện thoại: " + customerPhone, vietnameseFont));
			document.add(new Paragraph("Mã KH: " + customerCode, vietnameseFont));
			document.add(new Paragraph("Thu ngân: " + staffName, vietnameseFont));
			document.add(new Paragraph(
					"Ngày: " + paymentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), vietnameseFont));
			document.add(new Paragraph("\n", vietnameseFont));

			// Tạo bảng chi tiết dịch vụ
			PdfPTable table = new PdfPTable(5);
			table.setWidthPercentage(100);
			table.setWidths(new float[] { 10, 50, 10, 15, 15 });

			// Tiêu đề bảng
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

			// Dữ liệu bảng
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
					table.addCell(new PdfPCell(
							new Phrase(String.format("%,.0f VND", detail.getPrice().doubleValue()), vietnameseFont)));
					table.addCell(new PdfPCell(new Phrase(String.format("%,.0f VND", lineTotal), vietnameseFont)));
				}
			}

			document.add(table);
			document.add(new Paragraph("\n", vietnameseFont));

			// Tổng tiền và các thông tin thanh toán khác
			// Subtotal
			Paragraph subtotalText = new Paragraph("Tổng tiền hàng: " + String.format("%,.0f VND", subtotal),
					vietnameseFont);
			subtotalText.setAlignment(Element.ALIGN_RIGHT);
			document.add(subtotalText);

			// Giảm giá
			double discount = 0;
			try (Connection conn = DatabaseConnection.getConnection();
					PreparedStatement stmt = conn
							.prepareStatement("SELECT discount_amount FROM invoice WHERE order_id = ?")) {
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

			// Điểm quy đổi
			int points = 0;
			double pointValue = 0;
			try (Connection conn = DatabaseConnection.getConnection();
					PreparedStatement stmt = conn
							.prepareStatement("SELECT points_used FROM invoice WHERE order_id = ?")) {
				stmt.setInt(1, orderId);
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next() && rs.getObject("points_used") != null) {
						points = rs.getInt("points_used");
						pointValue = points * 1000; // Giả sử 1 điểm = 1000 VND
					}
				}
			}
			Paragraph pointsText = new Paragraph(
					"Điểm quy đổi: " + String.format("%,d điểm (%,.0f VND)", points, pointValue), vietnameseFont);
			pointsText.setAlignment(Element.ALIGN_RIGHT);
			document.add(pointsText);

			// Tổng cộng
			double grandTotal = subtotal - discount - pointValue;
			if (grandTotal < 0)
				grandTotal = 0;
			Paragraph totalText = new Paragraph("Tổng cộng: " + String.format("%,.0f VND", grandTotal), headerFont);
			totalText.setAlignment(Element.ALIGN_RIGHT);
			document.add(totalText);

			// Phương thức thanh toán
			String paymentMethod = "CASH";
			try (Connection conn = DatabaseConnection.getConnection();
					PreparedStatement stmt = conn
							.prepareStatement("SELECT payment_method FROM invoice WHERE order_id = ?")) {
				stmt.setInt(1, orderId);
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next() && rs.getString("payment_method") != null) {
						paymentMethod = rs.getString("payment_method");
					}
				}
			}
			Paragraph paymentMethodText = new Paragraph("Phương thức thanh toán: " + paymentMethod, vietnameseFont);
			paymentMethodText.setAlignment(Element.ALIGN_RIGHT);
			document.add(paymentMethodText);

			document.add(new Paragraph("\n", vietnameseFont));

			// Cảm ơn
			Paragraph thanksText = new Paragraph("Cảm ơn quý khách đã sử dụng dịch vụ!", vietnameseFont);
			thanksText.setAlignment(Element.ALIGN_CENTER);
			document.add(thanksText);

			document.close();
		} catch (DocumentException | IOException e) {
			throw new RuntimeException("Lỗi khi tạo PDF hóa đơn: " + e.getMessage());
		} catch (Exception e) {
			throw new RuntimeException("Lỗi không xác định khi tạo PDF: " + e.getMessage());
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
		return invoices.stream().filter(i -> i.getStatus() == StatusEnum.COMPLETED)
				.mapToDouble(i -> i.getTotal().doubleValue()).sum();
	}

	/**
	 * Lấy danh sách hóa đơn chưa thanh toán
	 */
	public List<Invoice> getUnpaidInvoices() {
		String condition = "status != ?";
		return invoiceRepository.selectByCondition(condition, StatusEnum.COMPLETED.name());
	}
}