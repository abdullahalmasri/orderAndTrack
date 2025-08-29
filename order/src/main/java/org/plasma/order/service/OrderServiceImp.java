package org.plasma.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.plasma.common.entities.order.Order;
import org.plasma.order.dao.OrderRepository;
import org.plasma.order.grpc.OrderServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
@Service
public class OrderServiceImp implements OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderServiceClient orderClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderMetrics metrics;

    @Override
    public void createOrder(String nameOfClient, String nameOfProvider, String description, BigDecimal price) throws Exception {
        // Create Order entity
        Order order = new Order(nameOfClient, nameOfProvider, description, price);

        // Convert to JSON for gRPC
        String json = objectMapper.writeValueAsString(order);

        // Call gRPC client
        orderClient.createOrder(json);

//        // Save to database
//        orderRepository.save(order);

        // Record metrics
        metrics.recordOrder("CREATED");
    }
}
