package com.example.client.exception;

public class BulkheadFullException extends RuntimeException {

    public BulkheadFullException(String message) {
        super(message);
    }
}
