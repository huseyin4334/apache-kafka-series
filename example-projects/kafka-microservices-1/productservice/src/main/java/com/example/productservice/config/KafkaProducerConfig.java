package com.example.productservice.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.kafka.producer.key-serializer}")
    private String keySerializer;
    @Value("${spring.kafka.producer.value-serializer}")
    private String valueSerializer;
    @Value("${spring.kafka.producer.acks}")
    private int acks;
    @Value("${spring.kafka.producer.retries}")
    private int retries;
    @Value("${spring.kafka.producer.properties.retry.backoff.ms}")
    private Long retryBackoffMs;

    @Value("${spring.kafka.producer.properties.linger.ms}")
    private Long lingerMs;

    @Value("${spring.kafka.producer.properties.request.timeout.ms}")
    private Long requestTimeoutMs;

    @Value("${spring.kafka.producer.properties.delivery.timeout.ms}")
    private Long deliveryTimeoutMs;

    Map<String, Object> producerConfigs() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        config.put(ProducerConfig.ACKS_CONFIG, String.valueOf(acks));
        config.put(ProducerConfig.RETRIES_CONFIG, String.valueOf(retries));
        config.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, String.valueOf(retryBackoffMs));
        config.put(ProducerConfig.LINGER_MS_CONFIG, String.valueOf(lingerMs));
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, String.valueOf(requestTimeoutMs));
        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, String.valueOf(deliveryTimeoutMs));
        return config;
    }

    @Bean
    ProducerFactory<String, ProductCreatedEvent> productCreateProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate() {
        return new KafkaTemplate<>(productCreateProducerFactory());
    }

    /*
        When we inject with different key value pairs the kafkaTemplate, Spring will automatically create a KafkaTemplate bean object with the configurations we provided.
     */

}
