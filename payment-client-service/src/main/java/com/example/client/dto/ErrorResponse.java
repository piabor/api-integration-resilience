package com.example.client.dto;

public record ErrorResponse(
        String code,
        String message
) {
}
