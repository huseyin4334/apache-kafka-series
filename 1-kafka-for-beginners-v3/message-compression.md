# Compression
- Compression is a feature in Kafka that allows you to compress the messages before they are stored on the disk. This can save a lot of disk space and network bandwidth especially when you are dealing with large messages.
- The best practice is use compression for the messages that are larger than 1KB.

# Compression Types
- There are several compression types in Kafka;
  - `none`: No compression
  - `producer`: Compression is defined by the producer (default)
  - `gzip`: Good compression ratio
  - `snappy`: Good compression ratio and fast
  - `lz4`: Fast compression and decompression
  - `zstd`: Good compression ratio and fast

# Compression Configuration
- We can set the compression type using the `compression.type` parameter.
- The default value is `producer`.
- The broker takes compressed messages and save it to the disk without decompressing it.

The disadvantage of compression is that it increases the CPU usage in producer and consumer side due the compression and decopression time. So, we need to find a balance between the CPU usage and the network bandwidth.