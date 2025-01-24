package org.kafka.kafkabasics.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

public class ConsumerDemo1 {

    static final Logger log = Logger.getLogger(ConsumerDemo1.class.getSimpleName());

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
    }
}
