package org.kafka.kafkabasics.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerWithKeys3 {

    private static final Logger log = LoggerFactory.getLogger(ProducerWithKeys3.class.getSimpleName());

    public static void main(String[] args) {
        System.out.println("Hello World!");
    }

    public void callbackAndStickyPartitioner() {
        // create producer properties
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "127.0.0.1:9092");

        // set producer properties
        properties.setProperty("key.serializer", StringSerializer.class.getName()); // we will set the key serializer for serializing the key to bytes
        properties.setProperty("value.serializer", StringSerializer.class.getName()); // we will set the value serializer for serializing the value to bytes

        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        for (int j = 0; j  < 10; j ++) {
            for (int i = 1; i < 31; i++) {
                // This key will be used to send data to the same partition
                String key = "id_" + i;
                String value = "Hello World! " + i + "th message";
                String topic = "first_topic";

                ProducerRecord<String, String> record = new ProducerRecord<>(
                        topic,
                        key,
                        value
                );

                // send data
                producer.send(record, (recordMetadata, e) -> {
                    if (e == null) {
                        log.info("Key: " + key + " | Partition: " + recordMetadata.partition());
                    } else {
                        log.error("Error while producing", e);
                    }
                });
            }
        }


        producer.flush();
        producer.close();
    }
}
