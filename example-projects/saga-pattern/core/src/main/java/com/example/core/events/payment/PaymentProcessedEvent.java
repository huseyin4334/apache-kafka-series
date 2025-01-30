package com.example.core.events.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class PaymentProcessedEvent {
    private UUID orderId;
    private UUID paymentId;
}
