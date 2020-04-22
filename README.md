* Build Helidon branch with se-messaging locally
```bash
git checkout -b se-messaging
mvn clean install -DskipTests
```
* Start ActiveMQ Artemis locally
```java
.../apache-artemis-2.11.0/bin$ ./artemis create kecbroker
.../apache-artemis-2.11.0/bin$ cd kecbroker/bin/
.../apache-artemis-2.11.0/bin/kecbroker/bin$ ./artemis-service start
```
Check out management console http://localhost:8161/

* Check configuration 
```yaml
mp:
  messaging:
    # Configure outgoing channels -> publishers
    outgoing:
      jms-incoming-channel:
        connector: smallrye-jms
        destination: kec-queue
        destination-type: queue
    # Configure incoming channels -> subscribers
    incoming:
      jms-outgoing-channel:
        connector: smallrye-jms
        destination: kec-queue
        destination-type: queue

# Connector configuration properties(common for all channels)
mp.messaging.connector.smallrye-jms:
  broker-uri: tcp://localhost:61616
```

* Start Helidon JMS POC
```bash
kec@vulcan:~/idp/ora/helidon-jms-poc/target$ java -jar helidon-jms-poc-se.jar 
2020.04.22 20:33:55 WARN org.apache.activemq.artemis.core.client Thread[main,5,main]: AMQ212076: Epoll is not available, please add to the classpath or configure useEpoll=false to remove this warning
[main] INFO io.smallrye.reactive.messaging.jms.JmsSource - Creating queue kec-queue
2020.04.22 20:33:56 WARN org.apache.activemq.artemis.core.client Thread[main,5,main]: AMQ212076: Epoll is not available, please add to the classpath or configure useEpoll=false to remove this warning
2020.04.22 20:33:56 INFO io.helidon.common.HelidonFeatures Thread[main,5,main]: Helidon SE 2.0.0-SNAPSHOT features: [Config, Messaging, WebServer]
2020.04.22 20:33:56 INFO io.helidon.webserver.NettyWebServer Thread[nioEventLoopGroup-2-1,10,main]: Channel '@default' started: [id: 0xdeab03f3, L:/0:0:0:0:0:0:0:0:8080]
WEB server is up! http://localhost:8080/send/HelloWorld
```

* Call send rest endpoint
```bash
curl http://localhost:8080/send/HelloWorld
```
* Check server output
```bash
Received message from JMS: HelloWorld
```



