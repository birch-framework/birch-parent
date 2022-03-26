# Birch Framework Starter
Starter project for Birch Framework.

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
      <version>1.3.1</version>
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
            <version>1.3.1</version>
            <scope>import</scope>
            <type>pom</type>
         </dependency>
      </dependencies>
   </dependencyManagement>
```

When `birch-starter` is configured as the parent of an aggregator Maven project, its modules can then define dependencies on modules of Birch Framework
without specifying the `<version>` element.  For example:
```xml
<dependencies>
   <dependency>
      <groupId>org.birchframework</groupId>
      <artifactId>birch-common</artifactId>
   </dependency>
</dependencies>
```