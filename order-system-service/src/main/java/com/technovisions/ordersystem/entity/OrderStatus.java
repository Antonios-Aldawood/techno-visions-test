package com.technovisions.ordersystem.entity;

/**
 * No RECEIVED status: this system does not track transportation/delivery,
 * so the lifecycle stops at FINISHED (kitchen/warehouse side only).
 */
public enum OrderStatus {
    CREATED,
    PREPARING,
    FINISHED
}
