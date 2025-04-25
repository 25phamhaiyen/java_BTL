package service;

import java.util.List;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;

import model.Invoice;
import model.Order;
import model.OrderDetail;
import repository.InvoiceRepository;
import repository.OrderDetailRepository;
import repository.OrderRepository;

public class InvoiceService {
	private final InvoiceRepository invoiceRepository;
	private OrderRepository orderRepository;
	private OrderDetailRepository orderDetailRepository;

	public InvoiceService(InvoiceRepository invoiceRepo, OrderRepository orderRepo,
			OrderDetailRepository orderDetailRepo) {
		this.invoiceRepository = invoiceRepo;
		this.orderRepository = orderRepo;
		this.orderDetailRepository = orderDetailRepo;

	// Phương thức xuất hóa đơn ra file PDF
	public void generateInvoicePDF(int orderId, String filePath) {
		try {
			// Tạo PdfFont với phông Arial Unicode MS từ hệ thống (hoặc phông hỗ trợ tiếng
			// Việt)
			PdfFont vietnameseFont = PdfFontFactory.createFont("C:/Windows/Fonts/arial.ttf",
					PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);

			// 1. Lấy thông tin đơn hàng từ OrderRepository
			List<Order> orders = orderRepository.selectByCondition("OrderID = ?", orderId);
			if (orders.isEmpty()) {
				System.out.println("Không tìm thấy đơn hàng.");
				return;
			}
			Order order = orders.get(0);

			// 2. Lấy thông tin hóa đơn từ InvoiceRepository
			List<Invoice> invoices = invoiceRepository.selectByCondition("OrderID = ?", orderId);
			if (invoices.isEmpty()) {
				System.out.println("Không tìm thấy hóa đơn.");
				return;
			}
			Invoice invoice = invoices.get(0);

			// 3. Lấy thông tin chi tiết đơn hàng từ OrderDetailRepository
			List<OrderDetail> orderDetails = orderDetailRepository.selectByCondition("OrderID = ?", orderId);

			// Tạo PdfWriter
			PdfWriter writer = new PdfWriter(filePath);

			// Tạo PdfDocument
			PdfDocument pdfDoc = new PdfDocument(writer);

			// Tạo Document từ PdfDocument
			Document document = new Document(pdfDoc);

			// Tiêu đề (Hóa đơn bán hàng)
			PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");
			Paragraph title = new Paragraph(
					new Text("HÓA ĐƠN SỐ " + invoice.getInvoiceId()).setFont(boldFont).setFont(vietnameseFont))
					.setTextAlignment(TextAlignment.CENTER).setFontSize(20);
			document.add(title);

			// Thông tin nhân viên (có thể lấy từ Order hoặc thông tin hệ thống)
			Paragraph staffInfo = new Paragraph("Nhân viên: " + order.getStaff().getFullName()).setFont(vietnameseFont)
					.setTextAlignment(TextAlignment.LEFT).setFontSize(12);
			document.add(staffInfo);

			// Thông tin người mua
			Paragraph buyerInfo = new Paragraph("KHÁCH HÀNG: " + order.getCustomer().getFullName() + "\n"
					+ "Số điện thoại: " + order.getCustomer().getPhone() + "\n").setFont(vietnameseFont)
					.setTextAlignment(TextAlignment.LEFT).setFontSize(12);
			document.add(buyerInfo);

			// Thông tin hóa đơn
			document.add(new Paragraph("Mã hóa đơn: " + invoice.getInvoiceId()).setFont(vietnameseFont));

			// Tạo bảng chi tiết đơn hàng
			Table table = new Table(5); // 5 cột: Mã dịch vụ, Tên dịch vụ, Số lượng, Đơn giá, Thành tiền
			table.addCell(
					new Cell().add(new Paragraph(new Text("Mã dịch vụ").setFont(boldFont).setFont(vietnameseFont)))); // Mã
																														// dịch
																														// vụ
			table.addCell(
					new Cell().add(new Paragraph(new Text("Tên dịch vụ").setFont(boldFont).setFont(vietnameseFont)))); // Tên
																														// dịch
																														// vụ
			table.addCell(
					new Cell().add(new Paragraph(new Text("Số lượng").setFont(boldFont).setFont(vietnameseFont)))); // Số
																													// lượng
			table.addCell(new Cell().add(new Paragraph(new Text("Đơn giá").setFont(boldFont).setFont(vietnameseFont)))); // Đơn
																															// giá
			table.addCell(
					new Cell().add(new Paragraph(new Text("Thành tiền").setFont(boldFont).setFont(vietnameseFont)))); // Thành
																														// tiền

			// Thêm chi tiết vào bảng
			for (OrderDetail detail : orderDetails) {
				table.addCell(new Cell().add(
						new Paragraph(String.valueOf(detail.getService().getServiceId())).setFont(vietnameseFont)));
				table.addCell(new Cell().add(new Paragraph(detail.getService().getName())));
				table.addCell(new Cell().add(new Paragraph(String.valueOf(detail.getQuantity()))));
				table.addCell(new Cell().add(new Paragraph(detail.getPrice().toString())));
			}

			document.add(table);

			// Thông tin thanh toán
			Paragraph paymentInfo = new Paragraph("Tổng số tiền: " + invoice.getTotal() + " VNĐ\n")
					.setFont(vietnameseFont).setTextAlignment(TextAlignment.LEFT).setFontSize(12);
			document.add(paymentInfo);

			// Ngày xuất hóa đơn (in bill)
			Paragraph printDate = new Paragraph("Ngày in hóa đơn: " + java.time.LocalDate.now()).setFont(vietnameseFont)
					.setTextAlignment(TextAlignment.RIGHT).setFontSize(12);
			document.add(printDate);

			document.close();
			System.out.println("Hóa đơn đã được tạo thành công!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
