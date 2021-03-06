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
package org.birchframework.framework.cxf;

import java.net.MalformedURLException;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;
import javax.ws.rs.Path;
import com.google.common.base.Throwables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.spring.boot.autoconfigure.CxfAutoConfiguration;
import org.birchframework.framework.beans.Beans;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

/**
 * For each interface in the classpath annotated with both {@link AutoProxy} and {@link Path}, creates a CXF JAX-RS proxy and registers it with the Spring
 * bean container. Will not create a proxy bean if there already exists a bean, for example for {@link org.springframework.stereotype.Service} annotated
 * implementation classes that inherit from respective {@link AutoProxy} annotated interfaces.
 * The requirement for auto-proxying JAX-RS resources are therefore as follows:
 * <ul>
 *    <li>This annotation applied to an <emphasis>interface</emphasis></li>
 *    <li>{@link javax.ws.rs.Path} must also be present</li>
 *    <li>The interface implementation (Impl) is not present as a bean (i.e. {@link org.springframework.stereotype.Service}) within the application context</li>
 * </ul>
 * Only when these criteria are met will a proxy bean be created and registered for the annotated interface
 * @see AutoProxy
 * @author Keivan Khalichi
 */
@Configuration
@EnableAutoConfiguration
@AutoConfigureAfter(CxfAutoConfiguration.class)
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("SpringFacetCodeInspection")
public class ResourceProxyBeanAutoConfiguration implements BeanPostProcessor {

   private static final UrlValidator URL_VALIDATOR = UrlValidator.getInstance();

   private final GenericApplicationContext   context;
   private final ResourceClientRequestFilter resourceClientRequestFilter;

   @PostConstruct
   protected void init() {
      final var aResourceInterfaces = Beans.classesAnnotatedWith(AutoProxy.class);
      aResourceInterfaces.stream().filter(c -> c.isInterface() && c.isAnnotationPresent(Path.class)).forEach(clazz -> {
         try {
            final var aBean = this.context.getBean(clazz);
            log.info("A bean of type {} that satisfies auto-wiring by type for {} already exists in the application context; skipping proxy creation",
                     aBean.getClass().getName(),
                     clazz.getName());
         }
         catch (NoSuchBeanDefinitionException nsbde) {
            try {
               this.registerBean(clazz);
            }
            catch (Exception e) {
               throw new RuntimeException(Throwables.getRootCause(e));
            }
         }
      });
   }

   private <T> void registerBean(Class<T> theClass) throws MalformedURLException {
      final var anEnvironment = this.context.getEnvironment();
      final var anAutoProxy   = theClass.getAnnotation(AutoProxy.class);
      final var aProviders    = List.of(this.resourceClientRequestFilter, anAutoProxy.providers());
      final var aUserName     = anEnvironment.resolvePlaceholders(anAutoProxy.username());
      final var aPassword     = anEnvironment.resolvePlaceholders(anAutoProxy.password());
      final var aBaseURI      = anEnvironment.resolvePlaceholders(anAutoProxy.baseURI());
      if (!URL_VALIDATOR.isValid(aBaseURI)) {
         throw new MalformedURLException(String.format("Invalid base URI %s specified for resource %s", aBaseURI, theClass.getName()));
      }
      final Supplier<T> aProxySupplier = () -> StringUtils.isBlank(aUserName)
                                             ? JAXRSClientFactory.create(aBaseURI, theClass, aProviders, anAutoProxy.threadSafe())
                                             : JAXRSClientFactory.create(aBaseURI, theClass, aProviders, aUserName, aPassword, null);
      this.context.registerBean(theClass, aProxySupplier);
      this.context.getBean(theClass);
      log.info("Registered JAX-RS proxy bean of type {}", theClass.getName());
   }
}