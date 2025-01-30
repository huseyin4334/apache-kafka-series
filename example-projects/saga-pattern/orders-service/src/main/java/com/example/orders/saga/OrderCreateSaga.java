package com.example.orders.saga;

import com.example.core.commands.OrderApproveCommand;
import com.example.core.commands.ProcessPaymentCommand;
import com.example.core.commands.ReserveProductCommand;
import com.example.core.events.order.OrderApproveFailedEvent;
import com.example.core.events.order.OrderApprovedEvent;
import com.example.core.events.order.OrderCreatedEvent;
import com.example.core.events.payment.PaymentProcessFailedEvent;
import com.example.core.events.payment.PaymentProcessedEvent;
import com.example.core.events.product.ProductReservationFailedEvent;
import com.example.core.events.product.ProductReservedEvent;
import com.example.core.types.OrderStatus;
import com.example.orders.dto.OrderHistory;
import com.example.orders.service.OrderHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@KafkaListener(topics = {
        "${events.orders.topic.name:order-created-event}",
        "${events.products.topic.name:product-reserved-event}",
        "${events.products.fail.topic.name:product-reservation-failed-event}",
        "${events.payments.topic.name:payment-processed-event}",
        "${events.payments.fail.topic.name:payment-failed-event}",
        "${events.orders.approve.topic.name:order-approved-event}",
        "${events.orders.approve.fail.topic.name:order-approve-failed-event}"
})
public class OrderCreateSaga {
    @Value("${commands.products.topic.name:product-reservation-command}")
    private String productReservationCommandTopic;

    @Value("${commands.payments.topic.name:process-payment-command}")
    private String processPaymentCommandTopic;

    @Value("${commands.orders.topic.name:order-approve-command}")
    private String orderApproveCommandTopic;

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
        // TODO: Implement the logic to handle the PaymentProcessFailedEvent
        log.info("Payment failed event catch: {}", event);
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
