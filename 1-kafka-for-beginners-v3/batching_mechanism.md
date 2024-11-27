# linger.ms & batch.size

**batch.size**: The maximum size of a batch. The default value is 16KB.

> Request is a batch of messages.

**max.in.flight.requests.per.connection**: This is the maximum number of unacknowledged requests the client will send on a single connection before blocking. The default value is 5.

For example, we have 50 messages. Our batch size is 10. Then, producer will send 1 batch (10 messages). If the ack is 0 or 1, The producer won't wait for acknowledge and will send other batch(request). Let's Assume, we didn't get ack or get error from the broker for 5 request. Anymore the producer won't send other requests. Because we didn't get ack for 5 requests and we reached the limit of `max.in.flight.requests.per.connection`.

---

- **linger.ms**: How long to wait until we send a batch. The batch size is 50. We have 10 messages. The producer will wait for 5 miliseconds to get more messages. If we don't get more messages, the producer will send the batch. The default value is 0.


---

High throughput values:
- compression.type = snappy
- batch.size = 32*1024 (32KB)
- linger.ms = 20 (20ms)


# max.block.ms & buffer.memory

**buffer.memory**: The total amount of memory available to the producer for buffering. If messages are sent faster than they can be delivered to the server, the producer will block for max.block.ms after which it will throw an exception.

The default value is 32MB.

**max.block.ms**: The maximum time to block the producer for free space in the buffer. The default value is 60,000ms (60 seconds). After this time, buffer memory is full, and the producer will throw an exception.

If we get an exception, we should control our broker. Because the broker can't handle the producer's request maybe.
Or Maybe, we should increase the buffer.memory.