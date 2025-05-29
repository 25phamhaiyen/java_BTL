package service;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import model.Invoice;
import repository.InvoiceRepository;
import utils.PaymentLogger;

public class PaymentService {
    private final InvoiceRepository invoiceRepository;
    private final QRPaymentService qrPaymentService;
    private final ScheduledExecutorService scheduler;

    public PaymentService() {
        this.invoiceRepository = InvoiceRepository.getInstance();
        this.qrPaymentService = QRPaymentService.getInstance();
        this.scheduler = Executors.newScheduledThreadPool(1);
        startPendingTransactionChecker();
    }

    private void startPendingTransactionChecker() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<Invoice> pendingInvoices = invoiceRepository.getPendingQRInvoices();
                PaymentLogger.info("Found " + pendingInvoices.size() + " pending QR invoices");
                for (Invoice invoice : pendingInvoices) {
                    QRPaymentService.PaymentStatusResult statusResult = qrPaymentService.checkPaymentStatus(invoice.getTransactionId());
                    if (statusResult.isCompleted()) {
                        invoice.setStatus(enums.StatusEnum.COMPLETED);
                        invoice.setAmountPaid(invoice.getTotal());
                        invoiceRepository.update(invoice);
                        PaymentLogger.info("Invoice #" + invoice.getInvoiceId() + " marked as COMPLETED");
                    } else if (statusResult.isFailed() || statusResult.isError()) {
                        invoice.setStatus(enums.StatusEnum.FAILED);
                        invoiceRepository.update(invoice);
                        PaymentLogger.info("Invoice #" + invoice.getInvoiceId() + " marked as FAILED");
                    }
                }
            } catch (Exception e) {
                PaymentLogger.error("Error checking pending QR invoices: " + e.getMessage(), e);
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    public void shutdown() {
        scheduler.shutdown();
        PaymentLogger.info("PaymentService shutdown");
    }
}