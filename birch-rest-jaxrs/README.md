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
   <version>${birch.version}</version>
</dependency>
```
If `birch.version` is not specified within the Maven project hierarchy, then replace it with the desired version.

# Error Reporting Framework
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

   @Bean
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