package model;

import enums.PaymentStatusEnum;

public class PaymentStatus {
    private int paymentStatusID;
    private PaymentStatusEnum status;

    public PaymentStatus() {}

    public PaymentStatus(int paymentStatusID, PaymentStatusEnum status) {
        this.paymentStatusID = paymentStatusID;
        this.status = status;
    }

    public int getPaymentStatusID() {
        return paymentStatusID;
    }

    public void setPaymentStatusID(int paymentStatusID) {
        this.paymentStatusID = paymentStatusID;
    }

    public PaymentStatusEnum getStatus() {
        return status;
    }

    public void setStatus(PaymentStatusEnum status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "PaymentStatus: ID: " + paymentStatusID +
               "\n\t\t  Name: " + status.getName() +
               "\n\t\t  Code: " + status.getCode();
    }
}
