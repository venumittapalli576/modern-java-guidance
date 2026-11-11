package com.example.shop;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<PurchaseOrder, UUID> {

    List<PurchaseOrder> findByStatus(OrderStatus status);
}
