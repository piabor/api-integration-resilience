package com.example.client.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than 0")
        BigDecimal amount,
        @NotBlank(message = "currency is required")
        @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3-letter uppercase code")
        String currency
) {
}
