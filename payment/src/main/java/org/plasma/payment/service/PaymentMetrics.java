package org.plasma.payment.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.springframework.stereotype.Component;

@Component
public class PaymentMetrics {
    private final Counter paymentCounter;
    private final Counter validationCounter;

    public PaymentMetrics(MeterRegistry registry) {
        paymentCounter = Counter.builder("payments_total")
                .tag("method", "unknown")
                .tag("status", "unknown")
                .register(registry);
        validationCounter = Counter.builder("payment_validations_total")
                .tag("result", "unknown")
                .register(registry);
    }

    public void recordPayment(String method, String status) {
        paymentCounter
//                .builder("payments_total")
//                .tag("method", method)
//                .tag("status", status)
                .increment();
    }

    public void recordValidation(boolean isValid) {
        validationCounter
//                .builder("payment_validations_total")
//                .tag("result", isValid ? "success" : "failure")
                .increment();
    }
}