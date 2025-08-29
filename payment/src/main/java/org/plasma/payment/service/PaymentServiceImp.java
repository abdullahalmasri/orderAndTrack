package org.plasma.payment.service;

import org.plasma.common.entities.payment.Payment;
import org.plasma.payment.dao.PaymentRepository;
import org.plasma.payment.grpc.OrderServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentServiceImp implements PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OrderServiceClient orderClient;
    @Autowired
    private PaymentMetrics metrics;
    @Autowired
    private PaymentSimulator paymentSimulator;

    @Override
    public void processPayment(UUID orderId,  Payment.PaymentMethod method,
                               String cardHolderName, String cardNumber, String cvv, String expiryDate) throws Exception {
//        Payment payment = new Payment(orderId, BigDecimal.valueOf(), method, Payment.PaymentStatus.PENDING,
//                cardHolderName, cardNumber, cvv, expiryDate);
        PaymentSimulator.ValidationResult validation = paymentSimulator.
                validateCard(cardHolderName, cardNumber, cvv, expiryDate);
        metrics.recordValidation(validation.isValid());
        if (!validation.isValid()) {
            throw new Exception("Card validation failed: " + validation.getMessage());
        }

        // gRPC call must succeed
        orderClient.createPayment(orderId, method, Payment.PaymentStatus.PENDING,
                cardHolderName, cardNumber, cvv, expiryDate);


//        metrics.recordPayment(method.name(), savedPayment.getStatus().name());
    }
}

