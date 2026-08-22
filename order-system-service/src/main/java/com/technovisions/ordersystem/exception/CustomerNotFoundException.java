package com.technovisions.ordersystem.exception;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long customerId) {
        super("Customer " + customerId + " does not exist");
    }
}
