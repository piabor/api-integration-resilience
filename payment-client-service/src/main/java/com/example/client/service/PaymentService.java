package com.example.client.service;

import com.example.client.dto.PaymentRequest;
import com.example.client.dto.PaymentResponse;
import com.example.client.dto.ProviderPaymentRequest;
import com.example.client.dto.ProviderPaymentResponse;
import com.example.client.exception.ProviderBadRequestException;
import com.example.client.exception.ProviderServerException;
import com.example.client.feign.PaymentProviderFeignClient;
import com.example.client.store.IdempotencyStore;
import feign.RetryableException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentProviderFeignClient paymentProviderFeignClient;
    private final ManualRetryService manualRetryService;
    private final SimpleCircuitBreaker simpleCircuitBreaker;
    private final BulkheadService bulkheadService;
    private final IdempotencyStore idempotencyStore;

    public PaymentService(PaymentProviderFeignClient paymentProviderFeignClient,
                          ManualRetryService manualRetryService,
                          SimpleCircuitBreaker simpleCircuitBreaker,
                          BulkheadService bulkheadService,
                          IdempotencyStore idempotencyStore) {
        this.paymentProviderFeignClient = paymentProviderFeignClient;
        this.manualRetryService = manualRetryService;
        this.simpleCircuitBreaker = simpleCircuitBreaker;
        this.bulkheadService = bulkheadService;
        this.idempotencyStore = idempotencyStore;
    }

    public PaymentResponse createPayment(String idempotencyKey, String mode, PaymentRequest request) {
        PaymentResponse existingResponse = idempotencyStore.get(idempotencyKey);
        if (existingResponse != null) {
            return existingResponse;
        }

        String paymentId = UUID.randomUUID().toString();

        try {
            PaymentResponse response = bulkheadService.execute(() -> processPayment(paymentId, mode, request));
            return idempotencyStore.save(idempotencyKey, response);
        } catch (ProviderBadRequestException ex) {
            throw ex;
        } catch (RetryableException | ProviderServerException ex) {
            PaymentResponse fallbackResponse = new PaymentResponse(
                    paymentId,
                    null,
                    "PROCESSING",
                    "Payment request accepted but provider is temporarily unavailable"
            );
            return idempotencyStore.save(idempotencyKey, fallbackResponse);
        }
    }

    private PaymentResponse processPayment(String paymentId, String mode, PaymentRequest request) {
        simpleCircuitBreaker.assertRequestAllowed();

        try {
            ProviderPaymentResponse providerResponse = manualRetryService.executeWithRetry(() ->
                    paymentProviderFeignClient.createPayment(
                            mode,
                            new ProviderPaymentRequest(request.amount(), request.currency())
                    )
            );

            simpleCircuitBreaker.recordSuccess();

            return new PaymentResponse(
                    paymentId,
                    providerResponse.providerPaymentId(),
                    "SUCCESS",
                    "Payment completed successfully"
            );
        } catch (ProviderBadRequestException ex) {
            simpleCircuitBreaker.recordIgnoredFailure();
            throw ex;
        } catch (RuntimeException ex) {
            simpleCircuitBreaker.recordFailure();
            throw ex;
        }
    }
}
