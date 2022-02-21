[![javadoc](https://javadoc.io/badge2/org.birchframework/birch-bridge-jms-kafka/javadoc.svg)](https://javadoc.io/doc/org.birchframework/birch-bridge-jms-kafka)
# Birch Framework JMS/Kafka Bridge
Provides auto-configurable bridges between the following JMS provides and Kafka:

* Apache ActiveMQ
* IBM MQ
* Tibco EMS


# Usage
1. To include `birch-jms-kafka-bridge` as a dependency to any Maven project or module, add the following as a dependency in its Maven POM file:
    ```xml
       <dependency>
          <groupId>org.birchframework</groupId>
          <artifactId>birch-bridge-jms-kafka</artifactId>
          <version>${birch.version}</version>
       </dependency>
    ``` 
 
2. Annotate your Spring Boot application main class with `EnableJMSKafkaBridge`
3. Apply the configurations as stated in [`BridgeAutoConfiguration`](https://javadoc.io/doc/org.birchframework/birch-bridge-jms-kafka/latest/org/birchframework/bridge/BridgeAutoConfiguration.html) 

# Other Dependencies
You must also include exactly one of the dependencies in the following sections.

## Apache ActiveMQ

```xml
   <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-activemq</artifactId>
   </dependency>
```

## IBM MQ

```xml
   <dependency>
      <groupId>com.ibm.mq</groupId>
      <artifactId>mq-jms-spring-boot-starter</artifactId>
      <version>2.5.3</version>
   </dependency>
```

**Replace the contents of the `<version>` element with the version of IBM MQ Spring Boot Starter that matches the version of Spring Boot.**

## Tibco EMS

Tibco has not published a Maven project on Maven Central for the EMS client libraries.  You must find other means of adding the following libraries to your
classpath:

* `tibcrypt.jar`
* `tibjms.jar`

# Configuration

## Apache ActiveMQ
Refer to the [Apache Camel ActiveMQ component](https://camel.apache.org/components/3.15.x/activemq-component.html#_spring_boot_auto_configuration) configuration documentation to configure bridges that use ActiveMQ as source or sink.

## IBM MQ
Refer to the [IBM MQ Spring support](https://github.com/ibm-messaging/mq-jms-spring#configuration-options) documentation to configure bridges that use IBM MQ as source or sink.

## Tibco EMS
Add the following dependency for EMS Spring auto-configuration:
```xml
<dependency>
   <groupId>org.birchframework</groupId>
   <artifactId>birch-ems-support</artifactId>
</dependency>
```

Refer to [`EMSAutoConfiguration`](https://javadoc.io/doc/org.birchframework/birch-ems-support/latest/org/birchframework/ems/EMSAutoConfiguration.html) Javadocs 
to auto-conffigure EMS bridges that use it as source or sink.

## Apache Kafka
Refer to the [Apache Kafka component](https://camel.apache.org/components/3.15.x/kafka-component.html#_spring_boot_auto_configuration) documentation to configure Kafka for bridges.

# Running Local Kafka Instance
Use the provided `birch-parent/scripts/docker-compose-kafka.yml` file to setup a local Kafka instances.
From **within** the `birch-parent/scripts` directory:
1. Edit the `.env` file in the same folder
   * Replace the value of `KAFKA_ADVERTISED_HOST_NAME` with the hostname or IP address of the Docker host
2. Run:
    ```shell
    sudo addgroup --uid 1001 kafka
    sudo adduser --uid 1001 --ingroup kafka --home /home/kafka kafka
    sudo chown kafka:kafka ~kafka
    docker-compose -f docker-compose-kafka.yml --env-file .env -d up
    ```
3. Install Kafka CLI by following [these instructions](https://dzone.com/articles/apache-kafka-basic-setup-and-usage-with-command-li)
4. Test this Kafka instance by running
    ```shell
    kafka-topics.sh --bootstrap-server localhost:9092 --list
    ```
    If there are no errors and only a newline is printed, then the Kafka instance is successfully started.
