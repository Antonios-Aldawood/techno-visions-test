package com.technovisions.orderaggregator.service;

import com.technovisions.orderaggregator.client.SystemServiceClient;
import com.technovisions.orderaggregator.dto.CreateCustomerRequest;
import com.technovisions.orderaggregator.dto.CustomerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerAggregatorService {

    private final SystemServiceClient systemServiceClient;

    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        CustomerResponse response = systemServiceClient.createCustomer(request);
        log.info("Customer created downstream with id={}", response.getId());
        return response;
    }
}
