package com.example.client.dto;

public record CircuitBreakerStatusResponse(
        String state,
        int consecutiveFailures,
        long retryAfterSeconds
) {
}
