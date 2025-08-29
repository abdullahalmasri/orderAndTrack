package org.plasma.order.grpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.plasma.common.dtos.OrderDTO;
import org.plasma.common.entities.order.Order;
import org.plasma.order.dao.OrderRepository;
import org.plasma.common.grpc.proto.CommonServicesGrpc;
import org.plasma.common.grpc.proto.Data;
import org.plasma.common.grpc.proto.ReadData;
import org.plasma.common.grpc.proto.ResponseCreated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@GrpcService
public class CommonServicesImpl extends CommonServicesGrpc.CommonServicesImplBase {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public StreamObserver<Data> dataCreated(StreamObserver<ResponseCreated> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(Data data) {
                try {
                    String json = data.getDataAsJson();
                    ResponseCreated response;
                    if ("order".equals(data.getService())) {
                        Order order = objectMapper.readValue(json, Order.class);
                        orderRepository.save(order);
                        response = ResponseCreated.newBuilder()
                                .setStatus("SUCCESS")
                                .build();
                    } else if ("payment".equals(data.getService())) {
                        // Extract orderId from JSON
                        UUID orderId = UUID.fromString(objectMapper.readTree(json).get("orderId").asText());
                        if (orderRepository.existsById(orderId)) {
                            Order byId = orderRepository.findById(orderId).orElse(null);
//                            if (byId.isPresent()) {
                                //here should get order to use it in payment
                                response = ResponseCreated.newBuilder()
                                        .setStatus("SUCCESS")
                                        .setErrorMessage(byId.toString())
                                        .build();
//                            }else {
//                                response = ResponseCreated.newBuilder()
//                                        .setStatus("SUCCESS")
//                                        .setErrorMessage("Order not found")
//                                        .build();
//                            }

                        } else {
                            response = ResponseCreated.newBuilder()
                                    .setStatus("FAILED")
                                    .setErrorMessage("Invalid orderId: " + orderId)
                                    .build();
                        }
                    } else {
                        response = ResponseCreated.newBuilder()
                                .setStatus("FAILED")
                                .setErrorMessage("Unsupported service: " + data.getService())
                                .build();
                    }
                    responseObserver.onNext(response);
                } catch (Exception e) {
                    responseObserver.onNext(ResponseCreated.newBuilder()
                            .setStatus("FAILED")
                            .setErrorMessage(e.getMessage())
                            .build());
                }
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<Data> dataRequest(StreamObserver<ReadData> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(Data data) {
                try {
                    String name = data.getDataAsJson(); // e.g., order ID
                    OrderDTO order = objectMapper.convertValue(orderRepository.findById(UUID.fromString(name))
                            .orElse(new Order()),OrderDTO.class);
                    if (order != null) {
                        String json = objectMapper.writeValueAsString(order);
                        ReadData response = ReadData.newBuilder()
                                .setName(data.getService())
                                .setDataAsJson(json)
                                .build();
                        responseObserver.onNext(response);
                    }
                } catch (Exception e) {
                    responseObserver.onError(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}