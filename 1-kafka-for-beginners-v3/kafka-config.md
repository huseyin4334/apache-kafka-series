# Configuration
- Kafka is highly configurable. You can configure Kafka by setting properties in the server.properties file.
- Also we can change the configuration at runtime using the Kafka CLI tools.

```bash
# WE have 3 controller with craft mode, 2 brokers.
# To list all the configuration
kafka-configs.sh

kafka-topics.sh --create --topic my-topic --partitions 3 --replication-factor 2 --bootstrap-server localhost:29092,localhost:29093,localhost:29094

# To describe a configuration (for dynamic configuration (Our setup is dynamic))
kafka-configs.sh --describe --entity-type topics --entity-name my-topic --bootstrap-server localhost:29092,localhost:29093,localhost:29094
# Configs: 


###########
# Alter the configuration (Alter is used to change the configuration)
kafka-configs.sh --alter --entity-type topics --entity-name my-topic --bootstrap-server localhost:29092,localhost:29093,localhost:29094 --add-config min.insync.replicas=2
# We changed the min.insync.replicas to 2

kafka-configs.sh --describe --entity-type topics --entity-name my-topic --bootstrap-server localhost:29092,localhost:29093,localhost:29094
# We can see the min.insync.replicas is 2 and other explanation about this.
# Configs: min.insync.replicas=2


###########
kafka-configs.sh --alter --entity-type topics --entity-name my-topic --bootstrap-server localhost:29092,localhost:29093,localhost:29094 --delete-config min.insync.replicas
# We deleted the min.insync.replicas

kafka-configs.sh --describe --entity-type topics --entity-name my-topic --bootstrap-server localhost:29092,localhost:29093,localhost:29094
# Configs:
```


# Segments And Indexes
Segments are the way Kafka stores data on disk. Each partition is made up of multiple segments. Each segment is a pair of files: a data file and an index file.

- The data file contains the actual messages.
- The index file contains two additional information:
  - The offset of the messages in the data file. Helps Kafka to quickly find messages by offset.
  - The timestamp of the messages. Helps Kafka to quickly find messages by timestamp. (This info is point to the offset)

When a segment reaches a certain size, it is closed and a new segment is created. This is called a log segment.
The index file is used to look up the offset of a message in the data file. This is how Kafka can quickly find messages by offset.

This working structure is like relational databases where we have data files and index files. It works in the same way but a little bit different.

---

When writing to segments, just one segment is active and the others are read-only. This is because Kafka is optimized for writes and not reads. This is why Kafka is so fast at writing data.

`log.segment.bytes` is the maximum size of a segment. When a segment reaches this size, it is closed and a new segment is created. (default is 1GB)
`log.segment.ms` is the maximum time a segment can stay open. When a segment reaches this time, it is closed and a new segment is created. (default is 1 week)


# Log Cleanup Policies
Log cleanup policies are used to determine when messages can be deleted from a partition. This is important because Kafka does not delete messages immediately after they are consumed. Instead, it waits for a certain condition to be met before deleting messages.

There are two log cleanup policies:
- `delete` - This policy deletes messages based on the `log.retention.ms` and `log.retention.bytes` properties.
  - `log.cleanup.policy=delete`
  - `log.retention.ms` - The maximum time a message can stay in a partition. (default is 7 days)
  - `log.retention.bytes` - The maximum size of a partition. When a partition reaches this size, messages are deleted. (default is -1, which means no limit)
- `compact` - This policy deletes messages based on the `cleanup.policy` property.
  - `log.cleanup.policy=compact`
  - When the same key is written multiple times, only the latest message is kept. This is useful for changelogs and databases.
  - But deleted keys can still seen by consumers because of `delete.retention.ms` property. (default is 24 hours). The message is deleted after 24 hours completely.
  - `segment.ms`, `segment.bytes` are also important for this policy.
  - `min.compaction.lag.ms` is the minimum time a message can stay in a partition before it is compacted. (default is 0). (Compaction is the process of removing duplicate keys)
  - `delete.retention.ms` is the amount of time a deleted key can still be seen by consumers. (default is 24 hours)
  - `min.cleanable.dirty.ratio` is the minimum ratio of dirty messages to clean messages before the log cleaner can clean a partition. (default is 0.5)
  - **These configurations are important for the compact policy.**

---

Also, `log.cleaner.backoff.ms` is the amount of time the log cleaner should wait before cleaning a partition. This is important because the log cleaner can impact the performance of the broker. (default is 15 seconds) (WE can think it like a garbage collector in Java)

---

```bash
kafka-topics.sh --create --topic my-topic --partitions 3 --replication-factor 2 --bootstrap-server localhost:29092,localhost:29093,localhost:29094 --config cleanup.policy=delete

kafka-configs.sh --entity-type topics --entity-name my-topic --bootstrap-server localhost:29092,localhost:29093,localhost:29094 --alter --add-config retention.ms=60000
```


# Unclean Leader Election
Unclean leader election is a feature that allows Kafka to elect a leader even if the leader is not in sync with the followers. This can happen if the leader is down for a long time and the followers are out of sync.

By default, unclean leader election is disabled. This means that Kafka will not elect a leader if the leader is not in sync with the followers. This is because it can lead to data loss.

To enable unclean leader election, you can set the `unclean.leader.election.enable` property to true. This will allow Kafka to elect a leader even if the leader is not in sync with the followers. But this can lead to data loss. Because the leader may have old data that is not in sync with the followers.