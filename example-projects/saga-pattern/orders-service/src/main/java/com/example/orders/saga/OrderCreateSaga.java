package com.example.orders.saga;

import com.example.core.commands.order.OrderApproveCommand;
import com.example.core.commands.payment.ProcessPaymentCommand;
import com.example.core.commands.product.CancelReservationCommand;
import com.example.core.commands.product.ReserveProductCommand;
import com.example.core.events.order.OrderApproveFailedEvent;
import com.example.core.events.order.OrderApprovedEvent;
import com.example.core.events.order.OrderCreatedEvent;
import com.example.core.events.payment.PaymentProcessFailedEvent;
import com.example.core.events.payment.PaymentProcessedEvent;
import com.example.core.events.product.ProductReservationFailedEvent;
import com.example.core.events.product.ProductReservedEvent;
import com.example.core.types.OrderStatus;
import com.example.orders.service.OrderHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@KafkaListener(topics = {
        "${events.orders.topic.name}",
        "${events.products.topic.name}",
        "${events.products.fail.topic.name}",
        "${events.payments.topic.name}",
        "${events.payments.fail.topic.name}",
        "${events.orders.approved.topic.name}",
        "${events.orders.rejected.topic.name}"
})
public class OrderCreateSaga {
    @Value("${commands.products.topic.name}")
    private String productReservationCommandTopic;

    @Value("${commands.payments.topic.name}")
    private String processPaymentCommandTopic;

    @Value("${commands.orders.approve.topic.name}")
    private String orderApproveCommandTopic;

    @Value("${commands.products.fail.topic.name}")
    private String productReservationFailedCommandTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderHistoryService orderHistoryService;

    public OrderCreateSaga(KafkaTemplate<String, Object> kafkaTemplate, OrderHistoryService orderHistoryService) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderHistoryService = orderHistoryService;
    }

    @KafkaHandler
    public void handleOrderCreatedEvent(@Payload OrderCreatedEvent event) {
        log.info("Order created event catch: {}", event);

        ReserveProductCommand command = new ReserveProductCommand(
                event.getProductId(),
                event.getProductQuantity(),
                event.getOrderId()
        );

        kafkaTemplate.send(productReservationCommandTopic, command);

        orderHistoryService.add(event.getOrderId(), OrderStatus.CREATED);
    }

    @KafkaHandler
    public void handleProductReservedEvent(@Payload ProductReservedEvent event) {
        log.info("Product reserved event catch: {}", event);

        ProcessPaymentCommand command = new ProcessPaymentCommand(
                event.getOrderId(),
                event.getProductId(),
                event.getProductPrice(),
                event.getProductQuantity()
        );

        kafkaTemplate.send(processPaymentCommandTopic, command);
    }

    @KafkaHandler
    public void handleProductReservationFailedEvent(@Payload ProductReservationFailedEvent event) {
        // TODO: Implement the logic to handle the ProductReservationFailedEvent
        log.info("Product reservation failed event catch: {}", event);
    }

    @KafkaHandler
    public void handlePaymentProcessedEvent(@Payload PaymentProcessedEvent event) {
        log.info("Payment processed event catch: {}", event);
        kafkaTemplate.send(orderApproveCommandTopic, new OrderApproveCommand(
                event.getOrderId()
        ));
    }

    @KafkaHandler
    public void handlePaymentFailedEvent(@Payload PaymentProcessFailedEvent event) {
        log.info("Payment failed event catch: {}", event);
        kafkaTemplate.send(productReservationCommandTopic, new CancelReservationCommand(
                event.getOrderId(),
                event.getProductId(),
                event.getProductQuantity()
        ));
    }

    @KafkaHandler
    public void handleOrderApprovedEvent(@Payload OrderApprovedEvent event) {
        log.info("Order approved event catch: {}", event);
        orderHistoryService.add(event.getOrderId(), OrderStatus.APPROVED);
    }

    @KafkaHandler
    public void handleOrderApproveFailedEvent(@Payload OrderApproveFailedEvent event) {
        // TODO: Implement the logic to handle the OrderApproveFailedEvent
        log.info("Order approve failed event catch: {}", event);
    }


    /*
     * Happy path:
     * Create a product.
     * Create an order with this product.
     * Call the order create history.
     */
}
