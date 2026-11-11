package com.example.shop;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** An entity is an ordinary class: its identity is the primary key, not the value of every field. */
@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String sku;

    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false)
    private Instant placedAt;

    protected PurchaseOrder() {
        // for JPA
    }

    public PurchaseOrder(String sku, int quantity, Instant placedAt) {
        this.sku = sku;
        this.quantity = quantity;
        this.placedAt = placedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getPlacedAt() {
        return placedAt;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PurchaseOrder other && id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return PurchaseOrder.class.hashCode();
    }
}
