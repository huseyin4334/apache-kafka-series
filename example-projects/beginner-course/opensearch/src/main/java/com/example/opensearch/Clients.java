package com.example.opensearch;

import java.net.URI;
import java.util.Properties;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;

public class Clients {

    public RestHighLevelClient createOpenSearchClient() {

		String connString = "http://localhost:9200";

		// we build a URI from the connection string
		RestHighLevelClient restHighLevelClient;

		URI connUri = URI.create(connString);

		// extract login information if it exists
		String userInfo = connUri.getUserInfo();

		if (userInfo == null) {
			// REST client without security
			restHighLevelClient = new RestHighLevelClient(
					RestClient.builder(new HttpHost(connUri.getHost(), connUri.getPort(), "http")));

		} else {
			System.out.println("create connection");
			String username = "admin";
			String password = "Search_678_Dryt";
			// Create a new instance of the REST High-Level client
			restHighLevelClient = new RestHighLevelClient(
					RestClient.builder(new HttpHost(connUri.getHost(), connUri.getPort()))
							.setHttpClientConfigCallback(httpClientBuilder -> {
								// Set the credentials provider
								CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
								credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
								httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
								httpClientBuilder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
								return httpClientBuilder;
							}));

			System.out.println("create connection");
		}

		return restHighLevelClient;
	}


    public KafkaConsumer<String, String> createKafkaConsumer() {
		String bootstrapServers = "127.0.0.1:29092,127.0.0.1:39092,127.0.0.1:49092";
		String groupId = "consumer-opensearch";

		Properties properties = new Properties();
		properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

		return new KafkaConsumer<>(properties);

	}

	public KafkaConsumer<String, String> createKafkaConsumerWithoutAutoCommit() {
		String bootstrapServers = "127.0.0.1:29092,127.0.0.1:39092,127.0.0.1:49092";
		String groupId = "consumer-opensearch";

		Properties properties = new Properties();
		properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
		properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

		return new KafkaConsumer<>(properties);

	}
    
}
