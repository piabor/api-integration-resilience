package com.example.client.controller;

import com.example.client.dto.CircuitBreakerStatusResponse;
import com.example.client.dto.PaymentRequest;
import com.example.client.dto.PaymentResponse;
import com.example.client.service.PaymentService;
import com.example.client.service.SimpleCircuitBreaker;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping
public class PaymentController {

    private final PaymentService paymentService;
    private final SimpleCircuitBreaker simpleCircuitBreaker;

    public PaymentController(PaymentService paymentService, SimpleCircuitBreaker simpleCircuitBreaker) {
        this.paymentService = paymentService;
        this.simpleCircuitBreaker = simpleCircuitBreaker;
    }

    @PostMapping("/payments")
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestHeader("Idempotency-Key") @NotBlank(message = "Idempotency-Key must not be blank") String idempotencyKey,
            @RequestHeader(value = "X-Mode", required = false) String mode,
            @Valid @RequestBody PaymentRequest request) {

        PaymentResponse response = paymentService.createPayment(idempotencyKey, mode, request);
        HttpStatus httpStatus = "PROCESSING".equals(response.status()) ? HttpStatus.ACCEPTED : HttpStatus.OK;
        return ResponseEntity.status(httpStatus).body(response);
    }

    @GetMapping("/circuit-breaker/status")
    public CircuitBreakerStatusResponse getCircuitBreakerStatus() {
        return simpleCircuitBreaker.getStatus();
    }
}
