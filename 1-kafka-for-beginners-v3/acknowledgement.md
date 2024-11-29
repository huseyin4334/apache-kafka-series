# Acknowledgement
`acks` parameter can be set to `0`, `1`, or `all`. 
If it is set to 0, the producer will not wait for the leader broker to acknowledge the message. It will send the message and continue to send the next message. If it is set to 1, the producer will wait for the leader broker to acknowledge the message. If it is set to all, the producer will wait for all in-sync replicas to acknowledge the message.

The default is `1` in v1.0 to v2.8

The default is `all` in v3.0+ (`-1` is same as `all`)
We can change the minimum number of in-sync replicas using the `min.insync.replicas` parameter. The default is `1`. This uses for the `acks=all` configuration. If we set it to `2`, the producer will wait for at least 2 in-sync replicas to acknowledge the message.


The most popular configuration is `acks=all` and `min.insync.replicas=2`. This option helps to durability and avalialability.

---

When we get `NOT_ENOUGH_REPLICAS` error, it means that the producer is waiting for the in-sync replicas to acknowledge the message. But the in-sync replicas are not enough. So, the producer cannot send the message. And the producer will retry. We have a property for it. `retries` property. The default value is `2147483647`. It means that the producer will retry until the message is sent. If we set it to `0`, the producer will not retry. If we set it to `1`.

kafka v2.0<= `retries` default value is `0`
katka v2.0+ `retries` default value is `2147483647`

And the `retry.backoff.ms` property is used to adjust the retry time. The default value is `100`. It means that the producer will wait 100 ms before retrying.

We have some timeout properties and sections of them;

- Send() Section;
  - mix.block.ms
- Batching Section;
  - linger.ms
- Await send Section And Retries;
  - retry.backoff.ms
- Inflight Section;
  - request.timeout.ms

`delivery.timeout.ms` is used to adjust the timeout for the producer. The default value is `120000`. It means that the producer will wait 120000 ms before timeout.
Records will be failed after `delivery.timeout.ms` if they are not acknowledged.

delivery.timeout.ms >= linger.ms + request.timeout.ms + retry.backoff.ms

---

If the ordering is important for us, we can set the `max.in.flight.requests.per.connection` parameter to `1`. The default value is `5`. It means that the producer can send 5 messages at the same time. But if we set it to `1`, the producer will send the messages one by one. It means that the producer will wait for the acknowledgment of the first message before sending the second message.

But after v1.0+, The idempotent producer is enabled by default. It means that the producer will send the messages one by one. It means that the producer will wait for the acknowledgment of the first message before sending the second message. So, we don't have to set the `max.in.flight.requests.per.connection` parameter to `1`.


# Idempotent Producer
For example;
We send a request, producer sent it and kafka broker commit and producer got an ack.
We send a request too, producer sent it and kafka broker commit but we didn't get an ack. Then the producer will retry. Broker will commit again (Will be dublicate). And send an ack.

But you see we set a dublicate message. When we use idempotent producer, broker will detect a dulicate message and it will send an ack. But it will not commit the message again.

This is default after v3.0

# V3.0+ Producer Configuration
- retries=Integer.MAX_VALUE
- max.in.flight.requests.per.connection=5
- acks=all
- enable.idempotence=true
- delivery.timeout.ms=120000 (2 minutes)