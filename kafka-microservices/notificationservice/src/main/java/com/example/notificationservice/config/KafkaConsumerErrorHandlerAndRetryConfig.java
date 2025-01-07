package com.example.notificationservice.config;

import com.example.notificationservice.exception.NotRetryableException;
import com.example.notificationservice.exception.RetryableException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Map;
import java.util.Objects;

@Configuration
public class KafkaConsumerErrorHandlerAndRetryConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Autowired
    Environment environment;

    @Bean
    ConsumerFactory<String, Object> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    private Map<String, Object> consumerConfigs() {
        return Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getEnv("spring.kafka.bootstrap-servers"),
                ConsumerConfig.GROUP_ID_CONFIG, getEnv("spring.kafka.consumer.group-id"),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class,
                JsonDeserializer.TRUSTED_PACKAGES, getEnv("spring.kafka.consumer.properties.spring.json.trusted.packages")
        );
    }

    private String getEnv(String key) {
        return Objects.requireNonNull(environment.getProperty(key));
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory, KafkaTemplate<String, Object> kafkaTemplate) {
        // Dead Letter Recovering
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(kafkaTemplate),
                new FixedBackOff(1000, 3) // 3 retries with 1 second interval. After that, the message will be sent to the dead-letter topic
        );

        errorHandler.addNotRetryableExceptions(NotRetryableException.class, HttpServerErrorException.class);
        errorHandler.addRetryableExceptions(RetryableException.class);

        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getEnv("spring.kafka.consumer.bootstrap-servers"),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class
        ));
    }

    /*
        If the consumer function throws IllegalArgumentException or HttpServerErrorException, the message will be sent to the dead-letter topic directly.
        We can add more exceptions to the notRetryableExceptions method.

        Also, we can add retryableExceptions method to the DefaultErrorHandler to specify the exceptions that we want to retry.

        Other exceptions will be sent to the dead-letter topic after the retry attempts are exhausted.

        Rebalancing;
        When a consumer is added or removed from a consumer group, the Kafka broker will trigger a rebalance operation.
        Consumers send a heartbeat to the broker at regular intervals to indicate that they are alive.
        If the broker does not receive a heartbeat from a consumer within a certain time period, it will consider the consumer dead and trigger a rebalance operation.
        We can change this time period by setting the session.timeout.ms property in the consumer configuration.
     */
}
