package com.example.notificationservice.exception;

public class RetryableException extends RuntimeException {
    public RetryableException(String message) {
        super(message);
    }
}
