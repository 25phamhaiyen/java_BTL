package tests;

import dao.InvoiceDAO;
import dao.OrderDAO;
import dao.PaymentStatusDAO;
import entity.Invoice;
import entity.Order;
import entity.PaymentStatus;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

public class TestInvoiceDAO {
    public static void main(String[] args) {
        InvoiceDAO invoiceDAO = new InvoiceDAO();
        OrderDAO orderDAO = new OrderDAO();
        PaymentStatusDAO paymentStatusDAO = new PaymentStatusDAO();
        
        
        Order order = orderDAO.selectById(3);
        if (order == null) {
            System.out.println("Không tìm thấy Order có ID = 3. Vui lòng kiểm tra lại dữ liệu!");
            return;
        }
        
        PaymentStatus payment = paymentStatusDAO.selectById(1);
        
        
        // 1. Test Insert
        
        Invoice newInvoice = new Invoice(0, order, BigDecimal.valueOf(order.getTotal()), new Timestamp(System.currentTimeMillis()), payment);

        int insertResult = invoiceDAO.insert(newInvoice);
        System.out.println("Insert Result: " + insertResult);
        System.out.println("Inserted Invoice ID: " + newInvoice.getInvoiceId());

        // 2. Test Update
        newInvoice.setTotalAmount(BigDecimal.valueOf(order.getTotal()));
        int updateResult = invoiceDAO.update(newInvoice);
        System.out.println("Update Result: " + updateResult);

        // 3. Test Select By ID
        Invoice selectedInvoice = invoiceDAO.selectById(newInvoice.getInvoiceId());
        System.out.println("Selected Invoice: " + selectedInvoice);

        // 4. Test Select All
        List<Invoice> invoices = invoiceDAO.selectAll();
        System.out.println("List of Invoices: ");
        for (Invoice invoice : invoices) {
            System.out.println(invoice);
        }

        // 5. Test Delete
        int deleteResult = invoiceDAO.delete(newInvoice);
        System.out.println("Delete Result: " + deleteResult);
    }
}
