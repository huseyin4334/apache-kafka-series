package com.example.orders.config;

import lombok.Setter;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.config.TopicBuilder;

@Setter
@PropertySource("classpath:topic.properties")
@Configuration
public class KafkaTopicsConfig {

    @Value("${partitions}")
    private short partitions;

    @Value("${replication.factor}")
    private short replicationFactor;

    // Events
    @Value("${events.orders.topic.name}")
    private String topicName;
    @Value("${events.products.topic.name}")
    private String productReservedEvent;
    @Value("${events.products.fail.topic.name}")
    private String productReservationFailedEvent;
    @Value("${events.payments.topic.name}")
    private String paymentProcessedEvent;
    @Value("${events.payments.fail.topic.name}")
    private String paymentFailedEvent;
    @Value("${events.orders.approved.topic.name}")
    private String orderApprovedEvent;
    @Value("${events.orders.rejected.topic.name}")
    private String orderRejectedEvent;


    // Commands
    @Value("${commands.products.topic.name}")
    private String productReserveCommand;

    @Value("${commands.payments.topic.name}")
    private String paymentProcessCommand;

    @Value("${commands.orders.approve.topic.name}")
    private String orderApproveCommand;

    @Value("${commands.orders.reject.topic.name}")
    private String orderRejectCommand;

    @Value("${commands.products.fail.topic.name}")
    private String productReserveFailedCommand;

    @Value("${commands.payments.fail.topic.name}")
    private String paymentProcessFailedCommand;

    @Bean
    NewTopic orderCreateTopic() {
        return TopicBuilder.name(topicName)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }

    @Bean
    NewTopic productReservedTopic() {
        return TopicBuilder.name(productReservedEvent)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }

    @Bean
    NewTopic productReservationFailedTopic() {
        return TopicBuilder.name(productReservationFailedEvent)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }

    @Bean
    NewTopic paymentProcessedTopic() {
        return TopicBuilder.name(paymentProcessedEvent)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }

    @Bean
    NewTopic paymentFailedTopic() {
        return TopicBuilder.name(paymentFailedEvent)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }

    @Bean
    NewTopic orderApprovedTopic() {
        return TopicBuilder.name(orderApprovedEvent)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }

    @Bean
    NewTopic orderRejectedTopic() {
        return TopicBuilder.name(orderRejectedEvent)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }

    @Bean
    NewTopic productReserveCommandTopic() {
        return TopicBuilder.name(productReserveCommand)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }

    @Bean
    NewTopic paymentProcessCommandTopic() {
        return TopicBuilder.name(paymentProcessCommand)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }

    @Bean
    NewTopic orderApproveCommandTopic() {
        return TopicBuilder.name(orderApproveCommand)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }

    @Bean
    NewTopic orderRejectCommandTopic() {
        return TopicBuilder.name(orderRejectCommand)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }

    @Bean
    NewTopic productReserveFailedCommandTopic() {
        return TopicBuilder.name(productReserveFailedCommand)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }

    @Bean
    NewTopic paymentProcessFailedCommandTopic() {
        return TopicBuilder.name(paymentProcessFailedCommand)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }



}
