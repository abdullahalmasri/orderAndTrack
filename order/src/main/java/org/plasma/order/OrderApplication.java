package org.plasma.order;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.plasma.order.grpc.CommonServicesImpl;
import org.plasma.order.service.OrderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.math.BigDecimal;

@SpringBootApplication(scanBasePackages = {"org.plasma.order", "org.plasma.common"})
@EntityScan(basePackages = "org.plasma.common.entities.order")
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

    @Bean
    CommandLineRunner initOrder(OrderService orderService) { // Inject OrderService
        return args -> {
            try {
                // Sample order data
                String nameOfClient = "Alice Smith";
                String nameOfProvider = "Acme Corp";
                String description = "Laptop purchase";
                BigDecimal price = new BigDecimal("999.99");

                System.out.println("Initiating test order...");
                orderService.createOrder(nameOfClient, nameOfProvider, description, price);
                System.out.println("Test order processed successfully");
            } catch (Exception e) {
                System.err.println("Test order failed: " + e.getMessage());
                e.printStackTrace(); // Detailed stack trace for debugging
            }
        };
    }
}