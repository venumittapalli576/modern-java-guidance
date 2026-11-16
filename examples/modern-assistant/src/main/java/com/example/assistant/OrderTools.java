package com.example.assistant;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/** What the model may do. Each @Tool method is offered to the model by name and description. */
@Component
class OrderTools {

    private final OrderDirectory orders;

    OrderTools(OrderDirectory orders) {
        this.orders = orders;
    }

    @Tool(description = "Look up the shipping status of an order by its order number")
    String orderStatus(@ToolParam(description = "The order number, for example A-1001") String orderNumber) {
        return orders.statusOf(orderNumber);
    }
}
