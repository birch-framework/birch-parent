[![javadoc](https://javadoc.io/badge2/org.birchframework/birch-bridge-jms-kafka/javadoc.svg)](https://javadoc.io/doc/org.birchframework/birch-bridge-jms-kafka)
# Birth Framework JMS/Kafka Bridge
Provides auto-configurable bridges between the following JMS provides and Kafka:

* Apache ActiveMQ
* IBM MQ
* Tibco EMS


# Usage
To include `birch-jms-kafka-bridge` as a dependency to any Maven project or module, add the following as a dependency in its Maven POM file:
```xml
   <dependency>
      <groupId>org.birchframework</groupId>
      <artifactId>birch-jms-kafka-bridge</artifactId>
      <version>1.0.0</version>
   </dependency>
```
**Replace the contents of the `<version>` element with the desired version of Birch Framework.**

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

Tibco has not publised a Maven project on Maven Central for the EMS client libraries.  You must find other means of adding the following libraries to your
classpath:

* `tibcrypt.jar`
* `tibjms.jar`

and add the following dependency for EMS auto-configuration:
```xml
<dependency>
   <groupId>org.birchframework</groupId>
   <artifactId>birch-ems-support</artifactId>
</dependency>
```
Once the above steps are complete, refer to `org.birchframework.ems.EMSAutoConfiguration` [Javadocs](https://www.javadoc.io/doc/org.birchframework/birch-ems-support/1.0.0/index.html) 
for auto-configuration of EMS.