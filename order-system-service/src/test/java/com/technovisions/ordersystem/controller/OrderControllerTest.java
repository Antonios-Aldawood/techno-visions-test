package com.technovisions.ordersystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.technovisions.ordersystem.dto.CreateOrderRequest;
import com.technovisions.ordersystem.dto.OrderResponse;
import com.technovisions.ordersystem.dto.UpdateOrderStatusRequest;
import com.technovisions.ordersystem.entity.OrderStatus;
import com.technovisions.ordersystem.exception.CustomerNotFoundException;
import com.technovisions.ordersystem.exception.InvalidOrderStatusException;
import com.technovisions.ordersystem.exception.OrderNotFoundException;
import com.technovisions.ordersystem.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private OrderResponse sampleResponse(Long id, OrderStatus status) {
        return OrderResponse.builder()
                .id(id).customerId(1L).productName("Widget").quantity(2)
                .price(new BigDecimal("9.99")).status(status).createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createOrder_returns201_whenRequestIsValid() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(1L, "Widget", 2, new BigDecimal("9.99"));
        when(orderService.createOrder(any())).thenReturn(sampleResponse(10L, OrderStatus.CREATED));

        mockMvc.perform(post("/internal/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void createOrder_returns404_whenCustomerMissing() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(404L, "Widget", 2, new BigDecimal("9.99"));
        when(orderService.createOrder(any())).thenThrow(new CustomerNotFoundException(404L));

        mockMvc.perform(post("/internal/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createOrder_returns400_whenQuantityIsZero() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(1L, "Widget", 0, new BigDecimal("9.99"));

        mockMvc.perform(post("/internal/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_returns400_whenPriceIsNegative() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(1L, "Widget", 1, new BigDecimal("-1.00"));

        mockMvc.perform(post("/internal/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCustomerOrders_returns200WithList_whenCustomerHasOrders() throws Exception {
        when(orderService.getOrdersForCustomer(1L)).thenReturn(
                List.of(sampleResponse(1L, OrderStatus.CREATED)));

        mockMvc.perform(get("/internal/customers/1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getCustomerOrders_returns200WithEmptyList_whenCustomerHasNoOrders() throws Exception {
        when(orderService.getOrdersForCustomer(2L)).thenReturn(List.of());

        mockMvc.perform(get("/internal/customers/2/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getCustomerOrders_returns404_whenCustomerMissing() throws Exception {
        when(orderService.getOrdersForCustomer(99L)).thenThrow(new CustomerNotFoundException(99L));

        mockMvc.perform(get("/internal/customers/99/orders"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateOrderStatus_returns200_whenOrderExistsAndStatusValid() throws Exception {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("PREPARING");
        when(orderService.updateOrderStatus(eq(5L), anyString())).thenReturn(sampleResponse(5L, OrderStatus.PREPARING));

        mockMvc.perform(put("/internal/orders/5/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PREPARING"));
    }

    @Test
    void updateOrderStatus_returns404_whenOrderMissing() throws Exception {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("PREPARING");
        when(orderService.updateOrderStatus(eq(999L), anyString())).thenThrow(new OrderNotFoundException(999L));

        mockMvc.perform(put("/internal/orders/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateOrderStatus_returns400_whenStatusIsIllegal() throws Exception {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("SHIPPED");
        when(orderService.updateOrderStatus(eq(5L), anyString())).thenThrow(new InvalidOrderStatusException("SHIPPED"));

        mockMvc.perform(put("/internal/orders/5/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateOrderStatus_returns400_whenStatusIsBlank() throws Exception {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("");

        mockMvc.perform(put("/internal/orders/5/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
