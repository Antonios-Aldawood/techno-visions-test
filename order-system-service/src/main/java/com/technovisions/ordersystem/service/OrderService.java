package com.technovisions.ordersystem.service;

import com.technovisions.ordersystem.dto.CreateOrderRequest;
import com.technovisions.ordersystem.dto.OrderResponse;
import com.technovisions.ordersystem.entity.Order;
import com.technovisions.ordersystem.entity.OrderStatus;
import com.technovisions.ordersystem.exception.CustomerNotFoundException;
import com.technovisions.ordersystem.exception.InvalidOrderStatusException;
import com.technovisions.ordersystem.exception.OrderNotFoundException;
import com.technovisions.ordersystem.repository.CustomerRepository;
import com.technovisions.ordersystem.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        if (!customerRepository.existsById(request.getCustomerId())) {
            throw new CustomerNotFoundException(request.getCustomerId());
        }

        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .productName(request.getProductName())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        Order saved = orderRepository.save(order);
        log.info("Created order id={} for customerId={}", saved.getId(), saved.getCustomerId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForCustomer(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }

        return orderRepository.findByCustomerId(customerId).stream()
                .map(OrderService::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String rawStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderStatus newStatus = parseStatus(rawStatus);
        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        log.info("Updated order id={} status to {}", saved.getId(), saved.getStatus());
        return toResponse(saved);
    }

    private static OrderStatus parseStatus(String rawStatus) {
        try {
            return OrderStatus.valueOf(rawStatus.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidOrderStatusException(rawStatus);
        }
    }

    public static OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .productName(order.getProductName())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
