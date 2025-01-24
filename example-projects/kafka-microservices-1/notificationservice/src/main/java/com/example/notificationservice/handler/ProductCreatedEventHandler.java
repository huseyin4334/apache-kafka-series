package com.example.notificationservice.handler;

import com.example.notificationservice.exception.NotRetryableException;
import com.example.notificationservice.exception.RetryableException;
import com.example.notificationservice.model.CreatedProduct;
import com.example.notificationservice.repository.CreatedProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.example.core.event.ProductCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class ProductCreatedEventHandler {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CreatedProductRepository repository;

    @KafkaListener(topics = "product-created-event-topic")
    public void handleProductCreatedEvent(ProductCreatedEvent event) {
        log.info("Received product created event: {}", event);
    }

    @Transactional
    @KafkaListener(topics = "product-created-event-topic")
    public void handleProductCreatedEventIdempotent(
            @Payload ProductCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header("PRODUCT_ID") String ownSetHeader
    ) {

        assert ownSetHeader.equals(key);

        log.info("Received product created event: {}", event);

        // Check if the event is already processed. (Look at db)
        boolean isProcessed = repository.isExistsById(key);

        if (isProcessed) {
            log.info("Event is already processed: {}", event);
            return;
        }

        // save the event to db
        repository.save(new CreatedProduct(ownSetHeader, key));
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
