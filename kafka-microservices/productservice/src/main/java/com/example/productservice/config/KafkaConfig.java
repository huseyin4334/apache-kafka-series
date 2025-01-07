package com.example.productservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    NewTopic createProductCreatedTopic() {
        return TopicBuilder.name("product-created-event-topic") // this topic will be created if it doesn't exist
                .partitions(3)
                .replicas(3)
                .config("min.insync.replicas", "2") // minimum number of replicas that must acknowledge a write
                .build();
    }
}
