package com.example.client.service;

import com.example.client.exception.BulkheadFullException;
import org.springframework.stereotype.Service;

import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

@Service
public class BulkheadService {

    private final Semaphore semaphore = new Semaphore(3);

    public <T> T execute(Supplier<T> action) {
        boolean acquired = semaphore.tryAcquire();
        if (!acquired) {
            throw new BulkheadFullException("Too many concurrent provider calls. Please try again shortly.");
        }

        try {
            return action.get();
        } finally {
            semaphore.release();
        }
    }
}
