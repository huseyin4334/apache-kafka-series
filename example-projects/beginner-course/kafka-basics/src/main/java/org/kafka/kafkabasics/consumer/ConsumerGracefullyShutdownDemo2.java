package org.kafka.kafkabasics.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

public class ConsumerGracefullyShutdownDemo2 {

    static final Logger log = Logger.getLogger(ConsumerGracefullyShutdownDemo2.class.getSimpleName());

    public static void main(String[] args) {
        Properties properties = new Properties();

        properties.setProperty("bootstrap.servers", "127.0.0.1:9092");

        // consumer serializers
        properties.setProperty("key.deserializer", StringSerializer.class.getName());
        properties.setProperty("value.deserializer", StringSerializer.class.getName());


        // consumer group id
        String groupId = "my-first-application";
        properties.setProperty("group.id", groupId);

        // consumer offset
        properties.setProperty("auto.offset.reset", "earliest");
        // none: throw exception to the consumer if no offset is found for the consumer's group
        // earliest: automatically reset the offset to the earliest offset
        // latest: automatically reset the offset to the latest offset

        // we go with a specific group id. Because of that, if I didn't connect before with this group id, kafka will set my offset to the earliest offset.
        // If i connect with the same group id, kafka will continue from where I left off.
        // I can see whe I left off in logs. It will show me the offset number. (ConsumerCoordinator)

        // create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        final Thread mainThread = Thread.currentThread();

        // shutdown hook is used to gracefully shutdown the application
        // shutdown hook is a thread that is registered with the JVM and gets executed when the JVM is shutting down.
        // it will trigger when we click the stop button in the IDE or when we press ctrl+c in the terminal. (It will stop the application gracefully)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // consumer.wakeup() is a special method to interrupt consumer.poll()
            // it will throw WakeUpException
            log.info("Detected a shutdown, let's exit be calling consumer.wakeup()");

            consumer.wakeup();

            // we added this line to wait for the main thread to finish
            try {
                mainThread.join();
            } catch (InterruptedException e) {
                log.severe("Application got interrupted");
            }
        }));

        try {
            // subscribe consumer to our topic(s)
            consumer.subscribe(Arrays.asList(
                    "first_topic",
                    "second_topic"
            ));


            // poll for new data
            while (true) {
                log.info("Polling for new data");
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                // Poll method is used to retrieve data from Kafka. It takes a parameter as a duration.
                // Duration gives for how long we want to poll data from Kafka. After this time, it will return data to us.
                // If there is no data, it will return an empty list.

                for (ConsumerRecord<String, String> record : records) {
                    log.info("Key: " + record.key() + ", Value: " + record.value());
                    log.info("Partition: " + record.partition() + ", Offset: " + record.offset());
                }
            }
        } catch (WakeupException e) {
            log.info("Received shutdown signal!");
        } catch (Exception e) {
            log.severe("General exception: " + e.getMessage());
        } finally {
            consumer.close();
            log.info("Consumer is closing");
        }


    }
}
