# Setup
We can install and use Kafka several ways. Here are some options:
- Install binaries and run them
- Use Docker
- Use Confluent Cloud

I don't want to install binaries on my machine, so I'll use Docker.
Every container is a broker in this case.
It will be our virtual development environment like our main machine.
And we should have java installed on our machine.
Because of that, I'll use Docker to run Kafka.
> https://hub.docker.com/r/apache/kafka
> https://learn.conduktor.io/kafka/how-to-install-apache-kafka-on-linux/

```bash
./kafka-topics.sh --bootstrap-server localhost:9092 --create --topic test-topic
```

```bash
docker exec --workdir /opt/kafka/bin/ -it broker sh
```

---
We will add path variables to our `.bashrc` file. Because everything working with sh files in Kafka is in the `bin` folder.

```bash
# This is default added in docker
echo "export PATH=$PATH:/opt/kafka/bin" >> ~/.bashrc

# or
vim ~/.bashrc

# Add this line to the end of the file
export PATH=$PATH:/opt/kafka/bin
```

```bash
# Start Server (This is in local)
# In the container, container starts with this command
# We won't use this command in the container
kafka-server-start.sh ./../config/server.properties
```

## Zookeeper
Before 3.3.1 version.

```bash 
# When we use zookeeper
/zooker-server-start.sh ./../config/zookeeper.properties
```

## Kraft Mode
> https://learn.conduktor.io/kafka/how-to-install-apache-kafka-on-linux-without-zookeeper-kraft-mode/

Production Ready with 3.3.1 version of kafka

```bash
kafka-storage.sh random-uuid
# We will get a random UUID for the broker.id
```

```bash
kafka-storage.sh format -t <uuid> -c ./../config/kraft/server.properties
```

```bash
kafka-server-start.sh ./../config/kraft/server.properties
```

With Docker;

```bash
docker run -d --name kafka -p 9092:9092 -e \ 
KAFKA_LISTENERS=PLAINTEXT://:9092 \
BROKER_ID=1 \ # WE USED UUID but we can use this
```

# Kafka CLI
We will use Kafka CLI to create, update, delete topics, etc.
And we will start with `karfka-topics.sh` commands.

## Kafka Topics
> [Codes](./resources/0-kafka-topics.sh)

```bash
./kafka-topics.sh
# We will see the usable commands

# bootstrap-server is the address of the Kafka server
# --create is the command to create a topic
# --topic is the name of the topic
# We set the server properties file. We can change it.
# not use --zookeeper because we use kraft mode
./kafka-topics.sh --bootstrap-server localhost:9092 \
--create \
--topic test-topic \
--config ./../config/server.properties

# We change the default partition number (3 is default)
./kafka-topics.sh --bootstrap-server localhost:9092 \
--create \
--topic test-topic \
--config ./../config/server.properties \
--partitions 10 \
--replication-factor 3 # This is for multiple server(broker) replication.
# We can set the replication factor to 1 for development.
# When we set 3, every information will replicate the 2 other servers.
# If we run this command with 1 broker. We will get an error.
```

---

Let's list the topics.

```bash
./kafka-topics.sh --bootstrap-server localhost:9092 --list

# We will see the names of the topics
```

Describe the topic.

```bash
./kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic test-topic
# We will see partitions and additional information

# Topic: test-topic PartitionCount: 10 ReplicationFactor: 3 Configs: segment.bytes=1073741824
# Topic: test-topic Partition: 0 Leader: 1 Replicas: 1,2,3 Isr: 1,2,3
# Topic: test-topic Partition: 1 Leader: 2 Replicas: 2,3,1 Isr: 2,3,1
# ...

# Leader is the broker id, Also replicas and Isr point the broker id.
```

## Kafka Console Producer
> [Code](./resources/1-kafka-console-producer.sh)

```bash
./kafka-console-producer.sh
# We will see the usable commands
# --broker-list is the address of the Kafka server
# --topic is the name of the topic
# We can write messages to the topic with this command

./kafka-console-producer.sh \
# this configuration file uses for adjusting the producer.
# broker informations, serialization settings, performance settings, etc.
--producer.config ./../config/server.properties \
--broker-list localhost:9092 \
--topic test-topic
# When acks=all, Kafka will finish take the message when all the brokers(leader and replicas) get the message.
# Extra we can set producer-propert
--producer-property acks=all
--producer-property compression.type=snappy

# When we enter this command, Command line will wait for the message.
# We are producing messages to the topic.

> Hello Kafka
> Hello Kafka 2
> Hello Kafka 3
> Ctrl + C
```

For `producer-property`;
1. bootstrap.servers: Addresses of the Kafka brokers to connect to.
2. key.serializer: Class to use for serializing message keys.
3. value.serializer: Class to use for serializing message values.
4. acks: Number of acknowledgments the producer requires for a request to be considered complete.
5. retries: Number of times to retry sending a message if it fails.
6. batch.size: Maximum number of messages to send in a single batch.
7. linger.ms: Time to wait before sending a message.
8. buffer.memory: Amount of memory the producer can use to buffer messages.
9. compression.type: Type of compression to use for messages (e.g., gzip, snappy).


---

If We try to write non-existing topic, we will get an timeout error.
Command will open message sending line. It will look at the topic is available or not.

If we configured the server with auto-create topic, we can write to the non-existing topic.
But it will warn us about the topic is not connected a leader because we have just 1 broker.
Auto-create is not a good practice for production.


---

Let's add keys to our messages;

```bash
./kafka-console-producer.sh \
--producer.config ./../config/server.properties \
--broker-list localhost:9092 \
--topic test-topic \
--property parse.key=true \
--property key.separator=: # We set the key separator as colon

> hi friend:Hello Kafka
> key:Hello Huseyin
> blabla # This will get an error because it doesn't have a key (NoKeySeperatorFound)
```

## Kafka Console Consumer
> [Code](./resources/2-kafka-console-consumer.sh)

Assume we produced some messages to the topic. ("Hello Kafka", "Hello Kafka 2", "Hello Kafka 3")
```bash
./kafka-console-producer.sh \
--producer.config ./../config/server.properties \
--broker-list localhost:9092 \
--topic test-topic \
# We want to partition the messages with RoundRobinPartitioner. So messages will seperate to the partitions.
--producer-property partitioner.class=org.apache.kafka.clients.producer.RoundRobinPartitioner
```

```bash
./kafka-console-consumer.sh
--consumer.config ./../config/server.properties \
--bootstrap-server localhost:9092 \
--topic test-topic
# we will see the unread messages
# Hello Kafka
# Hello Kafka 2
# Hello Kafka 3
# Ctrl + C

./kafka-console-consumer.sh
--consumer.config ./../config/server.properties \
--bootstrap-server localhost:9092 \
--topic test-topic
# We won't see any message because we read all the messages.

# We can set say the kafka to read from the beginning
./kafka-console-consumer.sh
--consumer.config ./../config/server.properties \
--bootstrap-server localhost:9092 \
--topic test-topic \
--from-beginning
# We will see all the messages from the beginning
# But they won't be ordered because of the partitioning. We read from different partitions seperately.
# Hello Kafka 2
# Hello Kafka
# Hello Kafka 3
```

---

Let's see more understandable values with properties;

```bash
./kafka-console-consumer.sh \
--consumer.config ./../config/server.properties \
--bootstrap-server localhost:9092 \
--topic test-topic \
# Formatter is the class to use to format the message for display.
# We control it with properties.
--formatter kafka.tools.DefaultMessageFormatter \
--property print.timestamp=true \
--property print.key=true \
--property print.value=true \
--property print.partition=true \
--from-beginning

# We will see the messages with the timestamp, key, value, and partition. (When new messages come, we will see them)
# 1632210130000     Partition:2    hi friend    Hello Kafka
# 1632210130000     Partition:0    key          Hello Huseyin
# 1632210130000     Partition:1    null         Hello World
```