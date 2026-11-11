package com.example.shop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PlaceOrderRequest(@NotBlank String sku, @Positive int quantity) {
}
