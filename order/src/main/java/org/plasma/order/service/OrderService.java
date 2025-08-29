package org.plasma.order.service;

import java.math.BigDecimal;

public interface OrderService {
    void createOrder(String nameOfClient, String nameOfProvider, String description, BigDecimal price) throws Exception;
}
