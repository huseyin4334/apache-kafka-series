package com.example.notificationservice.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;
import java.util.Objects;

@Configuration
@ConditionalOnProperty(value = "kafka.consumer.normal.enabled", havingValue = "true")
public class KafkaConsumerConfig {

    @Autowired
    Environment environment;

    @Bean
    ConsumerFactory<String, Object> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    private Map<String, Object> consumerConfigs() {
        return Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getEnv("spring.kafka.bootstrap-servers"),
                ConsumerConfig.GROUP_ID_CONFIG, "notification-service",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                JsonDeserializer.TRUSTED_PACKAGES, getEnv("spring.kafka.consumer.properties.spring.json.trusted.packages")
        );
    }

    private String getEnv(String key) {
        return Objects.requireNonNull(environment.getProperty(key));
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(ConsumerFactory<String, Object> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    /*
        ConcurrentKafkaListenerContainerFactory is a factory for creating KafkaListenerContainer instances.
        KafkaListenerContainer instances are responsible for managing the lifecycle of a Kafka Consumer.
        ConsumerFactory is a factory for creating Kafka Consumer instances. It's a strategy interface to provide a Consumer instance.

        https://docs.spring.io/spring-kafka/reference/kafka/receiving-messages/kafkalistener-lifecycle.html
     */
}
