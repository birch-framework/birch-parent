[![javadoc](https://javadoc.io/badge2/org.birchframework/birch-security-oauth-spring/javadoc.svg)](https://javadoc.io/doc/org.birchframework/birch-security-oauth-spring)
# Birch Security Spring OAuth2 Support 
Extends the Spring Security to support multiple OAuth2 realms for JAX-RS-based microservices.

# Usage
## Maven Dependency

Include `birch-security-oauth-spring` as a dependency to a microservice's Maven project by adding the following as a dependency in its Maven POM file:
```xml
<dependency>
   <groupId>org.birchframework</groupId>
   <artifactId>birch-security-oauth-spring</artifactId>
</dependency>
```

# Configuration
Refer to [`OAuth2ResourceServerAutoConfiguration`](https://javadoc.io/doc/org.birchframework/birch-security-oauth-spring/latest/org/birchframework/security/oauth2/OAuth2ResourceServerAutoConfiguration.html) 
Javadocs for documentation on how to auto-configure security for microservices using this module.

1. Annotate the Spring Boot application with [`@EnableOAuth2ResourceServerSecurity`](https://javadoc.io/doc/org.birchframework/birch-security-oauth-spring/latest/org/birchframework/security/oauth2/EnableOAuth2ResourceServerSecurity.html) 
in order to signal Spring Boot to auto-configure `birch-security-oauth-spring` for the microservice's JAX-RS endpoints
2. Provide a concrete class that implements the [`UserDetails`](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/core/userdetails/UserDetails.html) interface
3. Provide a concrete class that implements the [`GrantedAuthoritiesBuilder`](https://javadoc.io/doc/org.birchframework/birch-security-oauth-spring/latest/org/birchframework/security/oauth2/GrantedAuthoritiesBuilder.html)
interface, and reference it in the application configuration by setting the property `birch.security.oauth2.realms.<realm-name>.granted-authorities-builder`
4. Provide a Spring bean that implements the [`UserDetailsService`](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/core/userdetails/UserDetailsService.html) 
interface, returning an instance of the `UserDetails` implementation from step 2 above
5. Configure Birch Security OAuth2 configurations as per [`OAuth2ResourceServerAutoConfiguration`](https://javadoc.io/doc/org.birchframework/birch-security-oauth-spring/latest/org/birchframework/security/oauth2/OAuth2ResourceServerAutoConfiguration.html) Javadocs
6. In order to provide method-level security
   1. Annotate the Spring Application with [`@EnableGlobalMethodSecurity(prePostEnabled = true)`](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/config/annotation/method/configuration/EnableGlobalMethodSecurity.html)
   2. Provide a concrete class that implements the [`PermissionEvaluator`](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/access/PermissionEvaluator.html) interface
   3. Extend the Spring Boot application from [`GlobalMethodSecurityConfiguration`](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/config/annotation/method/configuration/GlobalMethodSecurityConfiguration.html); implement the interface's method as follows:
      ```java
      @Override
      protected MethodSecurityExpressionHandler createExpressionHandler() {
         final var anExpressionHandler = new DefaultMethodSecurityExpressionHandler();
         anExpressionHandler.setPermissionEvaluator(new ConcretePermissionEvaluator());  // implemented in step 6.ii above 
         return anExpressionHandler;
      }
      ```
   4. Annotate JAX-RS resource implementation class's methods with [`@PreAuthorize`](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/access/prepost/PreAuthorize.html)

At Spring Boot time, Birch Security OAuth2 will load one instance of each `@Path @Service` annotated JAX-RS service, one per OAuth2 realm specified in 
Birch Security OAuth2 configurations.  Resources in each realm are attached to the CXF Bus by the base path provided within each realm's configuration.  The 
Bearer token for each realm is evaluated per that realm's configurations.  Therefore, it is possible each realm must have its own `GrantedAuthoritiesBuilder`
in case each realm's IdP provide roles in a different manner than others.
