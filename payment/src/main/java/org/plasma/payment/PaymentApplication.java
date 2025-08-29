package org.plasma.payment;

import org.plasma.common.entities.payment.Payment;
import org.plasma.payment.service.PaymentService;
import org.plasma.payment.service.PaymentServiceImp;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.UUID;

@SpringBootApplication(scanBasePackages = {"org.plasma.payment", "org.plasma.common"})
@EntityScan(basePackages = "org.plasma.common.entities.payment")
public class PaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }

    @Bean
    CommandLineRunner initPayment(PaymentService paymentService) {

        return args -> {
            try {
                // Sample payment data
                UUID orderId = UUID.fromString("12948def-f20a-498b-abee-80975796f9b1");
//                BigDecimal amount = new BigDecimal("50.00");
                Payment.PaymentMethod method = Payment.PaymentMethod.VISA;
                String cardHolderName = "John Doe";
                String cardNumber = "4111111111111111";
                String cvv = "123";
                String expiryDate = "12/27";

                System.out.println("Initiating test payment...");
                paymentService.processPayment(orderId, method, cardHolderName, cardNumber, cvv, expiryDate);
                System.out.println("Test payment processed successfully");
            } catch (Exception e) {
                System.err.println("Test payment failed: " + e.getMessage());
            }
        };
    }
}