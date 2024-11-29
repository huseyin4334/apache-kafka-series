# Extended APIs
Kafka Consumers and Producers have existed for a long time, and they are considered **low-level** APIs.

In the high-level API, we have the following components to solve the common use cases:
- **Kafka Connect**: To import/export data from/to Kafka. (External Source -> Kafka to Kafka -> External Sink)
- **Kafka Streams**: To process data in Kafka. (Stream Processing) (Proccessed Kafka topic -> Another Kafka topic)
- **Schema Registry**: To store and retrieve Avro schemas. (Schema Management) (It's like a database table. We can't send data without same type of schema)
- **KSQL**: To query data in Kafka. (SQL like queries)


We will do an example;
We will connect a source with Kafka connect SSE (Server Sent Events) Source Connector, and we will send the data to Kafka. Then we will process the data with Kafka Streams and we will send the processed data to opensearch with kafka connect Elasticsearch sink connector.


# Kafka Connect
Kafka Connect is a tool for scalable and reliable streaming data between Apache Kafka and other systems. It makes it simple to quickly define connectors that move large collections of data into and out of Kafka. We can use before created connectors or we can create our own connectors.

Kafka connect developed for simply connect the any data source to Kafka.

In Kafka connect architecture, we have workers. Workers are responsible for running connectors. We can run multiple workers in a distributed mode.

Kafka connect has two modes:
- **Standalone Mode**: It runs a single Kafka Connect worker process. It is useful for development and testing.
- **Distributed Mode**: It runs multiple Kafka Connect worker processes. It is useful for production.

Also connectors have two types:
- **Source Connectors**: They are used to import data from another system into Kafka.
- **Sink Connectors**: They are used to export data from Kafka to another system.

ETL (Extract, Transform, Load) is a common use case for Kafka Connect.

Full details will be in the Kafka Connect section.

> [Connectors Hub](https://www.confluent.io/hub/)


For usage;
- We should find the connector that we want to use.
- We should download the connector.
- We should configure the connector. (<connector>.properties)
- We should start the connector.


# Kafka Streams
Kafka Streams is a client library for building applications and microservices, where the input and output data are stored in Kafka clusters. It combines the simplicity of writing and deploying standard Java and Scala applications on the client side with the benefits of Kafka's server-side cluster technology.

- Standart Java/Scala application
- No need to create a separate cluster (like Spark, Flink)
- High scalability, elasticity, fault tolerance
- Exactly once capability (This means that the data will be processed exactly once)
- One record at a time processing (We can't process the records in batch)
- Stateful processing (We can store the state in the Kafka Streams)


For usage:
- We should create a Kafka Streams application.
- We should configure the Kafka Streams application. (Connect to Kafka, Define the processing logic)
- We should start the Kafka Streams application.
  - This will be a consumer that reads from the input topic, processes the data, and writes to the output topic.


# Schema Registry
Kafka don't parse the data. It just sends the data as bytes. If we want to parse the data, we should use the Schema Registry.
Apache Avro is a widely used serialization format in the Kafka ecosystem. It is a compact, fast, binary data format that provides a schema. The schema is stored in the Schema Registry.

Schema Registry is a service that manages Avro schemas for Kafka. It provides a RESTful interface for storing and retrieving Avro schemas.

Producers send the schema to the Schema Registry. After that Producer sends the data with the schema id. Kafka validates the schema with the schema id. If the schema is not valid, Kafka will reject the data.
After that, Consumers get the schema id from the data and get the schema from the Schema Registry. Then they can parse the data with the schema.