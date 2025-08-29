package org.plasma.order.grpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.plasma.common.grpc.proto.CommonServicesGrpc;
import org.plasma.common.grpc.proto.Data;
import org.plasma.common.grpc.proto.ResponseCreated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
public class OrderServiceClient {
    private final CommonServicesGrpc.CommonServicesStub asyncStub;
    private final ObjectMapper objectMapper;

    @Autowired
    public OrderServiceClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9091)
                .usePlaintext()
                .build();
        asyncStub = CommonServicesGrpc.newStub(channel);
    }

    public void createOrder(String orderJson) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<ResponseCreated> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(ResponseCreated response) {
                System.out.println("Order created: " + response.getStatus());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Order creation error: " + t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        StreamObserver<Data> requestObserver = asyncStub.dataCreated(responseObserver);
        Data data = Data.newBuilder()
                .setService("order")
                .setDataAsJson(orderJson)
                .build();
        requestObserver.onNext(data);
        requestObserver.onCompleted();
        latch.await();
    }
}