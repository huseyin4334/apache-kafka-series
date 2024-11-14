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


## Consumer Groups
> [Code](./resources/3-kafka-console-consumer-in-groups.sh)
In this section, we will see all group members will get the same message.

```bash
./kafka-topics.sh \
--bootstrap-server localhost:9092 \
--create \
--topic test-topic-group \
--config ./../config/server.properties \
--partitions 3 \
```

```bash
./kafka-console-consumer.sh \
--consumer.config ./../config/server.properties \
--bootstrap-server localhost:9092 \
--topic test-topic-group \
--group test-group
```

In the other terminal;

```bash
./kafka-console-producer.sh \
--producer.config ./../config/server.properties \
--broker-list localhost:9092 \
--topic test-topic-group
--producer-property partitioner.class=org.apache.kafka.clients.producer.RoundRobinPartitioner
# We want to seperate the messages to the partitions.
> Hello World
> Hello World 2
> Hello World 3
```

We will see the messages in the consumer terminal.
Let's add more consumers to the group.

```bash
./kafka-console-consumer.sh \
--consumer.config ./../config/server.properties \
--bootstrap-server localhost:9092 \
--topic test-topic-group \
--group test-group
# We won't see any message because the first consumer read all the messages.
```

Continue to produce messages;

```bash
> Hello World 4
> Hello World 5
> Hello World 6
```

We will see the messages seperately in the consumers. Because partitions are assigned to the consumers seperately.

Consumer 1;
Hello World 4
Hello World 5

Consumer 2;
Hello World 6


If we open consumers more than the partition number, some consumers will be idle. Because there are no partitions to assign to them.
When we open new consumers, they will be assigned to the partitions. When we close the consumers, partitions will be assigned to the other consumers.


When we use --from-beginning, we will see all the messages 1 time. When we called the command again, we won't see any message because we read all the messages by group. 
It was working with only one consumer. Because every new consumer get a different group id by created by Kafka.


## Kafka Consumer Group Management
> [Code](./resources/4-kafka-consumer-groups.sh)

We can list, describe, delete the consumer groups with this command.
Also, we can reset the offsets of the consumer groups.
Also, we can connect to the topic with the consumer group.

```bash
./kafka-consumer-groups.sh
# We will see the usable commands

# We can list the consumer groups
./kafka-consumer-groups.sh \
--bootstrap-server localhost:9092 \
--list


# We can describe the consumer group
./kafka-consumer-groups.sh \
--bootstrap-server localhost:9092 \
--describe \
--group test-group

# We will get group name, topic name, partition, current offset, log end offset, lag, consumer id, host, client id
# current offset is the last read offset
# log end offset is the last message offset
# lag is the difference between the current offset and the log end offset
```

When we consume without group;
Kafka will open a new group for the consumer. We can see the group name with the command.

```bash
./kafka-consumer-groups.sh \
--bootstrap-server localhost:9092 \
--list
# test-group
# console-consumer-12345
# console-consumer-12345 is the group name for the consumer.
```


## Kafka Consumer Group Reset Offsets
> [Code](./resources/5-reset-offsets.sh)
We can reset the offsets of the consumer groups with this command.

Simulation Mode;

```bash
./kafka-consumer-groups.sh \
--bootstrap-server localhost:9092 \
--group test-group \
--topic test-topic-group \
--reset-offsets \
--to-earliest \
--dry-run
```

When we run this command, we will see the simulation of the reset. We won't reset the offsets.

Real Mode;

```bash
./kafka-consumer-groups.sh \
--bootstrap-server localhost:9092 \
--group test-group \
--topic test-topic-group \
--reset-offsets \
--to-earliest \
--execute
```

When we run this command, we will reset the offsets of the consumer group. We will see the messages from the beginning.

Also, we can set the offsets with manual values.

```bash
./kafka-consumer-groups.sh \
--bootstrap-server localhost:9092 \
--group test-group \
--topic test-topic-group \
--reset-offsets \
--to-offset 0 # move the offset to the 0th message \
# or
--to-latest # move the offset to the latest message \
# or
--shift-by 2 # move the offset 2 steps forward \
# or
--shift-by -2 # move the offset 2 steps back \
# or
--to-date 2021-09-21T00:00:00.000Z # move the offset to the date \
--execute
```