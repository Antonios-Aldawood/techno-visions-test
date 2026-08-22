package com.technovisions.ordersystem.repository;

import com.technovisions.ordersystem.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
