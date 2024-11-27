package com.example.opensearch;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;

import com.google.gson.JsonParser;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.common.xcontent.XContentType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenSearchConsumer {
    @SuppressWarnings("deprecation")
	public static void main(String[] args) {
        System.out.println("Main");

        Clients openSearchClient = new Clients();

		RestHighLevelClient elasticSearchClient = openSearchClient.createOpenSearchClient();
		KafkaConsumer<String, String> consumer = openSearchClient.createKafkaConsumer();
//		List<String> topics = Collections.singletonList("wikimedia");

		boolean isIndexExist = false;
		try (elasticSearchClient; consumer) {

			System.out.println("isIndexExist");
			isIndexExist = elasticSearchClient.indices().exists(new GetIndexRequest("wikimedia"), RequestOptions.DEFAULT);
			System.out.println("isIndexExist: " + isIndexExist);

			if (!isIndexExist) {
				log.info("wikimedia not index exist");
				CreateIndexRequest createIndexRequest = new CreateIndexRequest("wikimedia");
				elasticSearchClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
			} else {
				log.info("wikimedia index exist");
			}

			log.info("Begin while to instert data");
			consumer.subscribe(Collections.singletonList("wikimedia_recentchange"));
			while (true) {
				log.info("consumers.poll");
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(3000));

				int recordCount = records.count();

				log.info("Receive: " + recordCount + " record(s");

				for (ConsumerRecord<String, String> record : records) {
					
					
					// String id = record.topic()+"_"+record.partition()+"_"+record.offset();
					
					String id = extractId(record.value());
					
					try {
						IndexRequest indexRequest = new IndexRequest("wikimedia").source(record.value(), XContentType.JSON).id(id);

						IndexResponse response = elasticSearchClient.index(indexRequest, RequestOptions.DEFAULT);

						log.info("_doc/Id "+response.getId());
					} catch (Exception e) {

					}
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

    private static String extractId(String json) {
		return JsonParser.parseString(json).getAsJsonObject().get("meta").getAsJsonObject().get("id").getAsString();
	}
}
