# OrderAndTrack

OrderAndTrack is a microservices-based application for managing and tracking orders in an e-commerce system. It demonstrates a distributed system architecture using Spring Boot, gRPC for inter-service communication, and the Saga pattern for managing distributed transactions. The system includes services for handling orders, payments, and tracking, ensuring data consistency across services without relying on traditional two-phase commit (2PC) mechanisms.

## Table of Contents
- [Architecture](#architecture)
- [Saga Pattern Implementation](#saga-pattern-implementation)
- [Prerequisites](#prerequisites)
- [Setup and Installation](#setup-and-installation)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

## Architecture
The OrderAndTrack system is composed of multiple microservices, each responsible for a specific domain:

- **Order Service**: Manages order creation, updates, and queries. It initiates the Saga for order processing.
- **Payment Service**: Handles payment processing and validation, participating in the Saga to ensure payments are authorized or rolled back.
- **Tracking Service** (optional): Tracks order status and delivery updates.

The services communicate using gRPC for high-performance, strongly-typed RPC calls. A message broker (e.g., Kafka or RabbitMQ) can be used for event-driven Saga coordination. The Saga pattern ensures data consistency across services by coordinating a sequence of local transactions with compensating actions for failures.

## Saga Pattern Implementation
The Saga pattern is used to manage distributed transactions across microservices, ensuring eventual consistency without locking resources. In OrderAndTrack, the Saga pattern is implemented using the **orchestration approach**, where a central **Saga Orchestrator** coordinates the transaction steps.

### Why Saga?
Traditional ACID transactions (via 2PC) are not feasible in a microservices architecture with separate databases per service. The Saga pattern breaks down a transaction into a series of local transactions, each executed by a single service, with compensating transactions to undo changes in case of failure.

### How It Works in OrderAndTrack
Consider an order creation scenario:
1. **Order Service**: Creates an order and initiates the Saga.
2. **Payment Service**: Processes the payment for the order.
3. If any step fails (e.g., payment validation fails), the Saga Orchestrator triggers compensating transactions to roll back previous steps (e.g., cancel the order).

#### Saga Orchestration
- A **Saga Orchestrator** (a dedicated service or component) manages the transaction flow.
- Each service performs a local transaction and notifies the orchestrator of success or failure.
- The orchestrator sends commands to the next service or triggers compensating actions if a failure occurs.
- Example flow:
  - Order Service creates an order (T1) and notifies the orchestrator.
  - Orchestrator commands Payment Service to process payment (T2).
  - If payment fails, the orchestrator commands Order Service to cancel the order (C1).

#### Implementation Details
- **Technology**: Spring Boot for service implementation, gRPC for inter-service communication, and Kafka (optional) for event publishing.
- **Compensating Transactions**: Each service implements idempotent compensating actions (e.g., `cancelOrder`, `refundPayment`).
- **Saga Log**: A persistent log (e.g., in a database or Kafka) tracks the Saga’s state to ensure fault tolerance.

For a detailed code example, see the [Saga Implementation](#saga-implementation-example) section below.

## Prerequisites
- Java 17 or higher
- Maven 3.8.x
- Docker (optional, for containerized deployment)
- Kafka or RabbitMQ (optional, for event-driven Sagas)
- gRPC tools (`protoc` compiler)
- IDE (e.g., IntelliJ IDEA, Eclipse)
- MySQL or PostgreSQL (for service databases)

## Setup and Installation
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/abdullahalmasri/orderAndTrack.git
   cd orderAndTrack
   ```

2. **Install Dependencies**:
   Run the following command to install Maven dependencies:
   ```bash
   mvn clean install
   ```

3. **Set Up Databases**:
   - Create databases for each service (e.g., `order_db`, `payment_db`).
   - Update `application.properties` or `application.yml` in each service with database credentials:
     ```properties
     spring.datasource.url=jdbc:mysql://localhost:3306/order_db
     spring.datasource.username=root
     spring.datasource.password=your_password
     ```

4. **Configure gRPC**:
   - Ensure the `order` service is running on `localhost:9091` or update the `PaymentServiceClient` configuration:
     ```properties
     order.service.host=localhost
     ```

5. **(Optional) Set Up Kafka**:
   - Install and start Kafka if using event-driven Sagas:
     ```bash
     docker-compose up -d kafka
     ```

## Running the Application
1. **Start the Order Service**:
   ```bash
   cd order-service
   mvn spring-boot:run
   ```

2. **Start the Payment Service**:
   ```bash
   cd payment-service
   mvn spring-boot:run
   ```

3. **Start the Saga Orchestrator** (if separate):
   ```bash
   cd saga-orchestrator
   mvn spring-boot:run
   ```

4. **Verify Services**:
   - Order Service: `http://localhost:8080/health`
   - Payment Service: `http://localhost:8081/health`
   - gRPC Order Service: Ensure it’s accessible at `localhost:9091`.

## Testing
1. **Unit Tests**:
   Run unit tests for each service:
   ```bash
   mvn test
   ```

2. **Integration Tests**:
   - Use Postman or cURL to test the order creation endpoint:
     ```bash
     curl -X POST http://localhost:8080/api/orders \
     -H "Content-Type: application/json" \
     -d '{"nameOfClient":"Alice Smith","nameOfProvider":"Acme Corp","description":"Laptop purchase","price":999.99}'
     ```
   - Verify that the Saga completes successfully or rolls back on failure.

3. **gRPC Testing**:
   Use a gRPC client (e.g., BloomRPC or gRPCurl) to test the `OrderService` gRPC endpoints.

## Saga Implementation Example
Below is a sample implementation of the Saga Orchestrator for the order creation process using Spring Boot and gRPC.

```java
package org.plasma.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import org.plasma.common.grpc.proto.CommonServicesGrpc;
import org.plasma.common.grpc.proto.Data;
import org.plasma.common.grpc.proto.ResponseCreated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SagaOrchestrator {
    private final CommonServicesGrpc.CommonServicesStub orderServiceStub;
    private final CommonServicesGrpc.CommonServicesStub paymentServiceStub;
    private final ObjectMapper objectMapper;

    @Autowired
    public SagaOrchestrator(CommonServicesGrpc.CommonServicesStub orderServiceStub,
                           CommonServicesGrpc.CommonServicesStub paymentServiceStub,
                           ObjectMapper objectMapper) {
        this.orderServiceStub = orderServiceStub;
        this.paymentServiceStub = paymentServiceStub;
        this.objectMapper = objectMapper;
    }

    public void executeOrderSaga(OrderDTO orderDTO) throws Exception {
        // Step 1: Create Order
        String orderJson = objectMapper.writeValueAsString(orderDTO);
        CountDownLatch orderLatch = new CountDownLatch(1);
        final String[] orderId = {null};

        StreamObserver<ResponseCreated> orderResponseObserver = new StreamObserver<>() {
            @Override
            public void onNext(ResponseCreated response) {
                if ("SUCCESS".equals(response.getStatus())) {
                    orderId[0] = response.getDataAsJson(); // Assuming orderId is returned
                } else {
                    throw new RuntimeException("Order creation failed: " + response.getErrorMessage());
                }
            }

            @Override
            public void onError(Throwable t) {
                orderLatch.countDown();
                throw new RuntimeException("Order service error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                orderLatch.countDown();
            }
        };

        orderServiceStub.dataCreated(Data.newBuilder()
                .setService("order")
                .setDataAsJson(orderJson)
                .build(), orderResponseObserver);

        if (!orderLatch.await(10, TimeUnit.SECONDS)) {
            throw new Exception("Order creation timed out");
        }

        // Step 2: Process Payment
        PaymentDTO paymentDTO = new PaymentDTO(orderId[0], orderDTO.getPrice(), "CREDIT_CARD", "AUTHORIZED");
        String paymentJson = objectMapper.writeValueAsString(paymentDTO);
        CountDownLatch paymentLatch = new CountDownLatch(1);

        StreamObserver<ResponseCreated> paymentResponseObserver = new StreamObserver<>() {
            @Override
            public void onNext(ResponseCreated response) {
                if (!"SUCCESS".equals(response.getStatus())) {
                    // Trigger compensating transaction
                    cancelOrder(orderId[0]);
                    throw new RuntimeException("Payment processing failed: " + response.getErrorMessage());
                }
            }

            @Override
            public void onError(Throwable t) {
                paymentLatch.countDown();
                cancelOrder(orderId[0]);
                throw new RuntimeException("Payment service error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                paymentLatch.countDown();
            }
        };

        paymentServiceStub.dataCreated(Data.newBuilder()
                .setService("payment")
                .setDataAsJson(paymentJson)
                .build(), paymentResponseObserver);

        if (!paymentLatch.await(10, TimeUnit.SECONDS)) {
            cancelOrder(orderId[0]);
            throw new Exception("Payment processing timed out");
        }
    }

    private void cancelOrder(String orderId) {
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<ResponseCreated> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(ResponseCreated response) {
                System.out.println("Order cancelled: " + response.getStatus());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Failed to cancel order: " + t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        orderServiceStub.dataCreated(Data.newBuilder()
                .setService("order")
                .setDataAsJson("{\"orderId\":\"" + orderId + "\",\"action\":\"cancel\"}")
                .build(), responseObserver);

        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted during order cancellation");
        }
    }
}
```

### Key Features of the Saga Implementation
- **Orchestration**: The `SagaOrchestrator` coordinates the order creation and payment processing steps.
- **Compensating Transactions**: If the payment fails, the `cancelOrder` method is called to roll back the order.
- **gRPC Communication**: Uses gRPC stubs to communicate with the `order` and `payment` services.
- **Timeout Handling**: Uses `CountDownLatch` to handle timeouts for each step.
- **Idempotency**: Compensating transactions (e.g., `cancelOrder`) are designed to be idempotent to handle retries safely.

## Contributing
Contributions are welcome! Please follow these steps:
1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/your-feature`).
3. Commit your changes (`git commit -m "Add your feature"`).
4. Push to the branch (`git push origin feature/your-feature`).
5. Open a pull request.

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.