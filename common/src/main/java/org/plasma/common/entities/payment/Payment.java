package org.plasma.common.entities.payment;

//public class Payment {
//}

import jakarta.persistence.*;
import lombok.Setter;
import org.plasma.common.utils.GeneratedIdCreatedAt;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment extends GeneratedIdCreatedAt implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "order_id", nullable = false)
    private final UUID orderId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private final BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private final PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private final PaymentStatus status;

    @Transient
    private final String cardHolderName;

    @Transient
    private final String cardNumber;

    @Transient
    private final String cvv;

    @Transient
    private final String expiryDate;

    public enum PaymentMethod {
        VISA, MASTERCARD, PAYPAL, CASH_ON_DELIVERY
    }

    public enum PaymentStatus {
        PENDING, AUTHORIZED, COMPLETED, FAILED, REFUNDED
    }

    public Payment(UUID orderId, BigDecimal amount, PaymentMethod paymentMethod, PaymentStatus status,
                   String cardHolderName, String cardNumber, String cvv, String expiryDate) {
        super();
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.cardHolderName = cardHolderName;
        this.cardNumber = cardNumber;
        this.cvv = cvv;
        this.expiryDate = expiryDate;
    }

    public Payment() {
        super();
        this.orderId = null;
        this.amount = BigDecimal.ZERO;
        this.paymentMethod = null;
        this.status = null;
        this.cardHolderName = null;
        this.cardNumber = null;
        this.cvv = null;
        this.expiryDate = null;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCvv() {
        return cvv;
    }

    public String getExpiryDate() {
        return expiryDate;
    }
}