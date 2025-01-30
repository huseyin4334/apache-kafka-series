package com.example.core.dto;


import com.example.core.types.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class Order {
    private UUID orderId;
    private UUID customerId;
    private UUID productId;
    private Integer productQuantity;
    private OrderStatus status;

    public Order() {
    }

    public Order(UUID orderId, UUID customerId, UUID productId, Integer productQuantity, OrderStatus status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.productId = productId;
        this.productQuantity = productQuantity;
        this.status = status;
    }

    public Order(UUID customerId, UUID productId, Integer productQuantity, OrderStatus status) {
        this.customerId = customerId;
        this.productId = productId;
        this.productQuantity = productQuantity;
        this.status = status;
    }

}
