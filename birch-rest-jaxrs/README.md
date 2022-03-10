[![javadoc](https://javadoc.io/badge2/org.birchframework/birch-rest-jaxrs/javadoc.svg)](https://javadoc.io/doc/org.birchframework/birch-rest-jaxrs)
# Birch Framework REST - JAX-RS
Provides all the necessary dependenices to build JAX-RS-based REST APIs.  Both client and service APIs are supported.  Also provides a utility (`Responses`) of useful
routines to hook into processing of RESTful API verbs and extraction of bodies for API consumers.  Finally, a thin error reporting framework is included with which
the `Responses` utility can be used to streamline RESTful APIs error reporting.

# Usage
## Maven Dependency
Include `birch-rest-jaxrs` as a dependency to a microservice's Maven project by adding the following as a dependency in its Maven POM file:
```xml
<dependency>
   <groupId>org.birchframework</groupId>
   <artifactId>birch-rest-jaxrs</artifactId>
</dependency>
```

# Framework
## Error Reporting
To create a streamlined error reporting experience that standardizes RESTful APIs error reporting for microserives across the microservices ecosystem, create
exactly one `enum` that implements the `ErrorCode` interface.  For example:
```java
public enum LogisticsComponent implements Component {
   ORDER,
   PAYMENT,
   SHIPPING
}

@Getter
@RequiredArgsConstructor
public enum LogisticsErrorCode implements ErrorCode<LogisticsErrorCode> {
   L10000(LogisticsComponent.ORDER, "Order not found"),
   L10010(LogisticsComponent.ORDER, "Invlid order ID"),
   L10020(LogisticsComponent.ORDER, "Unable to save order");

   private final int code = Integer.parseInt(this.name().substring(1));

   private final LogisticsComponent component;
   private final String             description;

   @Override
   public ErrorResponse<LogisticsErrorCode> errorResponse() {
      //...
   }
}
```
When the microservice starts, the `Responses` utility class (discussed below) will seek this implementation and use it for translating payloads of APIs that 
include serialized versions of errors reported by the service.  The following demonstrates the use case:
```java
@JsonInclude(NON_NULL)
@Data
@NoArgsConstructor
public class Order implements Serializable {

   private final UUID   id;
   private final String companyName;
   private final String recipientName;
   private final String recipientAddress1;
   private final String recipientAddress2;
   private final String city;
   private final String stateProvinceCode;
   private final String postalCode;
   private final String countryCode;
   private final String status;
}

@Path("/orders")
@Produces(APPLICATION_JSON)
public interface OrderAPI {

   @Path("/findByID/{id}")
   @GET
   Response findByID(@PathParam("id") final UUID theOrderID);

   @Path("/save")
   @POST
   @Consumes(APPLICATION_JSON)
   Response save(final Order theOrder);
}

@Service
@RequiredArgsConstructor
public class OrderAPIImpl implements OrderAPI {
   
   private final OrderRepository orderRepoistory;
   
   public Response findByID(final UUID theOrderID) {
      final Optional<Order> anOrderOptional = orderRepoistory.findById(theOrderID);
      return anOrderOptional.map(order -> Response.ok(order).build())
                            .orElse(Response.status(NOT_FOUND).entity(LogisticsErrorCode.L10000).build());
   }

   public Response save(final Order theOrder) {
      final Optional<Order> anOrderOptional = orderRepoistory.save(theOrder);
      return anOrderOptional.map(order -> Response.ok(order).build())
                            .orElse(Response.status(NOT_MODIFIED).entity(LogisticsErrorCode.L10020).build());
   }
}
```

## JAX-RS Resource Auto-proxy
The annotation [`AutoProxy`](https://javadoc.io/doc/org.birchframework/birch-rest-jaxrs/latest/org/birchframework/framework/cxf/AutoProxy.html) can be 
applied to any JAX-RS resource interface in order to auto-create a CXF REST client proxy bean at application boot time,
but only if another bean of the same type is not already created in the Spring application context.  Consider the following use case:
```yaml
# application.yml
api:
  coindesk:
    base-url: https://api.coindesk.com/v1
```
```java
@Path("/bpi")
@Produces(APPLICATION_JSON)
@AutoProxy(baseURI = "${api.coindesk.base-url}")
public interface CoinDeskResource {

   @Path("/currentprice.json")
   @GET
   Response currentPrice();
}

@SpringBootApplication
@EnableREST
public class Application {

   public static void main(final String... theArgs) {
      SpringApplication.run(Application.class, theArgs);
   }
}
```
The `@AutoProxy` annotation is provided with a `baseURI` which is read from the Spring configuration.  At application boot time, the a proxy bean of
type `CoinDeskResource` is created and made available in the Spring Application context to be
auto-wired into any other bean that depends on it.  As demonstrated above, with this mechanism no manual `@Bean` annotated methods are needed 
within the `@SpringBootApplication` annotated class to create the JAX-RS proxy for `CoinDeskResource`.

[`@EnableREST`](https://javadoc.io/doc/org.birchframework/birch-rest-jaxrs/latest/org/birchframework/framework/cxf/EnableREST.html) is a marker annotation 
that imports several JAX-RS related auto-configurations.  It is a convenient way of auto-configuring CXF to seek JAX-RS annotated resources and Spring
configurations, associating them with the [`SpringBus`](https://cxf.apache.org/javadoc/latest/org/apache/cxf/bus/spring/SpringBus.html).


## Span
The span portion of the `birch-rest-jarx` provides the means to propogate correlation ID and client locale from JAX-RS clients to servers and beyond.  This is
made possible by default via [`@EnableREST`](https://javadoc.io/doc/org.birchframework/birch-rest-jaxrs/latest/org/birchframework/framework/cxf/EnableREST.html) 
annotation when placed on a Spring Boot application class.  In the previous examples, the auto-configurations imported by
`@EnableREST` will ensure proper client filters and interceptors are configured for REST endpoint clients.  However, if 
[`@AutoProxy`](https://javadoc.io/doc/org.birchframework/birch-rest-jaxrs/latest/org/birchframework/framework/cxf/AutoProxy.html) is not being utilized and
JAX-RS client proxies are being manually created, then the client filter must be injected into the proxy as follows:
```java
@SpringBootApplication
@EnableREST
public class Application {

   public static void main(final String... theArgs) {
      SpringApplication.run(Application.class, theArgs);
   }

   @Bean
   OrderAPI orderAPI(@Value("${logistics.servers.order.base-uri:http://localhost:8080/api}") final String theBaseURI,
                     final ResourceClientRequestFilter theResourceClientRequestFilter) {
      return JAXRSClientFactory.create(theBaseURI.toString(), OrderAPI.class, List.of(theResourceClientRequestFilter));
   }
}
```
In the above example, the [`create`](https://cxf.apache.org/javadoc/latest/org/apache/cxf/jaxrs/client/JAXRSClientFactory.html#create-java.lang.String-java.lang.Class-java.util.List-) 
method adds the auto-configured client filter to the client proxy's list of providers.  On subsequent method
calls to the proxy client, the filter will inject the span information into the request header.  On the server side, a filter picks up the headers transmitted by
this client's request, and injects them into a thread-local that is accessible via `SpanHeadersContainerBean`.

Exactly one [`Spannable`](https://javadoc.io/doc/org.birchframework/birch-common/latest/org/birchframework/dto/Spannable.html)
implementation is required in order to inform the Span farmework which request headers it must propogate throughout the REST API call chain.
```java
public enum LogisticsSpannable implements Spannable<LogisticsSpannable> {
   correlationID,
   locale;
}
```
If a UI client is transmitting the headers given the same keys as defined by the `Spannable` implementation, then they too will be injected into the server's thread
that is processing the request.

An added benefit of the Span framework is the injection of `Spannable` implementation's request header keys' values into SLF4J's 
[`MDC`](https://www.slf4j.org/api/org/slf4j/MDC.html) facility.
# Utilities
## `Responses`
The [`Responses`](https://javadoc.io/doc/org.birchframework/birch-rest-jaxrs/latest/org/birchframework/framework/jaxrs/Responses.html) utility class
provides methods to evaluate [`Response`](https://javaee.github.io/javaee-spec/javadocs/javax/ws/rs/core/Response.html) objects, which are normally returned 
by JAX-RS-based APIs, in a functional manner.  Using this convention makes software maintenance easier as source code for calling an API and consuming what it returns 
are co-located.  As an example, consider the following classes:
```java
@SpringBootApplication
public class Application {
   public static void main(final String... theArgs) {
      SpringApplication.run(Application.class, theArgs);
   }

   @Bean          // Here we could have used @AutoProxy instead of this method, but in this example we are explicitly creating the bean
   OrderAPI orderAPI(@Value("${logistics.servers.order.base-uri:http://localhost:8080/api}") final String theBaseURI) {
      return JAXRSClientFactory.create(theBaseURI, OrderAPI.class);
   }
}
```
In the following example, the API which returns a complex object is being called via HTTP `GET` and the payload is being ingested:
```java
@RequiredArgsConstructor
@Slf4j
public class FulfillmentManager {

   private final OrderAPI orderAPI;

   public String shipOrder(final UUID theOrderID) {
      final var anAtomicOrderStatus = new AtomicReference<Order>();
      Responses.of(this.orderAPI.findByID(theOrderID)).ifOKOrElse(
         Order.class,
         order -> {
            log.debug("Recipient name: {}", order.getRecipientName());
            order.setStatus("SHIPPED");
            // ... other order object manipulations
            final var aSavedOrder = orderAPI.save(order);
            anAtomicOrder.set(order);
         }, 
         errorCode -> log.error("Order retrieval failed; {}:{}; error description: {}", 
                                errorCode.getComponent(), errorCode.getCode(), errorCode.getDescription())
      );
      return anAtomicOrder.get();
   }
}
```