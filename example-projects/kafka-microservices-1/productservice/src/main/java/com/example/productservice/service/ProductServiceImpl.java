package com.example.productservice.service;

import com.example.productservice.models.CreateProductRestModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.core.event.ProductCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;

    public String createProductAsynchronously(CreateProductRestModel product) {
        String productId = UUID.randomUUID().toString();

        // TODO: Save the product to the database

        ProductCreatedEvent event = new ProductCreatedEvent(
                productId,
                product.getName(),
                product.getPrice(),
                product.getQuantity()
        );

        // This call is asynchronous. The message will be sent to Kafka, but we won't wait for a response.
        // Response type is CompletableFuture<SendResult<K, V>>. This means, it won't wait for the response.
        // But when the response is available, it will be a SendResult object.
        CompletableFuture<SendResult<String, ProductCreatedEvent>> future = kafkaTemplate.send(
                "product-created-event-topic",
                productId,
                event
        );

        kafkaEventFutureExecutor(future);

        return productId;
    }

    public String createProductSynchronously(CreateProductRestModel product) {
        String productId = UUID.randomUUID().toString();

        // TODO: Save the product to the database

        ProductCreatedEvent event = new ProductCreatedEvent(
                productId,
                product.getName(),
                product.getPrice(),
                product.getQuantity()
        );

        // This call is asynchronous. The message will be sent to Kafka, but we won't wait for a response.
        // Response type is CompletableFuture<SendResult<K, V>>. This means, it won't wait for the response.
        // But when the response is available, it will be a SendResult object.
        CompletableFuture<SendResult<String, ProductCreatedEvent>> future = kafkaTemplate.send(
                "product-created-event-topic",
                productId,
                event
        );

        kafkaEventFutureExecutor(future);

        future.join(); // Wait for the future to complete

        return productId;
    }

    private void kafkaEventFutureExecutor(CompletableFuture<SendResult<String, ProductCreatedEvent>> future) {
        future.whenComplete((result, exception) -> {
            if (exception != null) {
                // Handle the exception
                log.error("Error sending message to Kafka", exception);
            } else {
                // Handle the result
                log.info("Message sent to Kafka successfully. Topic: {}, Partition: {}, Offset: {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );
            }
        });

        /*
            When we don't have enough replicas to acknowledge the write, we will get KafkaProducerException: Failed to send.
            We will see the retries and the exception in the logs. ERROR: NOT_ENOUGH_REPLICAS (retry logs)
         */
    }

    public String createProductSynchronouslyWithGetMethod(CreateProductRestModel product) throws ExecutionException, InterruptedException {
        String productId = UUID.randomUUID().toString();

        // TODO: Save the product to the database

        ProductCreatedEvent event = new ProductCreatedEvent(
                productId,
                product.getName(),
                product.getPrice(),
                product.getQuantity()
        );

        SendResult<String, ProductCreatedEvent> result = kafkaTemplate.send(
                "product-created-event-topic",
                productId,
                event
        )
                .get(); // Wait for the future to complete

        return productId;
    }

    public String createProductSynchronouslyWithRecordObject(CreateProductRestModel product) throws ExecutionException, InterruptedException {
        String productId = UUID.randomUUID().toString();

        // TODO: Save the product to the database

        ProductCreatedEvent event = new ProductCreatedEvent(
                productId,
                product.getName(),
                product.getPrice(),
                product.getQuantity()
        );

        ProducerRecord<String, ProductCreatedEvent> record = new ProducerRecord<>(
                "product-created-event-topic",
                productId,
                event
        );

        record.headers().add("PRODUCT_ID", productId.getBytes());

        SendResult<String, ProductCreatedEvent> result = kafkaTemplate.send(record)
                .get(); // Wait for the future to complete

        return productId;
    }
}
