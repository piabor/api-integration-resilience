package com.example.client.dto;

public record ProviderPaymentResponse(
        String providerPaymentId,
        String status,
        String message
) {
}
