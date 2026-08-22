package com.technovisions.orderaggregator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.technovisions.orderaggregator.dto.CreateOrderRequest;
import com.technovisions.orderaggregator.dto.OrderResponse;
import com.technovisions.orderaggregator.dto.UpdateOrderStatusRequest;
import com.technovisions.orderaggregator.exception.DownstreamNotFoundException;
import com.technovisions.orderaggregator.service.OrderAggregatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderAggregatorService orderAggregatorService;

    private OrderResponse sampleResponse(Long id, String status) {
        return new OrderResponse(id, 1L, "Widget", 2, new BigDecimal("9.99"), status, LocalDateTime.now());
    }

    @Test
    void createOrder_returns201WithSuccessEnvelope_whenRequestIsValid() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(1L, "Widget", 2, new BigDecimal("9.99"));
        when(orderAggregatorService.createOrder(any())).thenReturn(sampleResponse(10L, "CREATED"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CREATED"));
    }

    @Test
    void createOrder_returns400_whenQuantityIsZero() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(1L, "Widget", 0, new BigDecimal("9.99"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createOrder_returns404WithErrorEnvelope_whenCustomerMissing() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(404L, "Widget", 1, BigDecimal.TEN);
        when(orderAggregatorService.createOrder(any()))
                .thenThrow(new DownstreamNotFoundException("Customer 404 does not exist"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void getCustomerOrders_returns200WithEmptyList_whenCustomerHasNoOrders() throws Exception {
        when(orderAggregatorService.getCustomerOrders(2L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/customers/2/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void getCustomerOrders_returns404_whenCustomerMissing() throws Exception {
        when(orderAggregatorService.getCustomerOrders(99L))
                .thenThrow(new DownstreamNotFoundException("Customer 99 does not exist"));

        mockMvc.perform(get("/api/v1/customers/99/orders"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateOrderStatus_returns200_whenStatusIsValid() throws Exception {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("PREPARING");
        when(orderAggregatorService.updateOrderStatus(anyLong(), any())).thenReturn(sampleResponse(5L, "PREPARING"));

        mockMvc.perform(put("/api/v1/orders/5/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PREPARING"));
    }

    @Test
    void updateOrderStatus_returns400WithErrorEnvelope_whenStatusIsIllegal() throws Exception {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("SHIPPED");
        when(orderAggregatorService.updateOrderStatus(anyLong(), any()))
                .thenThrow(new IllegalArgumentException("'SHIPPED' is not a valid order status"));

        mockMvc.perform(put("/api/v1/orders/5/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void updateOrderStatus_returns400_whenStatusIsBlank() throws Exception {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("");

        mockMvc.perform(put("/api/v1/orders/5/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
