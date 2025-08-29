package org.plasma.order.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.springframework.stereotype.Component;

@Component
public class OrderMetrics {
    private final Counter orderCounter;

    public OrderMetrics(MeterRegistry registry) {
        orderCounter = Counter.builder("orders_total")
                .tag("status", "unknown")
                .register(registry);
    }

    public void recordOrder(String status) {
        orderCounter
//                .builder("orders_total")
//                .tag("status", status)
                .increment();
    }
}