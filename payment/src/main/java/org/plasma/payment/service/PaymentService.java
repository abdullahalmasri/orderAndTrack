package org.plasma.payment.service;

import org.plasma.common.entities.payment.Payment;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {

    void processPayment(UUID orderId, Payment.PaymentMethod method,
                        String cardHolderName, String cardNumber, String cvv, String expiryDate) throws Exception;
}
