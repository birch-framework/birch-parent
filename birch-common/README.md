# Birch Common
Common components of the Birch Framework that accelerate Spring Boot based microservices development

# Usage
To include `birch-common` as a dependency to any Maven project or module, add the following as a dependency in its Maven POM file:
```xml
   <dependency>
      <groupId>org.birchframework</groupId>
      <artifactId>birch-common</artifactId>
      <version>1.0.0</version>
   </dependency>
```
**Replace the contents of the `<version>` element with the desired version of Birch Framework.**

# Maven Repository

If not already done so in the parent Maven POM, don't forget to tell Maven where to obtain Birch Framework:
```xml
   <repositories>
      <repository>
         <id>birch-releases</id>
         <name>Birch Framework Releases</name>
         <url>https://repo.maven.org/repo2</url>
         <snapshots>
            <enabled>false</enabled>
         </snapshots>
         <releases>
            <enabled>true</enabled>
         </releases>
      </repository>
   </repositories>
```