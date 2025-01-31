package com.example.products.handler;

import com.example.core.commands.product.CancelReservationCommand;
import com.example.core.commands.product.ReserveProductCommand;
import com.example.core.dto.Product;
import com.example.core.events.product.ProductReservationFailedEvent;
import com.example.core.events.product.ProductReservedEvent;
import com.example.products.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@KafkaListener(topics = {
        "${commands.products.topic.name}"
})
public class ProductCommandHandler {
    @Value("${events.products.topic.name}")
    private String productReservedEventTopic;

    @Value("${events.products.fail.topic.name}")
    private String productReservationFailedEventTopic;

    private final ProductService productService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ProductCommandHandler(ProductService productService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.productService = productService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaHandler
    public void handleReserveProductCommand(@Payload ReserveProductCommand command) {
        try {
            Product product = new Product(command.getProductId(), command.getQuantity());
            Product reservedProduct = productService.reserve(product, command.getOrderId());

            log.info("Product reserved: {}", reservedProduct);

            kafkaTemplate.send(productReservedEventTopic, new ProductReservedEvent(
                    command.getOrderId(),
                    reservedProduct.getId(),
                    reservedProduct.getPrice(),
                    reservedProduct.getQuantity()
            ));
        } catch (Exception ex) {
            log.error("Error processing ReserveProductCommand", ex);
            kafkaTemplate.send(productReservationFailedEventTopic, new ProductReservationFailedEvent(
                    command.getOrderId(),
                    command.getProductId(),
                    command.getQuantity()
            ));
        }
    }

    @KafkaHandler
    public void handleCancelReservationProductCommand(@Payload CancelReservationCommand command) {
        try {
            Product product = new Product(command.getProductId(), command.getQuantity());
            productService.cancelReservation(product, command.getOrderId());
        } catch (Exception ex) {
            log.error("Error processing CancelReservationCommand", ex);
        }
    }
}
