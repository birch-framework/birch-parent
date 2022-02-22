[![javadoc](https://javadoc.io/badge2/org.birchframework/birch-ems-support/javadoc.svg)](https://javadoc.io/doc/org.birchframework/birch-ems-support)
# Birch Framework Tibco EMS Support
Provides a Spring auto-configuration to configure JMS connection factories for Tibco EMS.  This module requires the following EMS dependencies.

# Usage
## Maven Dependency
Tibco has not published a Maven project on Maven Central for the EMS client libraries.  You must find other means of adding the following libraries to your
classpath:

* `tibcrypt.jar`
* `tibjms.jar`

Include `birch-ems-support` as a dependency to a microservice's Maven project by adding the following as a dependency in its Maven POM file:
```xml
<dependency>
   <groupId>org.birchframework</groupId>
   <artifactId>birch-ems-support</artifactId>
</dependency>
```

# Configuration
Refer to [`EMSAutoConfiguration`](https://javadoc.io/doc/org.birchframework/birch-ems-support/latest/org/birchframework/ems/EMSAutoConfiguration.html) Javadocs 
for documentation on how to auto-configure microservices that use EMS connection factories as JMS producers or consumers.
