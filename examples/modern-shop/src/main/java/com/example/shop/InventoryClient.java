package com.example.shop;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Blocking HTTP calls go through RestClient, built from the builder Spring Boot configures. */
@Component
public class InventoryClient {

    private final RestClient restClient;

    public InventoryClient(RestClient.Builder builder, ShopProperties properties) {
        this.restClient = builder.baseUrl(properties.inventoryUrl().toString()).build();
    }

    public boolean inStock(String sku, int quantity) {
        StockLevel level = restClient.get()
                .uri("/stock/{sku}", sku)
                .retrieve()
                .body(StockLevel.class);
        return level != null && level.available() >= quantity;
    }

    record StockLevel(String sku, int available) {
    }
}
