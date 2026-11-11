package com.example.shop;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Immutable configuration, bound through the record's constructor. */
@ConfigurationProperties("shop")
public record ShopProperties(
        @DefaultValue("http://localhost:8081") URI inventoryUrl,
        @DefaultValue("5s") Duration timeout) {
}
