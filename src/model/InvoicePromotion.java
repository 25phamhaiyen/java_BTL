package model;

public class InvoicePromotion {
    private Invoice invoice;
    private Promotion promotion;
    private double discountApplied;

    // Constructor
    public InvoicePromotion(Invoice invoice, Promotion promotion, double discountApplied) {
        this.invoice = invoice;
        this.promotion = promotion;
        this.discountApplied = discountApplied;
    }

    // Getters and Setters
    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public void setPromotion(Promotion promotion) {
        this.promotion = promotion;
    }

    public double getDiscountApplied() {
        return discountApplied;
    }

    public void setDiscountApplied(double discountApplied) {
        this.discountApplied = discountApplied;
    }
}
