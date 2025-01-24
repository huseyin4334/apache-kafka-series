package org.kafka.kafkabasics.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemo1 {

    private static final Logger log = LoggerFactory.getLogger(ProducerDemo1.class.getSimpleName());

    public static void main(String[] args) {
        log.info("Hello World!");

        // create producer properties
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "127.0.0.1:9092");

        // set producer properties
        properties.setProperty("key.serializer", StringSerializer.class.getName()); // we will set the key serializer for serializing the key to bytes
        properties.setProperty("value.serializer", StringSerializer.class.getName()); // we will set the value serializer for serializing the value to bytes

        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        // create a producer record
        // producer record is a generic class that takes two parameters. It uses for sending data to Kafka
        ProducerRecord<String, String> record = new ProducerRecord<>("first_topic", "Hello World!");

        // send data
        producer.send(record);

        // flush data and close producer
        // flush will send data and will wait for acknowledgement
        producer.flush();
        producer.close();
    }
}
