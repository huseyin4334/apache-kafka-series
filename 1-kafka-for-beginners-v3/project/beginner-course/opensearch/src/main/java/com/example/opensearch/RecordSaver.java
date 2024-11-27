package com.example.opensearch;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.logging.Logger;


@Slf4j
public class RecordSaver {
    public void save(ConsumerRecords<String, String> records, RestHighLevelClient openSearchClient, boolean logRecords) throws IOException {
        for (ConsumerRecord<String, String> record : records) {
            IndexRequest request = new IndexRequest("wikimedia")
                    .source(record.value(), XContentType.JSON);

            IndexResponse response = openSearchClient.index(request, RequestOptions.DEFAULT);

            if (logRecords)
                log.info("Record saved to OpenSearch, offset: {}, indexId: {}", record.offset(), response.getId());
        }
    }

    public void bulkSave(ConsumerRecords<String, String> records, RestHighLevelClient openSearchClient, boolean logRecords) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        for (ConsumerRecord<String, String> record : records) {
            IndexRequest request = new IndexRequest("wikimedia")
                    .source(record.value(), XContentType.JSON);

            bulkRequest.add(request);
        }

        if (bulkRequest.numberOfActions() > 0) {
            BulkResponse response = openSearchClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            if (logRecords)
                log.info("Inserted {} records, status: {}", response.getItems().length, response.status().getStatus());

            // We can add a delay here to simulate a slow consumer
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
