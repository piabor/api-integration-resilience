package com.example.provider.dto;

public record ProviderPaymentResponse(
        String providerPaymentId,
        String status,
        String message
) {
}
