package com.example.client.service;

import com.example.client.exception.ProviderServerException;
import feign.RetryableException;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ManualRetryService {

    private static final Logger log = LoggerFactory.getLogger(ManualRetryService.class);
    private static final long[] BACKOFF_MILLIS = {1_000L, 2_000L, 4_000L};
    private final AsyncExecutor asyncExecutor;

  public ManualRetryService(AsyncExecutor asyncExecutor) {
    this.asyncExecutor = asyncExecutor;
  }

  public <T> T execute(Supplier<T> action) {
            try {
                return action.get();
            } catch (RetryableException | ProviderServerException ex) {
                asyncExecutor.executeAsyncWithRetry(() -> execute(action));
                throw ex;
            }
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
