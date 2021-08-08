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

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import org.birchframework.framework.beans.Beans;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import static org.springframework.beans.factory.config.AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;

/**
 * Bean that provides JAX-RS convenient utility methods.
 * @author Keivan Khalichi
 */
@RequiredArgsConstructor
public class JAXRSUtils {

   public static final Set<String> DEFAULT_CLASSES_SCAN_PACKAGES = Set.of("com.fasterxml.jackson.jaxrs.json",
                                                                          "org.apache.cxf.jaxrs.spring",
                                                                          "org.apache.cxf.bus.managers");

   private final ApplicationContext context;

   public List<Object> findProviders(final Set<String> theClassesScanPackages) {
      final var aComponentScanPackagesSet = new HashSet<>(DEFAULT_CLASSES_SCAN_PACKAGES);
      aComponentScanPackagesSet.addAll(theClassesScanPackages);
      final var aClassesScanPackages = aComponentScanPackagesSet.toArray(new String[0]);
      final var aProviderClasses = Beans.classesAnnotatedWith(Provider.class, aClassesScanPackages);
      aProviderClasses.addAll(Beans.classesAnnotatedWith(org.apache.cxf.annotations.Provider.class, aClassesScanPackages));
      final var aBeanFactory = this.context.getAutowireCapableBeanFactory();
      return aProviderClasses.stream()
                             .map(providerClass -> {
                                try {
                                   return this.context.getBean(providerClass);
                                }
                                catch (NoSuchBeanDefinitionException e) {
                                   return providerClass.isInterface() || Modifier.isAbstract(providerClass.getModifiers())
                                        ? null
                                        : aBeanFactory.createBean(providerClass, AUTOWIRE_BY_TYPE, false);
                                }
                             })
                             .filter(Objects::nonNull)
                             .collect(Collectors.toList());
   }

   public Map<String, ?> findJAXRSFilterBeans() {
      final var aRequestFilterBeans = this.context.getBeansOfType(ContainerRequestFilter.class);
      final var aResponseFilterBeans = this.context.getBeansOfType(ContainerResponseFilter.class);
      final var aFilterBeans = new HashMap<String, Object>(aRequestFilterBeans);
      aFilterBeans.putAll(aResponseFilterBeans);
      return aFilterBeans;
   }
}