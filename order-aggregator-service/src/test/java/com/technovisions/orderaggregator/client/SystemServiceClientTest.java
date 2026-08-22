package com.technovisions.orderaggregator.client;

import com.technovisions.orderaggregator.dto.CreateCustomerRequest;
import com.technovisions.orderaggregator.dto.CreateOrderRequest;
import com.technovisions.orderaggregator.exception.DownstreamNotFoundException;
import com.technovisions.orderaggregator.exception.DownstreamServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Exercises SystemServiceClient's response-handling branches without a real HTTP server,
 * using Spring's documented approach of stubbing the WebClient's underlying ExchangeFunction.
 */
class SystemServiceClientTest {

    private ExchangeFunction exchangeFunction;
    private SystemServiceClient client;

    @BeforeEach
    void setUp() {
        exchangeFunction = mock(ExchangeFunction.class);
        WebClient webClient = WebClient.builder()
                .baseUrl("http://system-service.local")
                .exchangeFunction(exchangeFunction)
                .build();
        client = new SystemServiceClient(webClient, "test-api-key");
    }

    @Test
    void createCustomer_returnsResponse_whenSystemServiceSucceeds() {
        String body = """
                {"id":1,"fullName":"Jane Doe","email":"jane@example.com","phone":"+1234567890","createdAt":"2024-01-01T10:00:00"}
                """;
        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(
                ClientResponse.create(org.springframework.http.HttpStatus.CREATED)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .body(body)
                        .build()));

        var response = client.createCustomer(new CreateCustomerRequest("Jane Doe", "jane@example.com", "+1234567890"));

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFullName()).isEqualTo("Jane Doe");
    }

    @Test
    void createOrder_throwsDownstreamNotFound_whenSystemServiceReturns404() {
        String body = """
                {"code":"CUSTOMER_NOT_FOUND","message":"Customer 404 does not exist"}
                """;
        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(
                ClientResponse.create(org.springframework.http.HttpStatus.NOT_FOUND)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .body(body)
                        .build()));

        assertThatThrownBy(() -> client.createOrder(new CreateOrderRequest(404L, "Widget", 1, BigDecimal.ONE)))
                .isInstanceOf(DownstreamNotFoundException.class)
                .hasMessageContaining("Customer 404 does not exist");
    }

    @Test
    void createCustomer_throwsDownstreamServiceException_whenSystemServiceReturns500() {
        String body = """
                {"code":"INTERNAL_ERROR","message":"An unexpected error occurred"}
                """;
        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(
                ClientResponse.create(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .body(body)
                        .build()));

        assertThatThrownBy(() -> client.createCustomer(new CreateCustomerRequest("Jane", "jane@example.com", "+1234567890")))
                .isInstanceOf(DownstreamServiceException.class);
    }

    @Test
    void getCustomerOrders_throwsDownstreamNotFound_whenCustomerMissing() {
        String body = """
                {"code":"CUSTOMER_NOT_FOUND","message":"Customer 99 does not exist"}
                """;
        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(
                ClientResponse.create(org.springframework.http.HttpStatus.NOT_FOUND)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .body(body)
                        .build()));

        assertThatThrownBy(() -> client.getCustomerOrders(99L))
                .isInstanceOf(DownstreamNotFoundException.class);
    }
}
