package com.technovisions.orderaggregator.controller;

import com.technovisions.orderaggregator.dto.ApiResponse;
import com.technovisions.orderaggregator.dto.CreateOrderRequest;
import com.technovisions.orderaggregator.dto.OrderResponse;
import com.technovisions.orderaggregator.dto.UpdateOrderStatusRequest;
import com.technovisions.orderaggregator.service.OrderAggregatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderAggregatorService orderAggregatorService;

    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Incoming request: POST /api/v1/orders customerId={}", request.getCustomerId());
        OrderResponse response = orderAggregatorService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/customers/{customerId}/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getCustomerOrders(@PathVariable Long customerId) {
        log.info("Incoming request: GET /api/v1/customers/{}/orders", customerId);
        List<OrderResponse> orders = orderAggregatorService.getCustomerOrders(customerId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId, @Valid @RequestBody UpdateOrderStatusRequest request) {
        log.info("Incoming request: PUT /api/v1/orders/{}/status newStatus={}", orderId, request.getStatus());
        OrderResponse response = orderAggregatorService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
