package com.technovisions.orderaggregator.controller;

import com.technovisions.orderaggregator.dto.ApiResponse;
import com.technovisions.orderaggregator.dto.CreateCustomerRequest;
import com.technovisions.orderaggregator.dto.CustomerResponse;
import com.technovisions.orderaggregator.service.CustomerAggregatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerAggregatorService customerAggregatorService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        log.info("Incoming request: POST /api/v1/customers email={}", request.getEmail());
        CustomerResponse response = customerAggregatorService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
