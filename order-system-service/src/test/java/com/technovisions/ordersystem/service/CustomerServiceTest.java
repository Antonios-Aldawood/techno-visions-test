package com.technovisions.ordersystem.service;

import com.technovisions.ordersystem.dto.CreateCustomerRequest;
import com.technovisions.ordersystem.dto.CustomerResponse;
import com.technovisions.ordersystem.entity.Customer;
import com.technovisions.ordersystem.exception.CustomerNotFoundException;
import com.technovisions.ordersystem.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerService customerService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository);
    }

    @Test
    void createCustomer_persistsAndReturnsCustomer_whenRequestIsValid() {
        CreateCustomerRequest request = new CreateCustomerRequest("Jane Doe", "jane@example.com", "+1234567890");
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            customer.setId(1L);
            return customer;
        });

        CustomerResponse response = customerService.createCustomer(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFullName()).isEqualTo("Jane Doe");
        assertThat(response.getEmail()).isEqualTo("jane@example.com");
        assertThat(response.getPhone()).isEqualTo("+1234567890");
        assertThat(response.getCreatedAt()).isNotNull();

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        assertThat(captor.getValue().getFullName()).isEqualTo("Jane Doe");
    }

    @Test
    void assertCustomerExists_doesNotThrow_whenCustomerExists() {
        when(customerRepository.existsById(1L)).thenReturn(true);

        customerService.assertCustomerExists(1L);
    }

    @Test
    void assertCustomerExists_throwsCustomerNotFound_whenCustomerMissing() {
        when(customerRepository.existsById(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> customerService.assertCustomerExists(99L))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("99");
    }
}
