package com.example.core.events.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
@AllArgsConstructor
public class ProductReservationCancelledEvent {
    private UUID orderId;
    private UUID productId;
    private int quantity;
}
