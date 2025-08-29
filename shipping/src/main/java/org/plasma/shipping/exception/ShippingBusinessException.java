package org.plasma.shipping.exception;

import org.plasma.common.exception.BusinessException;

public class ShippingBusinessException extends BusinessException {
    public ShippingBusinessException(String message) {
        super(message);
    }
}
