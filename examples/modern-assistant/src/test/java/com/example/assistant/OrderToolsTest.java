package com.example.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;

class OrderToolsTest {

    @Test
    void theAnnotatedMethodIsOfferedToTheModelAsATool() {
        ToolCallback[] callbacks = MethodToolCallbackProvider.builder()
                .toolObjects(new OrderTools(new OrderDirectory()))
                .build()
                .getToolCallbacks();

        assertThat(callbacks).hasSize(1);
        assertThat(callbacks[0].getToolDefinition().name()).isEqualTo("orderStatus");
        assertThat(callbacks[0].call("{\"orderNumber\":\"A-1001\"}")).contains("shipped");
    }
}
