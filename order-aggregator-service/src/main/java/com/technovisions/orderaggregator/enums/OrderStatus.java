package com.technovisions.orderaggregator.enums;

/**
 * Mirrors com.technovisions.ordersystem.entity.OrderStatus in the system service.
 * Duplicated deliberately rather than shared via a common module - keeping the two
 * services independently deployable/buildable outweighs the small duplication cost
 * for a three-value enum.
 */
public enum OrderStatus {
    CREATED,
    PREPARING,
    FINISHED;

    public static OrderStatus parse(String rawValue) {
        try {
            return OrderStatus.valueOf(rawValue.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException("'" + rawValue + "' is not a valid order status. Allowed values: "
                    + java.util.Arrays.toString(OrderStatus.values()));
        }
    }
}
