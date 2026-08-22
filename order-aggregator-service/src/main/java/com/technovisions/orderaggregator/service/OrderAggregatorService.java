package com.technovisions.orderaggregator.service;

import com.technovisions.orderaggregator.client.SystemServiceClient;
import com.technovisions.orderaggregator.dto.CreateOrderRequest;
import com.technovisions.orderaggregator.dto.OrderResponse;
import com.technovisions.orderaggregator.dto.UpdateOrderStatusRequest;
import com.technovisions.orderaggregator.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAggregatorService {

    private final SystemServiceClient systemServiceClient;

    public OrderResponse createOrder(CreateOrderRequest request) {
        OrderResponse response = systemServiceClient.createOrder(request);
        log.info("Order created downstream with id={}", response.getId());
        return response;
    }

    public List<OrderResponse> getCustomerOrders(Long customerId) {
        return systemServiceClient.getCustomerOrders(customerId);
    }

    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        // Validate against our local enum copy before forwarding, so an illegal
        // status never reaches the system service in the first place.
        OrderStatus parsed = OrderStatus.parse(request.getStatus());
        OrderResponse response = systemServiceClient.updateOrderStatus(orderId,
                new UpdateOrderStatusRequest(parsed.name()));
        log.info("Order id={} status updated downstream to {}", orderId, response.getStatus());
        return response;
    }
}
