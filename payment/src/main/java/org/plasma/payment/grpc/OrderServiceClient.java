package org.plasma.payment.grpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.plasma.common.dtos.OrderDTO;
import org.plasma.common.entities.payment.Payment;
import org.plasma.common.grpc.proto.CommonServicesGrpc;
import org.plasma.common.grpc.proto.Data;
import org.plasma.common.grpc.proto.ReadData;
import org.plasma.common.grpc.proto.ResponseCreated;
import org.plasma.payment.dao.PaymentRepository;
import org.plasma.payment.service.PaymentMetrics;
import org.plasma.payment.service.PaymentSimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

@Component
public class OrderServiceClient {
    private final CommonServicesGrpc.CommonServicesStub asyncStub;
    private final ObjectMapper objectMapper;
    private final PaymentSimulator paymentSimulator;
    private final PaymentRepository paymentRepository;
    @Autowired
    private PaymentMetrics metrics;
    @Autowired
    public OrderServiceClient(ObjectMapper objectMapper, PaymentSimulator paymentSimulator, PaymentRepository paymentRepository) {
        this.objectMapper = objectMapper;
        this.paymentSimulator = paymentSimulator;
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9091)
                .usePlaintext()
                .build();
        asyncStub = CommonServicesGrpc.newStub(channel);
        this.paymentRepository = paymentRepository;
    }

    public void createPayment(UUID orderId, Payment.PaymentMethod method, Payment.PaymentStatus status,
                              String cardHolderName, String cardNumber, String cvv, String expiryDate) throws Exception {
//        PaymentSimulator.ValidationResult validation = paymentSimulator.
//                validateCard(cardHolderName, cardNumber, cvv, expiryDate);
//        if (!validation.isValid()) {
//            throw new Exception("Card validation failed: " + validation.getMessage());
//        }

        int maxRetries = 3;
        int retryDelaySeconds = 5;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            CountDownLatch latch = new CountDownLatch(1);
            try {
                StreamObserver<ResponseCreated> responseObserver = new StreamObserver<>() {
                    @Override
                    public void onNext(ResponseCreated response) {
                        System.out.println("Created: " + response.getStatus() + ", Error: " + response.getErrorMessage());
//                        if (!"SUCCESS".equals(response.getStatus())) {
                            // Save only if gRPC succeeds
                            OrderDTO orderDTO = null;
                            try {
                                JsonObject jsonObject = JsonParser.
                                        parseString(response.getErrorMessage()
                                                .replace("Order","").trim()).getAsJsonObject();
                                orderDTO = objectMapper.readValue(jsonObject.toString(),OrderDTO.class)
                                        ;
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                            Payment savedPayment = paymentRepository.save(new Payment(orderId, orderDTO.getPrice(),
                                    method, Payment.PaymentStatus.AUTHORIZED,
                                    null, null, null, null));
                            paymentRepository.save(savedPayment);
                            metrics.recordPayment(method.name(), savedPayment.getStatus().name());

                            System.out.println("gRPC success: " + response.getErrorMessage());
//                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.err.println("Error: " + t.getMessage());
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        latch.countDown();
                    }
                };

                StreamObserver<Data> requestObserver = asyncStub.dataCreated(responseObserver);
                Payment payment = new Payment(orderId, BigDecimal.ZERO, method, status, cardHolderName, cardNumber, cvv, expiryDate);
                String json = objectMapper.writeValueAsString(payment);
                Data data = Data.newBuilder()
                        .setService("payment")
                        .setDataAsJson(json)
                        .build();
                requestObserver.onNext(data);
                requestObserver.onCompleted();
//                if (!latch.await(10, TimeUnit.MINUTES)) {
//                    throw new Exception("gRPC call timed out");
//                }
                return; // Success
            } catch (Exception e) {
                lastException = e;
                System.err.println("Attempt " + attempt + " failed: " + e.getMessage());
                if (attempt < maxRetries) {
                    Thread.sleep(retryDelaySeconds * 1000);
                }
            } finally {
                latch.countDown();
            }
        }
        throw new Exception("Failed to create payment after " + maxRetries + " attempts", lastException);
    }

    public void requestOrder(String orderId) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<ReadData> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(ReadData response) {
                System.out.println("Received: " + response.getName() + ", Data: " + response.getDataAsJson());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error: " + t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        StreamObserver<Data> requestObserver = asyncStub.dataRequest(responseObserver);
        Data data = Data.newBuilder()
                .setService("order")
                .setDataAsJson(orderId)
                .build();
        requestObserver.onNext(data);
        requestObserver.onCompleted();
        latch.await();
    }
}