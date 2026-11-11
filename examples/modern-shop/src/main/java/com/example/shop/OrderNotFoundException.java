package com.example.shop;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {

    private final UUID orderId;

    public OrderNotFoundException(UUID orderId) {
        super("No order with id " + orderId);
        this.orderId = orderId;
    }

    public UUID orderId() {
        return orderId;
    }
}
