package org.kafka.kafkabasics.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.CooperativeStickyAssignor;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

public class ConsumerRebalancedDemo3 {

    static final Logger log = Logger.getLogger(ConsumerRebalancedDemo3.class.getSimpleName());

    /*
        We have some rebalance strategies;
        - RangeAssignor: It will assign partitions to consumers in a way that each consumer will get a range of partitions.
        - RoundRobin: It will assign partitions to consumers in a way that each consumer will get one partition at a time.
        - StickyAssignor: It will assign partitions to consumers in a way that each consumer will get a partition,
                            and it will keep it until the consumer dies or leaves the group.
                            Minimize the number of partitions movements when a consumer joins or leaves the group.
       - CooperativeStickyAssignor: It is the same as StickyAssignor. But it is cooperative. It will work with the new version of Kafka.
       - Default assignor is RangeAssignor. But allows upgrading to CooperativeStickyAssignor with kafka v3.0.0.

        Static Group Membership;
        - If a consumer dies, it will leave the group. If it comes back, it will join the group again.
        - When we set group.instance.id, it will be a static group membership.
        - Normally, when consumer dies and come  back again, it will join the group with a new id.
        - Because of that, it will get a new partition again.
        - Also, we can set session.timeout.ms a value. Kafka will wait for this time to see if the consumer is alive or not.
        - If the consumer doesn't send a heartbeat in this time, Kafka will remove the consumer from the group. And partitions will be reassigned.
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
        //properties.setProperty("group.instance.id", "consumer-1"); // static group membership (it will keep the partition when the consumer dies and come back again.

        // consumer offset
        properties.setProperty("auto.offset.reset", "earliest");

        // Set the partition assignment strategy to CooperativeStickyAssignor
        /*
            We will see 4 info;
            Assigned partitions: It will show the partitions that are assigned to the consumer.
            Current owned partitions: This means what partitions are owned by the consumer now.
            Added partitions: It will show different partitions that are added to the consumer.
            Revoked partitions: It will show the partitions that are revoked from the consumer.

            Example;
            1 consumer added to the group. It got a partition. But in this moment, first_topic-3 in my consumer.
            consumer-1;
            Assigned partitions: [first_topic-0, first_topic-1, first_topic-2]
            Current owned partitions: [first_topic-0, first_topic-1, first_topic-2, first_topic-3]
            Added partitions: []
            Revoked partitions: [first_topic-3]

            // consumer-2 got the partition
            consumer-2;
            Assigned partitions: [first_topic-3]
            Current owned partitions: []
            Added partitions: [first_topic-3]
            Revoked partitions: []

            ---
            After poll time;
            consumer-1;
            Assigned partitions: [first_topic-0, first_topic-1, first_topic-2]
            Current owned partitions: [first_topic-0, first_topic-1, first_topic-2]
            Added partitions: []
            Revoked partitions: []

            consumer-2;
            Assigned partitions: [first_topic-3]
            Current owned partitions: [first_topic-3]
            Added partitions: []
            Revoked partitions: []
         */

        properties.setProperty("partition.assignment.strategy", CooperativeStickyAssignor.class.getName());

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
