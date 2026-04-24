package com.example.client.dto;

public record PaymentResponse(
        String paymentId,
        String providerPaymentId,
        String status,
        String message
) {
}
