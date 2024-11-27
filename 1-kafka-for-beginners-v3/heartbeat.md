# Heartbeat And Session Timeout
- **Heartbeat** is a signal sent by the consumer to the broker to indicate that the consumer is alive. It is used to ensure that the consumer's session stays active in the group. If the consumer dies without sending a heartbeat, the broker will consider the consumer dead and trigger a rebalance.

- **Session Timeout** is the time the broker will wait for a heartbeat from the consumer. If the broker does not receive a heartbeat within the session timeout, it will consider the consumer dead and set even lower to faster consumer rebalance.

`heartbeat.interval.ms` and `session.timeout.ms` are the two important configurations that control the heartbeat mechanism in Kafka. The `heartbeat.interval.ms` is the time between heartbeats, and the `session.timeout.ms` is the time the broker will wait for a heartbeat.

Default values:
- `heartbeat.interval.ms`: 3 seconds
- `session.timeout.ms`: 10 seconds before Kafka  3.0, 45 seconds after Kafka 3.0

> This mechanism used to detect consumer application being down.


# Consumer Poll Thread
- The consumer poll thread is responsible for fetching records from the Kafka broker.

`max.poll.interval.ms` is the configuration that controls the maximum time between two consecutive polls. If the consumer does not poll within this time, the broker will consider the consumer dead and trigger a rebalance.

Default value: 5 minutes

> This mechanism used to detect consumer application being stuck. This means consumer application is not able to process the records fast enough.


`max.poll.records` is the configuration that controls the maximum number of records returned in a single poll.

Default value: 500 records

We can change this value based on the processing capacity of the consumer application or our message size.

---

`fetch.min.bytes` is the configuration that controls the minimum number of bytes the broker will wait for before sending records to the consumer.

This helps us to improving throughput and decreasing request sizes. Latency will increase if we increase this value.

Default value: 1 byte

`fetch.max.wait.ms` is the configuration that controls the maximum time the broker will wait for the `fetch.min.bytes` before sending records to the consumer.

This configuration helps us the decrease latency. Latency will increase if we increase this value.

Default value: 500 ms

---

`max.partition.fetch.bytes` is the configuration that controls the maximum number of bytes the broker will return per partition.

This configuration helps us to control the maximum amount of data from a single partition.

Default value: 1 MB

`fetch.max.bytes` is the configuration that controls the maximum number of bytes the broker will return in a single fetch request.

If we have more memory available, we can increase this value to fetch more data in a single request.

Default value: 50 MB


# Consumer Read From Closest Replica
- The consumer can read from the closest replica to reduce latency.
- `client.rack` is the configuration that helps the consumer to read from the closest replica. (Consumer settings)
- `client.id` is the configuration that helps the consumer to read from the closest replica. (Broker settings)