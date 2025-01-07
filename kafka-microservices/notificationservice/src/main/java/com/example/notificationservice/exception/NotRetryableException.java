package com.example.notificationservice.exception;

public class NotRetryableException extends RuntimeException {
    public NotRetryableException(String message) {
        super(message);
    }
}
