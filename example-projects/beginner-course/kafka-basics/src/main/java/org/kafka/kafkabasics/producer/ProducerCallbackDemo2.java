package org.kafka.kafkabasics.producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerCallbackDemo2 {

    private static final Logger log = LoggerFactory.getLogger(ProducerCallbackDemo2.class.getSimpleName());

    public static void main(String[] args) {
        log.info("Hello World!");
        Demos demos = new Demos();

        demos.callbackAndStickyPartitioner();
        demos.callbackAndStickyPartitionerAndConfigurations();
    }

    /*
         When we run this code, we will see the same partition number for each message generally.
         Because partitioner is clever enough. When we send messages so fast, it will send messages in a batch.
         This partitioner method name is StickyPartitioner. It will send messages to the same partition until the batch is full.
         When the batch is full, it will send the next message to the next partition.

     */
}

class Demos {
    private static final Logger log = LoggerFactory.getLogger(ProducerCallbackDemo2.class.getSimpleName());
    public void callbackAndStickyPartitioner() {
    // create producer properties
    Properties properties = new Properties();
    properties.setProperty("bootstrap.servers", "127.0.0.1:9092");

    // set producer properties
    properties.setProperty("key.serializer", StringSerializer.class.getName()); // we will set the key serializer for serializing the key to bytes
    properties.setProperty("value.serializer", StringSerializer.class.getName()); // we will set the value serializer for serializing the value to bytes

    // create the producer
    KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

    for (int i = 1; i < 11; i++) {
        // create a producer record
        // producer record is a generic class that takes two parameters. It uses for sending data to Kafka
        ProducerRecord<String, String> record = new ProducerRecord<>("first_topic", "Hello World! " + i + "th message");

        // send data
        producer.send(record, (recordMetadata, e) -> {
            if (e == null) {
                log.info("Received new metadata. \n" +
                        "Topic: " + recordMetadata.topic() + "\n" +
                        "Partition: " + recordMetadata.partition() + "\n" +
                        "Offset: " + recordMetadata.offset() + "\n" + // offset is the number of messages that are sent to the topic. This value come from the partition
                        "Timestamp: " + recordMetadata.timestamp()); // timestamp is the timestamp of the message
            } else {
                log.error("Error while producing", e);
            }
        });

        // This callback is called when the record is successfully sent or an exception is thrown.
        // We can use it for sure that the record is sent successfully or not.
        // If we can't write a callback and if send function take an exception, send method will throw an exception.

            /*new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {

                }
            }*/
    }


    // flush data and close producer
    // flush will send data and will wait for acknowledgement
    producer.flush();
    producer.close();
}

    public void callbackAndStickyPartitionerAndConfigurations() {
        // create producer properties
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "127.0.0.1:9092");

        // set producer properties
        properties.setProperty("key.serializer", StringSerializer.class.getName()); // we will set the key serializer for serializing the key to bytes
        properties.setProperty("value.serializer", StringSerializer.class.getName()); // we will set the value serializer for serializing the value to bytes
        properties.setProperty("batch.size", "400"); // batch size is the size of the batch that the producer will send to the broker
        // properties.setProperty("partitioner.class", RoundRobinPartitioner.class.getName());
        // if we don't set the partitioner, it will use the default partitioner. Default is StickyPartitioner

        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        for (int j = 1; j < 11; j++) {
            for (int i = 1; i < 31; i++) {
                // create a producer record
                // producer record is a generic class that takes two parameters. It uses for sending data to Kafka
                ProducerRecord<String, String> record = new ProducerRecord<>("first_topic", "Hello World! " + i + "th message");

                // send data
                producer.send(record, (recordMetadata, e) -> {
                    if (e == null) {
                        log.info("Received new metadata. \n" +
                                "Topic: " + recordMetadata.topic() + "\n" +
                                "Partition: " + recordMetadata.partition() + "\n" +
                                "Offset: " + recordMetadata.offset() + "\n" + // offset is the number of messages that are sent to the topic. This value come from the partition
                                "Timestamp: " + recordMetadata.timestamp()); // timestamp is the timestamp of the message
                    } else {
                        log.error("Error while producing", e);
                    }
                });

                // This callback is called when the record is successfully sent or an exception is thrown.
                // We can use it for sure that the record is sent successfully or not.
                // If we can't write a callback and if send function take an exception, send method will throw an exception.

                /*new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata recordMetadata, Exception e) {

                    }
                }*/
            }
        }



        // flush data and close producer
        // flush will send data and will wait for acknowledgement
        producer.flush();
        producer.close();
    }

}
