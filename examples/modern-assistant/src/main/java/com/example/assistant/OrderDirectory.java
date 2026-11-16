package com.example.assistant;

import java.util.Map;

import org.springframework.stereotype.Component;

/** Stands in for the order system. */
@Component
class OrderDirectory {

    private final Map<String, String> statusByOrderNumber = Map.of(
            "A-1001", "shipped",
            "A-1002", "being packed");

    String statusOf(String orderNumber) {
        return statusByOrderNumber.getOrDefault(orderNumber, "unknown order");
    }
}
