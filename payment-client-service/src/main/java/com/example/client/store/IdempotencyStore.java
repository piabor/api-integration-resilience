package com.example.client.store;

import com.example.client.dto.PaymentResponse;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IdempotencyStore {

    private final Map<String, PaymentResponse> responses = new ConcurrentHashMap<>();

    public PaymentResponse get(String key) {
        return responses.get(key);
    }

    public PaymentResponse save(String key, PaymentResponse response) {
        responses.putIfAbsent(key, response);
        return responses.get(key);
    }
}
