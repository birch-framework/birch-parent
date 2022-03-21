/*===============================================================
 = Copyright (c) 2021 Birch Framework
 = This program is free software: you can redistribute it and/or modify
 = it under the terms of the GNU General Public License as published by
 = the Free Software Foundation, either version 3 of the License, or
 = any later version.
 = This program is distributed in the hope that it will be useful,
 = but WITHOUT ANY WARRANTY; without even the implied warranty of
 = MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 = GNU General Public License for more details.
 = You should have received a copy of the GNU General Public License
 = along with this program.  If not, see <https://www.gnu.org/licenses/>.
 ==============================================================*/
package org.birchframework.security.oauth2;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import com.google.common.base.Throwables;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.Bus;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.birchframework.configuration.BirchProperties.IdPRealm;
import org.birchframework.framework.beans.Beans;
import org.birchframework.framework.cxf.JAXRSUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint;
import org.springframework.boot.actuate.autoconfigure.jolokia.JolokiaEndpoint;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.logging.LogFileWebEndpoint;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.actuate.web.mappings.MappingsEndpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.DefaultLoginPageConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import static org.apache.http.conn.ssl.TrustAllStrategy.INSTANCE;
import static org.birchframework.dto.BirchErrorCode.*;
import static org.springframework.boot.autoconfigure.security.SecurityProperties.DEFAULT_FILTER_ORDER;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;
import static org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256;

/**
 * Configures Spring Security as well as JAX-RS resources given an OAuth2 identity provider realm.  Each realm must have a unique
 * base path, which in turn for each a {@link org.springframework.security.authentication.AuthenticationProvider} is configured as well as
 * a CXF server created.
 * @author Keivan Khalichi
 * @see IdPRealm
 */
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("PMD.TooManyStaticImports")
public class OAuth2SecurityFilterChain implements SecurityFilterChain, Ordered {

   public static final Pattern contextPathPattern = Pattern.compile("^/?(([a-zA-Z-_]+/?)+)$");

   protected static final AtomicInteger order = new AtomicInteger(DEFAULT_FILTER_ORDER);

   @SuppressWarnings("HttpUrlsUsage")
   private static final String CXF_BUS_HTTP_TRASNPORT_ID = "http://cxf.apache.org/transports/http";

   protected final IdPRealm    properties;
   protected final Set<String> unsecureContextPaths;

   @Resource
   private GenericApplicationContext   context;
   @Resource
   private Bus                         bus;
   @Resource
   private UserDetailsService          userDetailsService;
   @Resource
   private ObjectPostProcessor<Object> objectPostProcessor;
   @Resource
   private GrantedAuthoritiesBuilder   defaultGrantedAuthoritiesBuilder;
   @Resource
   private BearerTokenResolver         defaultBearerTokenResolver;
   @Resource
   private JAXRSUtils                  jaxrsUtils;
   @Value("${cxf.path:/api}")
   private String                      cxfPath;
   @Value("${cxf.jaxrs.classes-scan-packages:}")
   private Set<String>                 classesScanPackages;
   @SuppressWarnings("InstanceVariableMayNotBeInitialized")
   private String                      contextPath;
   @SuppressWarnings("InstanceVariableMayNotBeInitialized")
   private SecurityFilterChain         delegate;

   @PostConstruct
   void init() {
      final var aRealmContextPath = normalizePath(this.properties.getRealmContextPath());
      if (aRealmContextPath == null) {
         log.warn("Realm: {} has specified the context path pattern: {} which has resulted in its removal from the effective context path",
                  this.properties.getValue(), this.properties.getRealmContextPath());
         this.contextPath = this.cxfPath;
      }
      else {
         this.contextPath = String.format("/%s/%s", normalizePath(this.cxfPath), aRealmContextPath);
         log.debug("Realm: {} effective context path: {}", this.properties.getValue(), this.contextPath);
      }
      try {
         this.createServices();
         this.delegate = this.httpSecurity().build();
      }
      catch (Exception e) {
         throw new RuntimeException(String.format("Unable to create security filter chain for IdP config %s", this.properties.getValue()), e);
      }
   }

   @Override
   public int getOrder() {
      return order.getAndSet(order.get() + 5);
   }

   @Override
   public boolean matches(final HttpServletRequest theRequest) {
      return this.delegate.matches(theRequest);
   }

   @Override
   public List<Filter> getFilters() {
      return this.delegate.getFilters();
   }

   protected HttpSecurity httpSecurity() throws Exception {

      final JwtDecoder aJWTDecoder;
      if (StringUtils.isNotBlank(this.properties.getJwkSetUri())) {
         aJWTDecoder = NimbusJwtDecoder.withJwkSetUri(this.properties.getJwkSetUri())
                                       .restOperations(this.jwkSetRestOperations(this.properties.isDisableSSLValidation()))
                                       .jwsAlgorithm(RS256)
                                       .build();
      }
      else {
         if (StringUtils.isNotBlank(this.properties.getIssuerUri())) {
            aJWTDecoder = new IssuerAwareJWTDecoderAdapter(this.properties.getIssuerUri(), this.properties.isDisableSSLValidation());
         }
         else {
            aJWTDecoder = new NimbusJwtDecoder(new DefaultJWTProcessor<>());
         }
      }

      final var anAuthenticationManagerBuilder = new AuthenticationManagerBuilder(this.objectPostProcessor);
      final var aJWTAuthProvider = new JwtAuthenticationProvider(aJWTDecoder);
      final var aJWTAuthConverter = this.jwtAuthConverter();
      aJWTAuthProvider.setJwtAuthenticationConverter(aJWTAuthConverter);
      anAuthenticationManagerBuilder.authenticationProvider(aJWTAuthProvider);
      anAuthenticationManagerBuilder.userDetailsService(this.userDetailsService);

      final var anHTTPSecurity = this.httpSecurity(anAuthenticationManagerBuilder);
      anHTTPSecurity.authorizeRequests((requests) -> {
         requests.requestMatchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class, MappingsEndpoint.class, LoggersEndpoint.class,
                                                     LogFileWebEndpoint.class, MetricsEndpoint.class, EnvironmentEndpoint.class,
                                                     PrometheusScrapeEndpoint.class, BeansEndpoint.class, JolokiaEndpoint.class,
                                                     ConditionsReportEndpoint.class)).permitAll();
         requests.antMatchers("/**/openapi.json").permitAll();
         if (!CollectionUtils.isEmpty(this.unsecureContextPaths)) {
            this.unsecureContextPaths.stream()
                                     .filter(path -> !StringUtils.equals(path, this.contextPath))
                                     .forEach(path -> requests.antMatchers(String.format("/%s/%s/**", normalizePath(this.cxfPath), normalizePath(path)))
                                                              .permitAll());
         }
         requests.antMatchers(String.format("/%s/**", normalizePath(this.contextPath))).authenticated();
      });

      anHTTPSecurity.oauth2ResourceServer(
         oauth2ResourceServer -> {
            oauth2ResourceServer.bearerTokenResolver(this.defaultBearerTokenResolver);
            oauth2ResourceServer.jwt(
               jwt -> {
                  try {
                     jwt.decoder(aJWTDecoder)
                        .jwtAuthenticationConverter(aJWTAuthConverter);
                     if (StringUtils.isNotBlank(this.properties.getJwkSetUri())) {
                        jwt.jwkSetUri(this.properties.getJwkSetUri());
                     }
                  }
                  catch (Exception e) {
                     throw new OAuth2ConfigurationException(B43000, e);
                  }
               }
            );
         }
      );
      log.info("HttpSecurity created for realm with key {} and name {}", this.properties.getValue(), this.properties.getName());
      return anHTTPSecurity;
   }

   protected void createServices() {

      this.classesScanPackages.add(this.getClass().getPackage().getName());
      final var aProviderBeans = this.jaxrsUtils.findProviders(this.classesScanPackages);
      final var aFilterBeans = this.jaxrsUtils.findJAXRSFilterBeans()
                                              .values()
                                              .stream()
                                              .filter(filter -> aProviderBeans.stream().noneMatch(providerBean -> providerBean.getClass() == filter.getClass()))
                                              .collect(Collectors.toList());
      final var aFeatures = aProviderBeans.stream()
                                          .filter(bean -> bean instanceof Feature)
                                          .map(bean -> (Feature) bean)
                                          .collect(Collectors.toList());
      aProviderBeans.removeAll(aFeatures);
      aProviderBeans.addAll(aFilterBeans);

      final var aResourceBeans = this.context.getBeansWithAnnotation(Service.class)
                                             .values()
                                             .stream()
                                             .filter(bean -> AnnotationUtils.findAnnotation(bean.getClass(), Path.class) != null)
                                             .collect(Collectors.toList());

      final var aFactory = new JAXRSServerFactoryBean();
      aFactory.setBus(this.bus);
      aFactory.setTransportId(CXF_BUS_HTTP_TRASNPORT_ID);
      aFactory.setProviders(aProviderBeans);
      aFactory.setAddress(this.properties.getRealmContextPath());
      aFactory.setServiceBeans(aResourceBeans);
      aFactory.setFeatures(aFeatures);
      aFactory.create();
      log.info("Configured services for IdP realm with key {} and name {}", this.properties.getValue(), this.properties.getName());
   }

   @SuppressWarnings("unchecked")
   protected Converter<Jwt, AbstractAuthenticationToken> jwtAuthConverter() throws Exception {
      final var aUserNameClaimName = this.properties.getUserNameClaimName();
      final var aGroupsClaimName = this.properties.getGroupsClaimName();
      final var aGrantedAuthsBuilder = Beans.instanceOrDefault((Class<GrantedAuthoritiesBuilder>) this.properties.getGrantedAuthoritiesBuilder(),
                                                               this.defaultGrantedAuthoritiesBuilder);
      return jwt -> {
         final var anAccessToken = new OAuth2AccessToken(BEARER, jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt());
         final String aUserName = jwt.getClaim(aUserNameClaimName);
         final var anAttributes = jwt.getClaims();
         final var anAuthorities = aGrantedAuthsBuilder.build(aGroupsClaimName, jwt);
         final var aPrincipal = new DefaultOAuth2AuthenticatedPrincipal(aUserName, anAttributes, anAuthorities);
         final var anAuthentication = new BearerTokenOAuth2Authentication(aPrincipal, anAccessToken, anAuthorities, this.properties);
         SecurityContextHolder.getContext().setAuthentication(anAuthentication);
         if (StringUtils.isBlank(aUserName)) {
            log.error(B43020.getDescription());
            throw new UsernameNotFoundException(B43020.asString());
         }
         else {
            try {
               final var aUser = this.userDetailsService.loadUserByUsername(aUserName);
               anAuthentication.setUser(aUser);
            }
            catch (UsernameNotFoundException e) {
               log.error("A user name not found error occurred; message: {}", Throwables.getRootCause(e).getMessage());
               throw e;
            }
         }
         return anAuthentication;
      };
   }

   protected HttpSecurity httpSecurity(final AuthenticationManagerBuilder theAuthenticationManagerBuilder) throws Exception {
      final var anHTTPSecurity = new HttpSecurity(this.objectPostProcessor, theAuthenticationManagerBuilder, Map.of(ApplicationContext.class, this.context));
      anHTTPSecurity.csrf(withDefaults())
                    .addFilter(new WebAsyncManagerIntegrationFilter())
                    .exceptionHandling(withDefaults())
                    .headers(withDefaults())
                    .sessionManagement(withDefaults())
                    .securityContext(withDefaults())
                    .requestCache(withDefaults())
                    .anonymous(withDefaults())
                    .servletApi(withDefaults())
                    .logout(logout -> {
                       logout.deleteCookies()
                             .invalidateHttpSession(true)
                             .logoutUrl(String.format("/%s/%s", normalizePath(this.contextPath), normalizePath(this.properties.getLogoutUri())));
                       if (StringUtils.isNotBlank(this.properties.getLogoutRedirectUri())) {
                          try {
                             final URI aLogoutRedirectURI = new URI(this.properties.getLogoutRedirectUri());
                             final var aLogoutRedirectURIString = StringUtils.isBlank(aLogoutRedirectURI.getScheme())
                                                                ? String.format("/%s/%s", normalizePath(this.contextPath), normalizePath(this.properties.getLogoutRedirectUri()))
                                                                : this.properties.getLogoutRedirectUri();
                             logout.logoutSuccessUrl(aLogoutRedirectURIString);
                          }
                          catch (URISyntaxException e) {
                             log.error("logout-redirect-uri: {} is a malformed URI", this.properties.getLogoutRedirectUri());
                          }
                       }
                    })
                    .apply(new DefaultLoginPageConfigurer<>());
      return anHTTPSecurity;
   }

   private RestOperations jwkSetRestOperations(final boolean theIsDisableSSLValidation) {
      if (theIsDisableSSLValidation) {
         try {
            final var anSSLContext = SSLContexts.custom().loadTrustMaterial(null, INSTANCE).build();
            final var aConnectionSocketFactory = new SSLConnectionSocketFactory(anSSLContext);
            final var anHTTPClient = HttpClients.custom().setSSLSocketFactory(aConnectionSocketFactory).build();
            return new RestTemplate(new HttpComponentsClientHttpRequestFactory(anHTTPClient));
         }
         catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new OAuth2ConfigurationException(B43010, e);
         }
      }
      return new RestTemplate();
   }

   public static String normalizePath(final String thePath) {
      if (StringUtils.isBlank(thePath)) {
         return null;
      }
      final var aMatcher = contextPathPattern.matcher(StringUtils.stripEnd(thePath.strip().replaceAll("(\\s*/\\s*\\s*)+", "/"), "/"));
      if (aMatcher.matches()) {
         return aMatcher.group(1);
      }
      return null;
   }
}