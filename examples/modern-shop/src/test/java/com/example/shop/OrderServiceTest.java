package com.example.shop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** A plain unit test: constructor injection means no Spring context is needed. */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orders;

    @Mock
    InventoryClient inventory;

    final Clock clock = Clock.fixed(Instant.parse("2026-11-11T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void placesAnOrderWhenStockIsAvailable() {
        when(inventory.inStock("SKU-1", 2)).thenReturn(true);
        when(orders.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderSummary summary = new OrderService(orders, inventory, clock).place(new PlaceOrderRequest("SKU-1", 2));

        assertThat(summary.sku()).isEqualTo("SKU-1");
        assertThat(summary.status()).isEqualTo("Awaiting shipment");
        assertThat(summary.placedAt()).isEqualTo(clock.instant());
    }

    @Test
    void rejectsAnOrderWhenStockIsShort() {
        when(inventory.inStock("SKU-1", 2)).thenReturn(false);

        assertThatThrownBy(() -> new OrderService(orders, inventory, clock).place(new PlaceOrderRequest("SKU-1", 2)))
                .isInstanceOf(OutOfStockException.class);
    }
}
