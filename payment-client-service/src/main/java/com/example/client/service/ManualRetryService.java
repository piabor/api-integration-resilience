package com.example.client.service;

import com.example.client.exception.ProviderServerException;
import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class ManualRetryService {

    private static final Logger log = LoggerFactory.getLogger(ManualRetryService.class);
    private static final long[] BACKOFF_MILLIS = {1_000L, 2_000L, 4_000L};
    private static final int MAX_RETRIES = 3;

    public <T> T executeWithRetry(Supplier<T> action) {
        RuntimeException lastException = null;

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                return action.get();
            } catch (RetryableException | ProviderServerException ex) {
                lastException = ex;
                if (attempt == MAX_RETRIES) {
                    throw ex;
                }

                log.warn("Retry attempt {} of {} after failure: {}", attempt + 1, MAX_RETRIES, ex.getMessage());
                sleep(BACKOFF_MILLIS[attempt]);
            }
        }

        throw lastException;
    }

    private void sleep(long durationMillis) {
        try {
            Thread.sleep(durationMillis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry sleep was interrupted", ex);
        }
    }
}
