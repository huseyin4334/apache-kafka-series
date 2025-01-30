package com.example.core.events.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class PaymentProcessFailedEvent {
    private UUID orderId;
    private UUID productId;
    private Integer productQuantity;
}
