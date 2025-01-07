package com.example.notificationservice.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.core.event.ProductCreatedEvent;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@KafkaListener(topics = "product-created-event-topic")
@Slf4j
@Component
public class ProductCreatedEventHandlerWithKafkaHandler {

    @KafkaHandler
    public void handleProductCreatedEvent(ProductCreatedEvent event) {
        log.info("Received product created event: {}", event);
    }

    /*
     When we use @KafkaEventListener in a class, we can define multiple methods in the class, each annotated with @KafkaHandler.
        Each method will be used to handle a different type of message. This can be use for polymorphic deserialization.
        For example our topic have multiple types of messages, we can use @KafkaHandler to handle each type of message in a different method.
        String, Integer, Long, etc. are the types of messages that can be handled by @KafkaHandler.
     */
}
