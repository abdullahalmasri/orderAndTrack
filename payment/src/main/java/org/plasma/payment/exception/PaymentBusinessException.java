package org.plasma.payment.exception;

import org.plasma.common.exception.BusinessException;

public class PaymentBusinessException extends BusinessException {
    public PaymentBusinessException(String message) {
        super(message);
    }
}
