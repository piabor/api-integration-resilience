package com.example.client.service;

import com.example.client.dto.CircuitBreakerStatusResponse;
import com.example.client.exception.CircuitBreakerOpenException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class SimpleCircuitBreaker {

    private static final int FAILURE_THRESHOLD = 3;
    private static final Duration OPEN_WAIT_TIME = Duration.ofSeconds(10);

    private State state = State.CLOSED;
    private int consecutiveFailures = 0;
    private Instant openedAt;
    private boolean halfOpenTestInProgress = false;

    public synchronized void assertRequestAllowed() {
        if (state == State.CLOSED) {
            return;
        }

        if (state == State.OPEN) {
            if (openedAt != null && Instant.now().isAfter(openedAt.plus(OPEN_WAIT_TIME))) {
                state = State.HALF_OPEN;
                halfOpenTestInProgress = true;
                return;
            }

            throw new CircuitBreakerOpenException("Circuit breaker is OPEN. Provider calls are temporarily blocked.");
        }

        if (state == State.HALF_OPEN) {
            if (halfOpenTestInProgress) {
                throw new CircuitBreakerOpenException("Circuit breaker is HALF_OPEN. Waiting for test request result.");
            }

            halfOpenTestInProgress = true;
        }
    }

    public synchronized void recordSuccess() {
        consecutiveFailures = 0;
        state = State.CLOSED;
        openedAt = null;
        halfOpenTestInProgress = false;
    }

    public synchronized void recordFailure() {
        if (state == State.HALF_OPEN) {
            openCircuit();
            return;
        }

        consecutiveFailures++;
        if (consecutiveFailures >= FAILURE_THRESHOLD) {
            openCircuit();
        }
    }

    public synchronized void recordIgnoredFailure() {
        if (state == State.HALF_OPEN) {
            halfOpenTestInProgress = false;
            state = State.CLOSED;
        }
    }

    public synchronized CircuitBreakerStatusResponse getStatus() {
        long retryAfterSeconds = 0;
        if (state == State.OPEN && openedAt != null) {
            long elapsed = Duration.between(openedAt, Instant.now()).toSeconds();
            retryAfterSeconds = Math.max(0, OPEN_WAIT_TIME.toSeconds() - elapsed);
        }

        return new CircuitBreakerStatusResponse(state.name(), consecutiveFailures, retryAfterSeconds);
    }

    private void openCircuit() {
        state = State.OPEN;
        openedAt = Instant.now();
        halfOpenTestInProgress = false;
    }

    private enum State {
        CLOSED,
        OPEN,
        HALF_OPEN
    }
}
