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

import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import com.nimbusds.jose.shaded.json.JSONArray;
import org.birchframework.configuration.BirchProperties;
import org.birchframework.framework.cxf.JAXRSUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.spring.boot.autoconfigure.CxfAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.security.reactive.ReactiveManagementWebSecurityAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring resources security auto-configuration of CXF JAX-RS resources, in OAuth2 mode.  Since this configuration will create endpoints for
 * all {@link org.springframework.stereotype.Service} annotated classes, it is important that the CXF auto-scanning of components, beans,
 * and classes be turned off by setting the following values in the application configuration:
 * <pre>
 * cxf.jaxrs.component-scan=false
 * cxf.jaxrs.classes-scan=false
 * </pre>
 * Not turning off scanning will have unpredictable and likely undesirable consequences such as service/endpoint creation error at CXF Bus bootstrap time.
 * @author Keivan Khalichi
 */
@Configuration(proxyBeanMethods = false)
@EnableAutoConfiguration(exclude = {org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class,
                                    ManagementWebSecurityAutoConfiguration.class, ReactiveManagementWebSecurityAutoConfiguration.class})
@EnableConfigurationProperties(BirchProperties.class)
@ConditionalOnExpression("${birch.security.oauth2.enabled} and '${birch.security.oauth2.mode:STANDARD}'.toUpperCase() == 'STANDARD'  ")
@AutoConfigureAfter(CxfAutoConfiguration.class)
@AutoConfigureBefore({SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
@RequiredArgsConstructor
public class OAuth2ResourceServerAutoConfiguration {

   private final BirchProperties           properties;
   private final GenericApplicationContext context;

   @PostConstruct
   public void init() {
      this.properties.getSecurity()
                     .getOauth2()
                     .getRealms()
                     .entrySet()
                     .stream()
                     .filter(entry -> entry.getValue().isEnabled())
                     .forEach(entry -> {
                        final var aBeanName = String.format("%s-%s", StringUtils.uncapitalize(OAuth2SecurityFilterChain.class.getSimpleName()), entry.getKey());
                        this.context.registerBean(
                           aBeanName,
                           SecurityFilterChain.class,
                           () -> new OAuth2SecurityFilterChain(entry.getValue(), this.properties.getSecurity().getUnsecureContextPaths())
                        );
                     });
   }

   @Bean
   @ConditionalOnMissingBean(AuthenticationEventPublisher.class)
   DefaultAuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher theEventPublisher) {
      return new DefaultAuthenticationEventPublisher(theEventPublisher);
   }

   @Bean
   BearerTokenResolver defaultBearerTokenResolver() {
      return new DefaultBearerTokenResolver();
   }

   @Bean
   @ConditionalOnMissingBean(GrantedAuthoritiesBuilder.class)
   GrantedAuthoritiesBuilder defaultGrantedAuthoritiesBuilder() {
      return (roleClaim, jwt) -> jwt.<JSONArray>getClaim(roleClaim)
                                    .stream()
                                    .<GrantedAuthority>map(role -> new SimpleGrantedAuthority((String) role))
                                    .collect(Collectors.toList());
   }

   @Bean
   @ConditionalOnMissingBean(JAXRSUtils.class)
   JAXRSUtils jaxrsUtils(final ApplicationContext theContext) {
      return new JAXRSUtils(theContext);
   }
}