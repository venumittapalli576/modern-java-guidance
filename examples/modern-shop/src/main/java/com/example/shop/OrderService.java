package com.example.shop;

import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Dependencies arrive through the one constructor. No @Autowired, and the fields are final. */
@Service
public class OrderService {

    private final OrderRepository orders;
    private final InventoryClient inventory;
    private final Clock clock;

    public OrderService(OrderRepository orders, InventoryClient inventory, Clock clock) {
        this.orders = orders;
        this.inventory = inventory;
        this.clock = clock;
    }

    @Transactional
    public OrderSummary place(PlaceOrderRequest request) {
        if (!inventory.inStock(request.sku(), request.quantity())) {
            throw new OutOfStockException(request.sku());
        }
        PurchaseOrder saved = orders.save(new PurchaseOrder(request.sku(), request.quantity(), clock.instant()));
        return OrderSummary.from(saved);
    }

    @Transactional(readOnly = true)
    public OrderSummary find(UUID id) {
        return orders.findById(id)
                .map(OrderSummary::from)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<OrderSummary> pending() {
        return orders.findByStatus(OrderStatus.PENDING).stream()
                .map(OrderSummary::from)
                .toList();
    }
}
