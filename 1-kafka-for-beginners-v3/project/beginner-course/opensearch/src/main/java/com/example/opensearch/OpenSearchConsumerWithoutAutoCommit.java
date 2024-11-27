package com.example.opensearch;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.GetIndexRequest;

import java.io.IOException;
import java.util.List;

@Slf4j
public class OpenSearchConsumerWithoutAutoCommit {
    public static void main(String[] args) throws IOException {
        System.out.println("Main");

        Clients clients = new Clients();

        RestHighLevelClient openSearchClient = clients.createOpenSearchClient();
        KafkaConsumer<String, String> consumer = clients.createKafkaConsumerWithoutAutoCommit();

        try (openSearchClient; consumer) {
            // create an index
            boolean indexIsExist = openSearchClient.indices().exists(new GetIndexRequest("wikimedia"), RequestOptions.DEFAULT);

            if (!indexIsExist) {
                openSearchClient.indices().create(new CreateIndexRequest("wikimedia"), RequestOptions.DEFAULT);
                log.info("Index created");
            } else
                log.info("Index already exists");

            // subscribe consumer to our topic(s)
            consumer.subscribe(List.of("wikimedia"));
            RecordSaver recordSaver = new RecordSaver();

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(java.time.Duration.ofMillis(100));

                int recordCount = records.count();
                log.info("Received " + recordCount + " records");

                recordSaver.save(records, openSearchClient, false);

                // commit the offsets of the records that have been processed. (Batch)
                consumer.commitSync();
                log.info("Offsets have been committed");
            }
        }




    }
}
