# simple-redis-server

In this project I have tried to implement a simple persisent in-memory cache. Simple-redis-server consists of a LRU cache which persisted periodcally
on the disk using Write-Ahead-Log technique. Currently the cache size is 3 (for test purposes), which can be altered by changing method ```initDbs``` in
class ```BaseAbstractRedisServer``` . The cache is persisted every 100ms and the server is able to recover gracefully from any sudden or shutdown signals.

## How to run
Please run the following command in your terminal to start an instance of simple-redis-server:

```
cd $PROJECT_DIR
./gradlew startServer --console=plain
```

NOTE: This application runs on port 6789 by default. You can change this port in class ```Server```.

## Tests
Test directory contains unit tests to ensure proper functionality of ```RedisServer```, ```Cache``` and the newly impelemented ```DoublyLinkedList```.

## Client
You can run a client application which can communicate with a simple-redis-server instance, by following instruction in this repository:

[simple-redis-client](https://github.com/arman-yekkehkhani/simple-redis-client)
