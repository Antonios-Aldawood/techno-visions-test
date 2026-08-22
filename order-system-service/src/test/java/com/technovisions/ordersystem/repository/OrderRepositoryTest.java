package com.technovisions.ordersystem.repository;

import com.technovisions.ordersystem.entity.Customer;
import com.technovisions.ordersystem.entity.Order;
import com.technovisions.ordersystem.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void findByCustomerId_returnsOnlyThatCustomersOrders() {
        Customer customerA = customerRepository.save(Customer.builder()
                .fullName("Customer A").email("a@example.com").phone("+10000000000")
                .createdAt(LocalDateTime.now()).build());
        Customer customerB = customerRepository.save(Customer.builder()
                .fullName("Customer B").email("b@example.com").phone("+10000000001")
                .createdAt(LocalDateTime.now()).build());

        orderRepository.save(Order.builder()
                .customerId(customerA.getId()).productName("Widget").quantity(1)
                .price(BigDecimal.ONE).status(OrderStatus.CREATED).createdAt(LocalDateTime.now()).build());
        orderRepository.save(Order.builder()
                .customerId(customerA.getId()).productName("Gadget").quantity(2)
                .price(BigDecimal.TEN).status(OrderStatus.PREPARING).createdAt(LocalDateTime.now()).build());
        orderRepository.save(Order.builder()
                .customerId(customerB.getId()).productName("Gizmo").quantity(3)
                .price(BigDecimal.TEN).status(OrderStatus.CREATED).createdAt(LocalDateTime.now()).build());

        List<Order> customerAOrders = orderRepository.findByCustomerId(customerA.getId());

        assertThat(customerAOrders).hasSize(2)
                .extracting(Order::getProductName)
                .containsExactlyInAnyOrder("Widget", "Gadget");
    }

    @Test
    void findByCustomerId_returnsEmptyList_whenCustomerHasNoOrders() {
        Customer customer = customerRepository.save(Customer.builder()
                .fullName("Lonely Customer").email("lonely@example.com").phone("+10000000002")
                .createdAt(LocalDateTime.now()).build());

        List<Order> orders = orderRepository.findByCustomerId(customer.getId());

        assertThat(orders).isEmpty();
    }
}
