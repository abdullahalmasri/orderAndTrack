package org.plasma.payment.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PaymentSimulator {
    public ValidationResult validateCard(String cardHolderName, String cardNumber, String cvv, String expiryDate) {
        // Mock validation logic
        if (cardNumber == null || !cardNumber.matches("\\d{16}")) {
            return new ValidationResult(false, "Invalid card number");
        }
        if (cvv == null || !cvv.matches("\\d{3}")) {
            return new ValidationResult(false, "Invalid CVV");
        }
        if (expiryDate == null || !expiryDate.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            return new ValidationResult(false, "Invalid expiry date");
        }
        // Simulate funds check (e.g., assume card has $1000)
        BigDecimal availableFunds = new BigDecimal("1000.00");
//        if (amount.compareTo(availableFunds) > 0) {
//            return new ValidationResult(false, "Insufficient funds");
//        }
        return new ValidationResult(true, "Card validated");
    }

    public static class ValidationResult {
        private final boolean isValid;
        private final String message;

        public ValidationResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }

        public boolean isValid() {
            return isValid;
        }

        public String getMessage() {
            return message;
        }
    }
}