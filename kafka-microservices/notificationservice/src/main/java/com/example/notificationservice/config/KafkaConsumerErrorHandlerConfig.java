package com.example.notificationservice.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;
import java.util.Objects;

@Configuration
@ConditionalOnProperty(value = "kafka.consumer.error-handler.enabled", havingValue = "true")
public class KafkaConsumerErrorHandlerConfig {

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
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate));

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
        ErrorHandlingDeserializer is a deserializer that wraps a delegate deserializer and catches exceptions, returning null instead.
        If the value deserialization fails, the ErrorHandlingDeserializer will catch the exception and log it.
        The ErrorHandlingDeserializer will return null if the deserialization fails.
        After that, we should handle the null value in the consumer.

        DefaultErrorHandler is a default implementation of the ErrorHandler interface.
        In default behavior, the DefaultErrorHandler will log the error and the record, and seek to the next record.
        We can give a custom recoverer to the DefaultErrorHandler to handle the error.
        Recoverer is a strategy interface to recover from an error. It defines what to do when an error occurs.

        DeadLetterPublishingRecoverer is a recoverer that sends the failed record to a dead-letter topic.
        Default dead-letter topic name is <original-topic-name>.DLT
        We can customize the dead-letter topic name by passing the dead-letter topic name to the DeadLetterPublishingRecoverer constructor.

        For example, listener get an error while processing a record from the topic "product-created-event-topic".
        After the retry attempts are exhausted, the record will be sent to the topic "product-created-event-topic.DLT".

     */
}
