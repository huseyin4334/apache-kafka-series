package com.kafka.kafkaproducer.wikimedia;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.net.URI;
import java.util.Properties;

@Slf4j
public class WikimediaChangeProducer {

    public static void main(String[] args) {
        String bootstrapServers = "127.0.0.1:29092,127.0.0.1:39092,127.0.0.1:49092";
        String topic = "wikimedia-change-events";
        String wikimediaUrl = "https://stream.wikimedia.org/v2/stream/recentchange";

        // create producer properties
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", bootstrapServers);
        properties.setProperty("key.serializer", StringSerializer.class.getName());
        properties.setProperty("value.serializer", StringSerializer.class.getName());

        // v2.8- we should set these properties for safer producer
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, String.valueOf(Integer.MAX_VALUE));
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");


        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        // handle changes
        EventHandler handler = new WikimediaHandler(producer, topic);
        EventSource.Builder builder = new EventSource.Builder(handler, URI.create(wikimediaUrl));
        EventSource eventSource = builder.build();

        // start the event source
        eventSource.start();

        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            eventSource.close();
            producer.close();
        }));

        // work just 5 minutes
        try {
            Thread.sleep(5 * 60 * 1000);
        } catch (InterruptedException e) {
            log.error("Error while sleeping", e);
        } finally {
            eventSource.close();
            producer.close();
        }




    }
}
