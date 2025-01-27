package com.example.transferservice.service;

import com.example.transferservice.model.TransferRestModel;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext // This annotation is used to indicate that the ApplicationContext associated with a test is dirty and should be closed. If we need to create a new ApplicationContext after other tests have run, we can use this annotation.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@EmbeddedKafka(topics = {"withdraw-money-topic", "deposit-money-topic"}, partitions = 3, count = 3, controlledShutdown = true)
@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
class TransferServiceImplTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    private KafkaMessageListenerContainer<String, TransferRestModel> container;
    private BlockingQueue<ConsumerRecord<String, TransferRestModel>> records;

    @BeforeAll
    void setUp() {
        Map<String, Object> consumerProps = consumerProps();

        DefaultKafkaConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);

        // container is a message listener container that will listen to the withdraw-money-topic
        container = new KafkaMessageListenerContainer<>(consumerFactory, new ContainerProperties("withdraw-money-topic"));

        records = new LinkedBlockingDeque<>();
        container.setupMessageListener((MessageListener<String, TransferRestModel>) record -> {
            System.out.println("Listened message: " + record);
            records.add(record);
        });

        // Start the container and wait until it has started.
        container.start();

        // Wait until the container has the required number of assigned partitions
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    @AfterAll
    void tearDown() {
        container.stop();
    }

    /*
     * Test Naming Convention:
     * MethodName_StateUnderTest_ExpectedBehavior
     * mvn test -Dtest=TransferServiceImplTest#transferV2_TransferRestModel_ReturnsTrue
     */
    @Test
    void transferV2_TransferRestModel_ReturnsTrue() throws InterruptedException {
        // Given (Arrange)
        TransferRestModel transferRestModel = new TransferRestModel();
        transferRestModel.setAmount(new BigDecimal(100));
        transferRestModel.setSenderId("A");
        transferRestModel.setRecepientId("B");

        // When (Act)
        boolean result = transferService.transferV2(transferRestModel);

        // Then (Assert)
        ConsumerRecord<String, TransferRestModel> r = records.poll(3000, java.util.concurrent.TimeUnit.MILLISECONDS);

        assertNotNull(r);
        assertEquals(transferRestModel.getSenderId(), r.value().getSenderId());
        assertEquals(transferRestModel.getRecepientId(), r.value().getRecepientId());
        assertEquals(transferRestModel.getAmount(), r.value().getAmount());
    }

    private Map<String, Object> consumerProps() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("sender", "false", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // log consumerProps
        consumerProps.forEach((k, v) -> System.out.println(k + ":" + v));
        return consumerProps;
    }
}

/*
 * EmbeddedKafka annotation is used to start an embedded Kafka broker. It is used to test Kafka components.
 * spring.embedded.kafka.brokers sets by spring.
 * controlledShutdown is used to control the shutdown of the Kafka broker.
 * Given, when, then uses AAA (Triple A) pattern. Behavior-Driven Development (BDD) style.
 * Arrange, act, assert uses AAA (Triple A) pattern. Classic style.
 */