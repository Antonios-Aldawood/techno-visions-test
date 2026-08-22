package com.technovisions.orderaggregator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.technovisions.orderaggregator.dto.CreateCustomerRequest;
import com.technovisions.orderaggregator.dto.CustomerResponse;
import com.technovisions.orderaggregator.exception.DownstreamServiceException;
import com.technovisions.orderaggregator.service.CustomerAggregatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerAggregatorService customerAggregatorService;

    @Test
    void createCustomer_returns201WithSuccessEnvelope_whenRequestIsValid() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest("Jane Doe", "jane@example.com", "+1234567890");
        CustomerResponse response = new CustomerResponse(1L, "Jane Doe", "jane@example.com", "+1234567890", LocalDateTime.now());
        when(customerAggregatorService.createCustomer(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.error").isEmpty());
    }

    @Test
    void createCustomer_returns400WithErrorEnvelope_whenEmailIsInvalid() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest("Jane Doe", "not-an-email", "+1234567890");

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    void createCustomer_returns400WithErrorEnvelope_whenPhoneIsInvalid() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest("Jane Doe", "jane@example.com", "abc");

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createCustomer_returns502WithErrorEnvelope_whenSystemServiceUnavailable() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest("Jane Doe", "jane@example.com", "+1234567890");
        when(customerAggregatorService.createCustomer(any()))
                .thenThrow(new DownstreamServiceException("System service is unavailable"));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("DOWNSTREAM_ERROR"));
    }
}
