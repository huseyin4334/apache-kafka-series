# Multiple Brokers, Multiple Controllers And Schema Registry
[docker-file](./docker-sources/docker-compose.yml)

We have 3 controller, 3 brokers and 1 schema registry. Cluster adjusted in Kraft mode.

> PLAINTEXT, PLAINTEXT_HOST means that we are not using SSL. We are using plain text.
> But generally, PLAINTEXT is used for internal communication. PLAINTEXT_HOST is used for external communication.
> CONTROLLER means that we are using a controller protocol.
> <protocol>://<host>:<port>

- kafka_process_roles: controller | broker
  - Controller role will start the kafka in kraft mode. And it will be controller.
- KAFKA_LISTENERS
  - This parameter is used to adjust to accept the incoming requests. But this definition is used for internal adjustment.
    - For example, We have a kafka broker container. When we set this parameter like `PLAINTEXT://:19092,PLAINTEXT_HOST://:9092`, It means that kafka will listen to all ip ranges from 19092 port and 9092 port. But this is not enough for external communication. We need to use `KAFKA_ADVERTISED_LISTENERS` parameter for external communication. ((`PLAINTEXT://0.0.0.0:19092`))
- KAFKA_ADVERTISED_LISTENERS
  - This parameter is used to external communication definition. It is used to communicate with the outside world.
  - PLAINTEXT://broker-3:19092,PLAINTEXT_HOST://localhost:49092
  - If come a request to broker-3:19092 or come from localhost:49092 (host machine, not insode of the container), we will accept it.
- KAFKA_CONTROLLER_QUORUM_VOTERS
  - This parameter is used to adjust the controller quorum voters. We have 3 controller. So, we need to adjust this parameter like `1@controller-1:9093,2@controller-2:9093,3@controller-3:9093`
  - <controller-id>@<controller-name>:<port>

When we create this cluster with docker compose, docker compose will create a network. And all containers will be connected to this network. So, they can communicate with each other. All ports can connectable from the same network containers. We don't have to open ports for internal communication. Also, in the same network docker provides a DNS server. So, we can use the container name as a host name. For example, we can use `broker-1` as a host name.

If we create these kafka brokers or controlers with normal docker run command without network mapping, we need to open ports for communication.

---

We can reach the bootsrap servers from any brokers like that;
```bash
./kafka-topics.sh --create --topic first_topic --partitions 3 --replication-factor 1 --bootstrap-server broker-1:19092,broker-2:19092,broker-3:19092
```

Also, we can reach the bootsrap servers from any external place like that;
```bash
./kafka-topics.sh --create --topic first_topic --partitions 3 --replication-factor 1 --bootstrap-server localhost:29092,localhost:39092,localhost:49092
```