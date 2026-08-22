package com.technovisions.orderaggregator.service;

import com.technovisions.orderaggregator.client.SystemServiceClient;
import com.technovisions.orderaggregator.dto.OrderResponse;
import com.technovisions.orderaggregator.dto.UpdateOrderStatusRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderAggregatorServiceTest {

    @Mock
    private SystemServiceClient systemServiceClient;

    private OrderAggregatorService orderAggregatorService;

    @BeforeEach
    void setUp() {
        orderAggregatorService = new OrderAggregatorService(systemServiceClient);
    }

    @Test
    void updateOrderStatus_normalizesCaseAndForwardsToClient_whenStatusIsValid() {
        when(systemServiceClient.updateOrderStatus(eq(5L), any())).thenReturn(
                new OrderResponse(5L, 1L, "Widget", 1, BigDecimal.ONE, "PREPARING", LocalDateTime.now()));

        OrderResponse response = orderAggregatorService.updateOrderStatus(5L, new UpdateOrderStatusRequest("preparing"));

        assertThat(response.getStatus()).isEqualTo("PREPARING");
        ArgumentCaptor<UpdateOrderStatusRequest> captor = ArgumentCaptor.forClass(UpdateOrderStatusRequest.class);
        verify(systemServiceClient).updateOrderStatus(eq(5L), captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("PREPARING");
    }

    @Test
    void updateOrderStatus_neverCallsClient_whenStatusIsIllegal() {
        assertThatThrownBy(() -> orderAggregatorService.updateOrderStatus(5L, new UpdateOrderStatusRequest("SHIPPED")))
                .isInstanceOf(IllegalArgumentException.class);

        verify(systemServiceClient, never()).updateOrderStatus(any(), any());
    }
}
