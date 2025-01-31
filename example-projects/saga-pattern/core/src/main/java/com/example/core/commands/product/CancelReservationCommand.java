package com.example.core.commands.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
@AllArgsConstructor
public class CancelReservationCommand {
    private UUID orderId;
    private UUID productId;
    private int quantity;
}
