package com.example.notificationservice.handler;

import com.example.notificationservice.exception.NotRetryableException;
import com.example.notificationservice.exception.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.example.core.event.ProductCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class ProductCreatedEventHandler {

    @Autowired
    RestTemplate restTemplate;

    @KafkaListener(topics = "product-created-event-topic")
    public void handleProductCreatedEvent(ProductCreatedEvent event) {
        log.info("Received product created event: {}", event);
    }

    @KafkaListener(topics = "product-created-event-topic")
    public void handleProductCreatedEventRetryable(ProductCreatedEvent event) {
        try {
            String response = restTemplate.getForObject("http://localhost:8080/api/400", String.class);
            log.info("Received product created event: {}", event);
        } catch (RestClientException e) {
            log.error("Error occurred while sending the event to the mock service", e);
            throw new RetryableException("Error occurred while sending the event to the mock service");
        } catch (Exception e) {
            log.error("Error occurred while sending the event to the mock service", e);
            throw new NotRetryableException("Error occurred while sending the event to the mock service");
        }
    }
}
