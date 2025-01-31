package com.example.payments.handler;

import com.example.core.commands.payment.ProcessPaymentCommand;
import com.example.core.dto.Payment;
import com.example.core.events.payment.PaymentProcessFailedEvent;
import com.example.core.events.payment.PaymentProcessedEvent;
import com.example.payments.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@KafkaListener(topics = "${commands.payments.topic.name}")
public class PaymentProcessHandler {

    @Value("${events.payments.topic.name}")
    private String paymentProcessedEventTopic;

    @Value("${events.payments.fail.topic.name}")
    private String paymentFailedEventTopic;

    private final PaymentService paymentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentProcessHandler(PaymentService paymentService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.paymentService = paymentService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaHandler
    public void handlePaymentProcessCommand(@Payload ProcessPaymentCommand command) {
        try {
            log.info("Payment process command catch: {}", command);
            Payment payment = paymentService.process(
                    new Payment(
                            command.getOrderId(),
                            command.getProductId(),
                            command.getProductPrice(),
                            command.getProductQuantity()
                    )
            );

            kafkaTemplate.send(paymentProcessedEventTopic, new PaymentProcessedEvent(
                    payment.getOrderId(),
                    payment.getId()
            ));

        } catch (Exception e) {
            log.error("Error processing PaymentProcessCommand", e);
            kafkaTemplate.send(paymentFailedEventTopic, new PaymentProcessFailedEvent(
                    command.getOrderId(),
                    command.getProductId(),
                    command.getProductQuantity()
            ));
        }
    }
}
