package com.example.shop;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Order> getOrder(@PathVariable String id) {
        Order order = orderRepository.getOne(id);
        return ResponseEntity.ok(order);
    }

    @RequestMapping(value = "/open", method = RequestMethod.GET)
    public List<String> openOrderIds() {
        return orderRepository.findAll().stream()
                .filter(order -> statusLabel(order).equals("Open"))
                .map(Order::getId)
                .collect(Collectors.toList());
    }

    private String statusLabel(Order order) {
        String label;
        switch (order.getStatus()) {
            case "PENDING":
            case "PROCESSING":
                label = "Open";
                break;
            default:
                label = "Closed";
        }
        return label;
    }

    private boolean inStock(String sku) {
        Integer available = restTemplate.getForObject("http://inventory/stock/" + sku, Integer.class);
        return available != null && available > 0;
    }
}
