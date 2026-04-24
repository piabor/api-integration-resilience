package com.example.client.dto;

import java.math.BigDecimal;

public record ProviderPaymentRequest(
        BigDecimal amount,
        String currency
) {
}
