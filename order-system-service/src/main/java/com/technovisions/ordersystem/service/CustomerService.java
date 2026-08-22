package com.technovisions.ordersystem.service;

import com.technovisions.ordersystem.dto.CreateCustomerRequest;
import com.technovisions.ordersystem.dto.CustomerResponse;
import com.technovisions.ordersystem.entity.Customer;
import com.technovisions.ordersystem.exception.CustomerNotFoundException;
import com.technovisions.ordersystem.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        Customer customer = Customer.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .createdAt(LocalDateTime.now())
                .build();

        Customer saved = customerRepository.save(customer);
        log.info("Created customer id={}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public void assertCustomerExists(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }
    }

    public static CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
