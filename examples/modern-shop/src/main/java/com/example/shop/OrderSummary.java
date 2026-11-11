package com.example.shop;

import java.time.Instant;
import java.util.UUID;

/** What the API returns. A record: immutable, with equals, hashCode and toString for free. */
public record OrderSummary(UUID id, String sku, int quantity, String status, Instant placedAt) {

    static OrderSummary from(PurchaseOrder order) {
        return new OrderSummary(order.getId(), order.getSku(), order.getQuantity(),
                label(order.getStatus()), order.getPlacedAt());
    }

    /** No default branch: adding an OrderStatus constant makes this stop compiling until it is handled. */
    private static String label(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Awaiting shipment";
            case SHIPPED -> "On its way";
            case DELIVERED, CANCELLED -> "Closed";
        };
    }
}
