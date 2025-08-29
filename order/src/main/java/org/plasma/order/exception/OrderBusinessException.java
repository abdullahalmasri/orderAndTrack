package org.plasma.order.exception;

import org.plasma.common.exception.BusinessException;

public class OrderBusinessException extends BusinessException {
    public OrderBusinessException(String message) {
        super(message);
    }
}
