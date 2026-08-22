package com.technovisions.ordersystem.exception;

public class InvalidOrderStatusException extends RuntimeException {

    public InvalidOrderStatusException(String status) {
        super("'" + status + "' is not a valid order status");
    }
}
