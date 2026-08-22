package com.technovisions.ordersystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.technovisions.ordersystem.dto.CreateCustomerRequest;
import com.technovisions.ordersystem.dto.CustomerResponse;
import com.technovisions.ordersystem.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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

// Filters (including InternalApiKeyFilter) are disabled here: this slice tests
// controller/validation wiring, not the API key gate - that has its own test.
@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    @Test
    void createCustomer_returns201_whenRequestIsValid() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest("Jane Doe", "jane@example.com", "+1234567890");
        CustomerResponse response = CustomerResponse.builder()
                .id(1L).fullName("Jane Doe").email("jane@example.com").phone("+1234567890")
                .createdAt(LocalDateTime.now())
                .build();
        when(customerService.createCustomer(any())).thenReturn(response);

        mockMvc.perform(post("/internal/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Jane Doe"));
    }

    @Test
    void createCustomer_returns400_whenEmailIsBlank() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest("Jane Doe", "", "+1234567890");

        mockMvc.perform(post("/internal/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCustomer_returns400_whenFullNameIsBlank() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest("", "jane@example.com", "+1234567890");

        mockMvc.perform(post("/internal/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
