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

public class ConsumerAutoCommitDemo2 {

    static final Logger log = Logger.getLogger(ConsumerAutoCommitDemo2.class.getSimpleName());

    /*
        When we call consumer.poll(), offsets are committed automatically.
        It is not recommended to use this method in production.
        Because we didn't process the data yet. If we process the data and commit the offset, it is better.

        We can change this behavior with the enable.auto.commit property.
        Also, we can set the auto.commit.interval.ms property. This property is used to set the interval for committing the offset.
        For example, this value is 5000 milliseconds. It will get data with poll() method and commit offsets after 5 seconds.
        commitSync() method is used to commit the offset manually and synchronously.
        commitAsync() method is used to commit the offset asynchronously.

        poll() method uses the auto.commit.interval.ms property to commit the offset with commitAsync() method.
     */

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

        // create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        final Thread mainThread = Thread.currentThread();

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
