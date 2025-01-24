package com.example.opensearch;

import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;

import java.io.IOException;


@Slf4j
public class RecordSaverIdempotence {
    public void save(ConsumerRecords<String, String> records, RestHighLevelClient openSearchClient) throws IOException {
        for (ConsumerRecord<String, String> record : records) {
            String id = getIdByJson(record.value());

            IndexRequest request = new IndexRequest("wikimedia").source(record.value(), XContentType.JSON)
                    .id(id);

            IndexResponse response = openSearchClient.index(request, RequestOptions.DEFAULT);

            log.info("Record saved to OpenSearch, offset: {}, indexId: {}, extractedId: {}", record.offset(), response.getId(), id);
        }
    }

    private String getIdByJson(String json) {
        return JsonParser.parseString(json)
                .getAsJsonObject()
                .get("meta")
                .getAsJsonObject()
                .get("id")
                .getAsString();
    }
}
