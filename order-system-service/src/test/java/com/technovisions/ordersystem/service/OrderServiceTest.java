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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, customerRepository);
    }

    private Order sampleOrder(Long id, Long customerId, OrderStatus status) {
        return Order.builder()
                .id(id)
                .customerId(customerId)
                .productName("Widget")
                .quantity(2)
                .price(new BigDecimal("9.99"))
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createOrder_persistsAsCreatedStatus_whenCustomerExists() {
        CreateOrderRequest request = new CreateOrderRequest(1L, "Widget", 2, new BigDecimal("9.99"));
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(10L);
            return order;
        });

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(response.getCustomerId()).isEqualTo(1L);
    }

    @Test
    void createOrder_throwsCustomerNotFound_whenCustomerMissing() {
        CreateOrderRequest request = new CreateOrderRequest(404L, "Widget", 1, BigDecimal.TEN);
        when(customerRepository.existsById(404L)).thenReturn(false);

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("404");
    }

    @Test
    void getOrdersForCustomer_returnsOrders_whenCustomerHasOrders() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.findByCustomerId(1L)).thenReturn(
                List.of(sampleOrder(1L, 1L, OrderStatus.CREATED), sampleOrder(2L, 1L, OrderStatus.PREPARING)));

        List<OrderResponse> responses = orderService.getOrdersForCustomer(1L);

        assertThat(responses).hasSize(2);
    }

    @Test
    void getOrdersForCustomer_returnsEmptyList_whenCustomerHasNoOrders() {
        when(customerRepository.existsById(2L)).thenReturn(true);
        when(orderRepository.findByCustomerId(2L)).thenReturn(List.of());

        List<OrderResponse> responses = orderService.getOrdersForCustomer(2L);

        assertThat(responses).isEmpty();
    }

    @Test
    void getOrdersForCustomer_throwsCustomerNotFound_whenCustomerMissing() {
        when(customerRepository.existsById(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> orderService.getOrdersForCustomer(99L))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void updateOrderStatus_updatesStatus_whenOrderExistsAndStatusIsValid() {
        Order existing = sampleOrder(5L, 1L, OrderStatus.CREATED);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.updateOrderStatus(5L, "PREPARING");

        assertThat(response.getStatus()).isEqualTo(OrderStatus.PREPARING);
    }

    @Test
    void updateOrderStatus_throwsOrderNotFound_whenOrderMissing() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrderStatus(999L, "PREPARING"))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void updateOrderStatus_throwsInvalidOrderStatus_whenStatusIsIllegal() {
        Order existing = sampleOrder(5L, 1L, OrderStatus.CREATED);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> orderService.updateOrderStatus(5L, "SHIPPED"))
                .isInstanceOf(InvalidOrderStatusException.class);
    }
}
