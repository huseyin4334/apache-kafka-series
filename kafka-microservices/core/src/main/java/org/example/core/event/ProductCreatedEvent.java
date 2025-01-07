package org.example.core.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor // Required for JSON deserialization. Consumers will use this to create an instance of this class from JSON.
public class ProductCreatedEvent {
    private String productId;
    String name;
    BigDecimal price;
    Integer quantity;

    @Override
    public String toString() {
        return "ProductCreatedEvent{" +
                "productId='" + productId + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                '}';
    }
}
