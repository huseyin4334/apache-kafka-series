package com.example.productservice.models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
public class ErrorMessage {
    private Date timestamp;
    private String message;
    private String details;

    public ErrorMessage(String message, String details, Date timestamp) {
        this.message = message;
        this.details = details;
        this.timestamp = timestamp;
    }
}
