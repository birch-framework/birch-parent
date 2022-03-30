[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-black.svg)](https://sonarcloud.io/dashboard?id=org.birchframework%3Abirch-parent)

[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=org.birchframework%3Abirch-parent&metric=bugs)](https://sonarcloud.io/dashboard?id=org.birchframework%3Abirch-parent)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=org.birchframework%3Abirch-parent&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=org.birchframework%3Abirch-parent)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=org.birchframework%3Abirch-parent&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=org.birchframework%3Abirch-parent)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=org.birchframework%3Abirch-parent&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=org.birchframework%3Abirch-parent)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=org.birchframework%3Abirch-parent&metric=security_rating)](https://sonarcloud.io/dashboard?id=org.birchframework%3Abirch-parent)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=org.birchframework%3Abirch-parent&metric=sqale_index)](https://sonarcloud.io/dashboard?id=org.birchframework%3Abirch-parent)

![Status Checks](https://img.shields.io/github/checks-status/birch-framework/birch-parent/HEAD?style=plastic)

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.birchframework/birch-parent.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22org.birchframework%22)
[![javadoc](https://javadoc.io/badge2/org.birchframework/birch-parent/javadoc.svg)](https://javadoc.io/doc/org.birchframework)
# Birch Framework: An Accelerator for Microservices Development
Accelerator framework to build Microservices based on Spring Boot

# Overview
The Birch Framework can be used as a parent module in any Maven project to provide a quick solution to build microservices.  The stack is based on the
following dependencies (defined in module):

* Spring Boot (birch-parent)
* Spring Actuator ([birch-common](birch-common/README.md))
* Spring Cloud Config ([birch-common](birch-common/README.md))
* Logback ([birch-common](birch-common/README.md))
* Lombok ([birch-common](birch-common/README.md))
* Orika ([birch-common](birch-common/README.md))
* Google Reflections ([birch-common](birch-common/README.md))
* Apache CXF JAX-RS ([birch-rest-jaxrs](birch-rest-jaxrs/README.md))
* Open API ([birch-rest-jaxrs](birch-rest-jaxrs/README.md))
* Apache Kafka ([birch-kakfa-utils](birch-kafka-utils/README.md), birch-spring-kafka)
* Spring Security OpenID Connect (OIDC) ([birch-security-oauth-spring](birch-security-oauth-spring/README.md))

Birch Framework also provides support for messaging bridges between the following JMS providers and Apache Kafka:

* Apache ActiveMQ
* IBM MQ
* Tibco EMS

NOTE: the aforementioned product dependencies must be included when using the bridge module.  They are not included by the Birch Framework.

Bridge stack uses the following dependencies:

* Apache Camel ([birch-bridge-jms-kafka](birch-bridge-jms-kafka/README.md))

# Usage
Refer to [birch-starter](birch-starter/README.md) documentation on how to include aggregator dependency of Birch Framework.

# Developer's Guide

# Build
Execute the following from the root of the project (`birch-parent`):

    mvn clean install
