package com.example.shop;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** A Spring Boot 4 slice test: the new WebMvcTest package, and @MockitoBean in place of @MockBean. */
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    OrderService orderService;

    @Test
    @WithMockUser
    void returnsAnOrder() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.find(id)).thenReturn(
                new OrderSummary(id, "SKU-1", 2, "Awaiting shipment", Instant.parse("2026-11-11T12:00:00Z")));

        mockMvc.perform(get("/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("SKU-1"));
    }

    @Test
    @WithMockUser
    void reportsAMissingOrderAsAProblemDetail() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.find(id)).thenThrow(new OrderNotFoundException(id));

        mockMvc.perform(get("/orders/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Order not found"));
    }
}
