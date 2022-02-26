[![javadoc](https://javadoc.io/badge2/org.birchframework/birch-common/javadoc.svg)](https://javadoc.io/doc/org.birchframework/birch-common)
# Birch Common
Common components of the Birch Framework that accelerate Spring Boot based microservices development

# Usage
To include `birch-common` as a dependency to any Maven project or module, add the following as a dependency in its Maven POM file:
```xml
   <dependency>
      <groupId>org.birchframework</groupId>
      <artifactId>birch-common</artifactId>
      <version>${birch.version}</version>
   </dependency>
```

# Libraries and Utilities

## Java POJO Regex Binding
The [`Parser`](https://javadoc.io/doc/org.birchframework/birch-common/latest/org/birchframework/framework/regex/Parser.html) class provides a mechanism for 
parse a regular expression producing a POJO as a result of binding Regex capture groups to POJO properties. The following is an example of such binding:
```java
@RegexBinding("Age:\\s+(\\d+)\\s+\\|\\s+Last Name:\\s+(\\w*)\\s+\\|\\s+First Name:(\\s+)(\\w*)\\s+\\|\\s+Deceased:\\s+(y|n|Y|N|t|f|T|F)\\s+\\|\\s+(.*)")
@Getter
public class DemographicInfo {

   @CaptureGroup(1)
   private int age;

   @CaptureGroup(2)
   private String lastName;

   @CaptureGroup(5)
   private boolean deceased;

   @CaptureGroup(4)
   private String firstName;

   @CaptureGroup(6)
   private Type type;

   public enum Type {
      ROCK_STAR,
      CHARACTER
   }
}
```
Given a string that matches the [`@RegexBinding`](https://javadoc.io/doc/org.birchframework/birch-common/latest/org/birchframework/framework/regex/RegexBinding.html), 
the `Parser.parse(String)` 
method will produce an instance of `DemographicInfo` with capture groups defined by the `@RegexBiding` are properly mapped to their respective values in 
properties annotated with [`@CaptureGroup`](https://javadoc.io/doc/org.birchframework/birch-common/latest/org/birchframework/framework/regex/CaptureGroup.html).

The following code initializes the Parser for the above class:
```java
final var parser = Parser.of(DemographicInfo.class);
final var objects = parser.parse("Age: 27 | Last Name: Stardust  | First Name: Ziggy    | Deceased: Y | ROCK_STAR");
```
In the above example, `objects` is a `List<?>` containing one instance of `DemographicInfo`.

Multiple classes can be provided to the `Parser.of(Class<?>...)`, for which when parsing each line of input, one instance of the first matched Regex binding POJO
will be returned in the results.

A common use case is to parse a file split into lines of text and feed those lines as a stream to the overloaded `Parser.parse(Stream<String>)` method which 
returns a stream.  This pattern is very memory efficient as it returns a stream that can then be used to evaluate the values in real-time.  The following is 
such an example:
```java
final var objects = parser.parse(Files.lines(Paths.get("path/to/population-demographics.txt")));
objects.forEach(pojo -> {
   if (pojo instanceof DemographicInfo) {
      final var demographic = (DemographicInfo) pojo;
      // Here place logic to use consume the 'demographic' pojo
   }
});
```

## Rate Gauge

The class [`RateGauge`](https://javadoc.io/doc/org.birchframework/birch-common/latest/org/birchframework/framework/metric/RateGauge.html) provides the means to
calculate a rate via the Micrometer framework.  A rate is defined as a count in a time period, for example requests/second, message/minute.
To create a `RateGauge`, use its builder as in the following example:
```java
final var rateGauge = RateGauge.builder()
                               .withName("message-rate")
                               .withDescription("Incoming message rate")
                               .withTags(Tag.of("inbound-queue", "in-request-queue"))
                               .withRegistry(meterRegistry)
                               .register();
```
where `meterRegistry` is a reference to a Spring Actuator bean.  Then call the [`rateGauge.increment()`](https://javadoc.io/static/org.birchframework/birch-common/1.1.2/org/birchframework/framework/metric/RateGauge.html#increment()) 
method in your process to demonstrate one unit of processing. When the APM that consumes Actuator-exported metrics requests a sampling of this metric, this
RateGauge instance calculates units per second and returns the value to the APM.  A custom value can be provided by calling `builder().valueFunction(BiFunction<Long, Long, Double>` 
in order to override the default `count / second` calculation.