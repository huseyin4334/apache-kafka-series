# Apache Kafka
- Created by LinkedIn in 2011
- Now open source under Apache 2.0 mainly maintained by Confluent, IBM, Cloudera, etc.
- Distributed, resilient architecture, fault-tolerant
- Horizontally scalable 
  - can scale to 100s of brokers
  - can scale to millions of messages per second
- High performance (latency of less than 10ms)
- Used by the 2000+ companies.

## Use cases
- Messaging system
- Activity tracking
- Gather metrics from many different locations
- Application logs gathering
- Stream processing
- Decoupling of system dependencies
- Integration with big data technologies
- Microservices communication (Pub/Sub)




# Theory

## Topics, Partitions, And Offsets
### Topics
- A particular stream of data
- It's similar to a table in a database. (without the constraints)
- You can have as many topics as you want.
- A topic is identified by its **name**.
- Any kind of message format is allowed.
- The sequence of messages is called a **data stream**.
- We can't query a topic. We can only append to it and read from it.

### Partitions And Offsets
- Topics are split into partitions.
- Each partition is ordered.
- Also, all messages within the partitions are ordered.
- Each message within a partition gets an incremental id number called **offset**.
- Each partition has own offset.

> Kafka topics are **immutable**. It means that once a message is written to a partition, it can't be changed.

---

Example:
We want to get the position of all the trucks in real-time.
Every truck will send its GPS coordinates to Kafka every 20 seconds.
Each message has the GPS coordinates(latitude and longitude), truck id, and a timestamp.

We choose 10 partitions for the topic `truck_position`. (It's arbitrary. We will learn how to choose the number of partitions later.)

We have a location dashboard that needs to display the position of all the trucks in real-time.
When a truck successfully arrives at a location, we need to send a notification to the manager.

Trucks --> Kafka --> Location Dashboard, Notification Service

- Topic: `truck_position`
- Partitions: 
  - 0, 1, 2, 3, 4, 5, 6, 7, 8, 9


---

Let's check what happening;
- Data will be written to the topic `truck_position` **immutably**.
- Data is kept for a limited time (default is 7 days).
- Offsets are not reused even if the data is deleted. (Offsets will increase always)
- Order is guaranteed only within a partition, not across partitions.
- Data is assigned randomly to a partition unless a key is provided. 


## Producers And Message Keys
### Producers
- Producers write data to topics.
- Producer automatically knows to which broker, topic and partition to write to.
- **Producers choose which message to assign to which partition within a topic.**
- In kafka broker failures, Producers will automatically recover. (Assume that we have more than one broker)
  - Kafka producer can handle the broker failures and the broker leader changes.
  - Producer can send messages to the new leader without any downtime.
  - In this way, Producers automatically recover from the broker failures.


### Message Keys
- Key is a value that is sent with the message.
- It can be any type. (string, number, binary, etc.)
- When this key is `null`, the message is sent to a partition which is selected in a round-robin way. (Load balancing)
- When the key is not `null`, the message is sent to a specific partition based on a hash of the key. (Hashing)

When we need to guarantee that messages from a specific key will always go to the same partition (order is important), we can use the key.

For example, we want to send our truck gps data to the same partition for ordered by time. We can use the truck id as the key.


### Message
- Message has;
- Key (optional)
- Value
- Compression type
- Headers (optional)
- Partition + Offset
- Timestamp (optional)


### Message Serialization
- Kafka only accepts byte arrays.
- It gets the byte array from producer and sends the binary array to the consumer.

Because of this, Kafka needs a **Serializer** and a **Deserializer** for the message.
Kafka clever enough to know which serializer/deserializer to use based on the type of the objects.

Kafka come with common serializers/deserializers;
- String (including JSON)
- Integer, Long, Double
- Int, Float
- Avro (schema registry)
- Protobuf


### Hashing
Kafka use `Producer Partitioner Logic` to the find the partition for the message.
A kafka partitioner is a code logic that takes a record and determines which partition to send the record to.

Default partitioner is **murMur2**.
`target = Math.abs(Utils.murmur2(keyBytes)) % (numPartitions - 1)`


## Consumers And Deserialization
- Consumers read data from topics. (identified by name)
- Consumers automatically know which broker to read from.
- In case of broker failures, consumers know how to recover.
- **Data  is read in order from lowest offset to highest offset.**

Deserialization is same like serialization process.

Consumer should know which format the data for get it.
If we send the different type of data from producer, consumer can fail.


## Consumer Groups And Offsets
### Consumer Groups
- Consumers read data in consumer groups.
- Consumer groups are used to read data in parallel.
- Also, separate the load between the consumers.

Consumer groups guarantee that a message is only read by one consumer in the group.

It's ok when we have 1 topic, 1 consumer group and multiple partitions and consumers.

But how will work when we have 1 topic, 2 consumer groups and multiple partitions and consumers?
Answer is;
- Each consumer group will read all the messages from the topic.
  - So, every message will be read by all the consumer groups in parallel, independently and 1 time.

For example;
- We have a topic `truck_position` with 10 partitions.
- We have 2 consumer groups `location_dashboard` and `notification_service`.
- location_dashboard will get gps data and show the location of the trucks.
- notification_service will get gps data and send a notification to the manager when the truck arrives at the location.

In this example, both consumer groups will read all the messages from the topic `truck_position`.

---

When we create a distinct group, we will use `group.id` property for read the data 1 time.
When we don't set the `group.id`, Kafka will use a random group id for the consumer.

### Offsets
Kafka stores the offsets at which a consumer group has been reading.
The offsets committed are in Kafka **topic** named `__consumer_offsets`. (This is an internal topic.)

When a consumer reads data from Kafka;
Consumer want a message from the partition.
Kafka will return the message and the offset.
When a consumer reads successfully, it will commit the offset to Kafka.
Kafka will store the offset in the `__consumer_offsets` topic and won't return the same message again.

When a consumer fails, it will read the data from the last committed offset.


### Delivery Semantics
By default, Java consumers have **at least once** delivery semantics. It automatically commits offsets.
There are 3 delivery semantics;
- At most once
  - Offsets are committed as soon as the message is received.
  - If the processing goes wrong, the message will be lost.
- At least once
  - Offsets are committed after the message is processed.
  - If the processing goes wrong, the message will be read again.
  - In this case, the message can be processed twice.
- Exactly once
  - Kafka workflows: use Transactional API(easy with Kafka Streams)
  - External System workflows: use an **idempotent** consumer.

When we need to guarantee that a message is processed exactly once, we need to use the `Exactly Once` delivery semantics.


## Brokers And Topics
A kafka cluster is composed of multiple brokers.
Brokers are just Kafka servers.

Each broker is identified with its `ID` (integer).
Each broker contains certain topic partitions.

Brokers have the topic partitions distributed among them.
When we create a topic with 3 partitions, each broker will have 1 partition.
When we create a topic with 2 partitions, each broker will have 1 partition and the third partition will be distributed between the brokers.

After connecting to any broker(called a `bootstrap broker`), we will be connected to the entire cluster.

Every kafka broker is also called a `bootstrap server`.

Kafka Clients sent a request to the bootstrap server, and get list of all brokers in the cluster (metadata).
After that, client can connect to the specific broker to produce or consume data.


## Topic Replication
Replication factor is the number of copies of the topic.
Topics should have a replication factor of at least 2. (usually 2 or 3)

When we set this value, kafka will replicate the data to other brokers to ensure fault tolerance.
If one broker is down, another broker can serve the data.

---

At any time **only one broker** can be **a leader for a partition**.
Producers can only send data to the leader broker.
Also, kafka consumers by default read data from the leader.

When the leader broker is down, a new leader will be elected from the ISR (In-Sync Replicas).

After the kafka v2.4+, kafka consumers can read data from the closest replica broker. (It's called `Fetch from follower`)
This way can help improve the latency, and also decrease the network costs.


## Producer Acknowledgements
Producer can receive acknowledgements from the broker.
There are 3 possible values;
- acks=0
  - Producer won't wait for an acknowledgement from the broker.
  - It's the fastest but least secure.
- acks=1
  - Producer will wait for an acknowledgement from the leader broker.
  - It's the default value.
- acks=all
  - Producer will wait for an acknowledgement from all the in-sync replicas.
  - It's the slowest but most secure.

When we set the `acks=all`, we can guarantee that the data is written to multiple brokers, and it's safe.

Durability is the guarantee that once data is written more than 1 broker, it won't be lost.


## Management (Zookeeper, Kafka KRaft)
**Kafka 2.x versions can't work without Zookeeper.**
Kafka 3.x versions can work without Zookeeper. (It's called Kafka KRaft)
Kafka 4.x versions will not have Zookeeper.

### Zookeeper
- Zookeeper manages brokers. (keeps a list of brokers)
- Zookeeper helps in performing leader election for partitions.
- Zookeeper sends notifications to Kafka in case of changes (e.g. new topic, broker dies, broker comes up, delete topics, etc.)

Zookeeper by design operates with an odd number of servers. (3, 5, 7, etc.)
Zookeeper has a leader (handle writes) and followers (handle reads).

Until kafka 4.x, kafka not production ready without Zookeeper.


### Kafka KRaft
Kafka KRaft is a new feature in Kafka 3.x.
Kafka KRaft is a self-managed metadata quorum.
Kafka KRaft is a replacement for Zookeeper.

Zookeeper shows scaling issues when Kafka clusters have more than 100k partitions.
By removing Zookeeper, Kafka can scale to millions of partitions.
Improve stability, makes easier to operate, and reduce the operational cost.
Single security model for the whole system.
Single process to start with kafka. (No need to start Zookeeper and Kafka separately)
Faster controller shutdown and recover.