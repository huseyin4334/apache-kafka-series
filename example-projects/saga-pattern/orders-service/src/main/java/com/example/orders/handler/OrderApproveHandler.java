package com.example.orders.handler;

import com.example.core.commands.order.OrderApproveCommand;
import com.example.core.events.order.OrderApproveFailedEvent;
import com.example.core.events.order.OrderApprovedEvent;
import com.example.orders.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@KafkaListener(topics = "${commands.orders.approve.topic.name:order-approve-command}")
public class OrderApproveHandler {

    @Value("${events.orders.approve.topic.name:order-approved-event}")
    private String orderApprovedEventTopic;

    @Value("${events.orders.approve.fail.topic.name:order-approve-failed-event}")
    private String orderApproveFailedEventTopic;


    private final OrderService orderService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderApproveHandler(OrderService orderService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderService = orderService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener
    private void handleOrderApproveEvent(@Payload OrderApproveCommand command) {
        try {
            log.info("Order approve event catch: {}", command);
            orderService.approveOrder(command.getOrderId());

            kafkaTemplate.send(orderApprovedEventTopic, new OrderApprovedEvent(
                    command.getOrderId()
            ));
        } catch (Exception e) {
            log.error("Error processing OrderApproveCommand", e);
            kafkaTemplate.send(orderApproveFailedEventTopic, new OrderApproveFailedEvent(
                    command.getOrderId()
            ));
        }
    }
}
