package com.example.core.events.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductReservationFailedEvent {
    private UUID orderId;
    private UUID productId;
    private Integer productQuantity;
}
