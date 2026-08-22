package com.technovisions.orderaggregator.client;

import com.technovisions.orderaggregator.dto.CreateCustomerRequest;
import com.technovisions.orderaggregator.dto.CreateOrderRequest;
import com.technovisions.orderaggregator.dto.CustomerResponse;
import com.technovisions.orderaggregator.dto.OrderResponse;
import com.technovisions.orderaggregator.dto.UpdateOrderStatusRequest;
import com.technovisions.orderaggregator.exception.DownstreamNotFoundException;
import com.technovisions.orderaggregator.exception.DownstreamServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Slf4j
@Component
public class SystemServiceClient {

    private static final String API_KEY_HEADER = "X-Internal-Api-Key";

    private final WebClient webClient;
    private final String apiKey;

    public SystemServiceClient(WebClient systemServiceWebClient,
                                @Value("${system-service.api-key}") String apiKey) {
        this.webClient = systemServiceWebClient;
        this.apiKey = apiKey;
    }

    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        log.info("Calling system service: POST /internal/customers");
        return execute(() -> webClient.post()
                .uri("/internal/customers")
                .header(API_KEY_HEADER, apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CustomerResponse.class)
                .block());
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Calling system service: POST /internal/orders");
        return execute(() -> webClient.post()
                .uri("/internal/orders")
                .header(API_KEY_HEADER, apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OrderResponse.class)
                .block());
    }

    public List<OrderResponse> getCustomerOrders(Long customerId) {
        log.info("Calling system service: GET /internal/customers/{}/orders", customerId);
        return execute(() -> webClient.get()
                .uri("/internal/customers/{customerId}/orders", customerId)
                .header(API_KEY_HEADER, apiKey)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<List<OrderResponse>>() {
                })
                .block());
    }

    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        log.info("Calling system service: PUT /internal/orders/{}/status", orderId);
        return execute(() -> webClient.put()
                .uri("/internal/orders/{orderId}/status", orderId)
                .header(API_KEY_HEADER, apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OrderResponse.class)
                .block());
    }

    private <T> T execute(java.util.function.Supplier<T> call) {
        try {
            return call.get();
        } catch (WebClientResponseException.NotFound ex) {
            log.warn("System service returned 404: {}", ex.getResponseBodyAsString());
            throw new DownstreamNotFoundException(extractMessage(ex));
        } catch (WebClientResponseException ex) {
            log.error("System service returned {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new DownstreamServiceException(extractMessage(ex));
        } catch (WebClientRequestException ex) {
            log.error("Failed to reach system service", ex);
            throw new DownstreamServiceException("System service is unavailable", ex);
        }
    }

    private String extractMessage(WebClientResponseException ex) {
        try {
            return ex.getResponseBodyAs(com.fasterxml.jackson.databind.JsonNode.class)
                    .path("message").asText(ex.getStatusText());
        } catch (Exception parseError) {
            return ex.getStatusText();
        }
    }
}
