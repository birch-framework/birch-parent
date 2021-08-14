[comment]: <> ([![Build Status]&#40;https://cicd.birchframework.org/buildStatus/icon?job=Birch/master&style=plastic&#41;]&#40;https://cicd.birchframework.org/job/Birch/job/master/&#41;)

[comment]: <> ([![Quality Gate]&#40;https://sonar.birchframework.org/api/project_badges/measure?metric=alert_status&project=org.birchframework:birch-parent&#41;]&#40;https://sonar.birchframework.org/dashboard?id=org.birchframework:birch-parent&#41;)
[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-white.svg)](https://sonarcloud.io/dashboard?id=org.birchframework%3Abirch-parent)

[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=org.birchframework%3Abirch-parent&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=org.birchframework%3Abirch-parent)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=org.birchframework%3Abirch-parent&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.birchframework%3Abirch-parent)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=org.birchframework%3Abirch-parent&metric=bugs)](https://sonarcloud.io/dashboard?id=org.birchframework%3Abirch-parent)
[![Maven Central](https://img.shields.io/maven-central/v/org.birchframework/birch-parent.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22org.birchframework%22)
[![javadoc](https://javadoc.io/badge2/org.birchframework/birch-parent/javadoc.svg)](https://javadoc.io/doc/org.birchframework)
# Birch Parent: Accelerator for Microservices Development
Accelerator framework to build Microservices based on Spring Boot

# Overview
The Birch Framework can be used as a parent module in any Maven project to provide a quick solution to build microservices.  The stack is based on the
following dependencies (defined in module):
* Spring Boot (birch-parent)
* Spring Actuator (birch-common)
* Spring Cloud Config (birch-common)
* Logback (birch-common)
* Lombok (birch-common)
* Orika (birch-common)
* Google Reflections (birch-common)
* Apache CXF JAX-RS (birch-rest-jaxrs)
* Open API (birch-rest-jaxrs)
* Apache Kafka (birch-kakfa-utils, birch-spring-kafka)
* Spring Security OpenID Connect (OIDC) (birch-security-oauth-spring)

Birch Framework also provides support for messaging bridges between the following JMS providers and Apache Kafka:

* Apache ActiveMQ
* IBM MQ
* Tibco EMS

NOTE: the aforementioned product dependencies must be included when using the bridge module.  They are not included by the Birch Framework.

Bridge stack uses the following dependencies:
* Apache Camel (birch-bridge-jms-kafka)

# Usage
There are 2 ways of using Birch Framework:
* Parent POM
* Import

**In the examples below, replace the contents of the `<version>` element with the desired version of Birch Framework.**
## Parent POM
The easiest way to use Birch Framework is to use it as a starter parent project in the project top level Maven POM, as follows:
```xml
   <parent>
      <groupId>org.birchframework</groupId>
      <artifactId>birch-starter</artifactId>
      <version>1.0.0</version>
   </parent>
```
## Import
Another way of using Birch Framework is to include it as a module dependency in the top level Maven POM, as follows:
```xml
   <dependencyManagement>
      <dependencies>
         <dependency>
            <groupId>org.birchframework</groupId>
            <artifactId>birch-starter</artifactId>
            <version>1.0.0</version>
            <scope>import</scope>
         </dependency>
      </dependencies>
   </dependencyManagement>
```
# Developer's Guide

# Git Repository
## Cloning the full repository
Run the following command to clone the repository for the first time:

    git clone https://github.com/birch/birch-parent.git

# Build
Execute the following from the root of the project (`birch-parent`):

    mvn clean install

# Release

**Releases can only be executed by Release admins.**

## Prepare local environment
### Git

Create links to the `post-checkout` script so that your new branches get the correct build status badges.
#### Windows
Run the following in the Windows Command Prompt **from the repository root (`birch-parent`)**:

    mklink .git\hooks\post-checkout %cd%\scripts\post-checkout

`git-bash` is required for this script to function properly.  All Git operations must be executed using `git-bash` for Windows.
#### UNIX
Run the following in a shell prompt **from the repository root (`birch-parent`)**:

    ln -s ${PWD}/scripts/post-checkout -t .git/hooks

## Creating the release
In order to create a release:
1. Ensure all local changes are committed and merged into master before running the release, as not doing so will cause the Maven release to fail.
2. Run the following script from the root of the project (`birch-parent`):
```
   bash scripts/release
```
3. Accept all defaults by pressing `Enter` for each prompt, except if creating a release with a minor and/or major version change, then enter the new version number.

   **Do not deviate from default tag naming convention of `birch-parent-<version>` where `<version>` is in the format `<major>.<minor>.<release>`**

4. Release will be committed to `release/<version>` branch and pushed to the repository.  When CI/CD discovers this new release branch, it will publish it to Maven Central.
