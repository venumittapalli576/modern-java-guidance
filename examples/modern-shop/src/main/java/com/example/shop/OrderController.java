package com.example.shop;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @GetMapping("/{id}")
    public OrderSummary find(@PathVariable UUID id) {
        return orders.find(id);
    }

    @GetMapping
    public List<OrderSummary> pending() {
        return orders.pending();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderSummary place(@Valid @RequestBody PlaceOrderRequest request) {
        return orders.place(request);
    }
}
