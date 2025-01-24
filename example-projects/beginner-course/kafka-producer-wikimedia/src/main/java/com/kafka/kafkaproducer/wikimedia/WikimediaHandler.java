package com.kafka.kafkaproducer.wikimedia;


import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.MessageEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;


@AllArgsConstructor
@Slf4j
public class WikimediaHandler implements EventHandler {

    KafkaProducer<String, String> producer;
    String topic;


    @Override
    public void onOpen() {

    }

    @Override
    public void onClosed() {
        producer.close();
    }

    @Override
    public void onMessage(String s, MessageEvent messageEvent) {
        log.info("Message: {}", messageEvent.getData());
        producer.send(
                new ProducerRecord<>(topic, messageEvent.getData())
        );
    }

    @Override
    public void onComment(String s) {

    }

    @Override
    public void onError(Throwable throwable) {

    }
}
