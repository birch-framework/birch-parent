[![javadoc](https://javadoc.io/badge2/org.birchframework/birch-kafka-utils/javadoc.svg)](https://javadoc.io/doc/org.birchframework/birch-kafka-utils)
# Birch Framework Kafka Utils
Provides a REST endpoint and also exports Micrometer gauge for Kafka consumer lag for all topics on a configured broker.

# Usage
## Maven Dependency
Include `birch-kafka-utils` as a dependency to any Maven project or module, add the following as a dependency in its Maven POM file:
```xml
<dependency>
   <groupId>org.birchframework</groupId>
   <artifactId>birch-kafka-utils</artifactId>
   <version>${birch.version}</version>
</dependency>
```
If `birch.version` is not specified within the Maven project hierarchy, then replace it with the desired version. 

# Kafka Broker Configuration
Refer to [`KafkaAdminUtilsAutoConfiguration`](https://javadoc.io/doc/org.birchframework/birch-kafka-utils/latest/org/birchframework/framework/kafka/KafkaAdminUtilsAutoConfiguration.html) Javadocs for Kafka configuration documentation.

## REST Endpoint 
1. Include `birch-kafka-utils` as a dependency to the microservice Maven project as described above 
2. Ensure `KafkaAdminUtilsAutoConfiguration` is picked up by the Spring Boot application configuration
3. When the Spring Boot application loads, the HTTP `GET` endpoint will be exposed at the path: `/kafka/topicLags`

## Micrometer Gauge
1. Include `birch-kafka-utils` as a dependency to the microservice Maven project as describe above
2. Include the Spring Actuator dependency to the same Maven project
3. Ensure `KafkaAdminUtilsAutoConfiguration` is picked up by the Spring Boot application configuration
4. When the Spring Boot application loads, gauges will be reported by Spring Actuator to the `birch.kafka.consumer.lag` metric, wherein topics are tagged by their names and values represent the consumer lag

The Micrometer gauge will be exported to the the configured exporter specified for Spring Actuator.

You may **optionally** configure the following in the microservice's `application.yml`:
```yaml
 birch:
   kafka:
     admin:
       sample-interval-ms: 5_000              # Scheduled milliseconds interval of sampling consumer lags; defaults to every 5 seconds 
       re-register-interval-ms: 21_600_000    # Scheduled milliseconds interval of gauge re-registration in order to tag any new topics; defaults to every 6 hours
```
