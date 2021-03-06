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
package org.birchframework.framework.i18n;

import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.spring.boot.autoconfigure.CxfAutoConfiguration;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.birchframework.configuration.BirchProperties;
import org.birchframework.framework.spring.CustomScopesAutoConfiguration;
import org.birchframework.framework.stub.Stub;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * General configuration for span-based internationalization.  This framework allows for locale information from a user client web application to
 * span all microservices.
 * @author Keivan Khalichi
 */
@Configuration(proxyBeanMethods = false)
@EnableAutoConfiguration
@EnableConfigurationProperties(BirchProperties.class)
@ConditionalOnProperty(prefix = "birch.i18n", name = "enabled")
@ConditionalOnClass({SpringBus.class, CXFServlet.class})
@AutoConfigureAfter({DispatcherServletAutoConfiguration.class, CustomScopesAutoConfiguration.class,
                     CxfAutoConfiguration.class})
@Import(CustomScopesAutoConfiguration.class)
@RefreshScope
public class InternationalizationAutoConfiguration {

   /**
    * Creates a {@link ResourceBundleMessageSource}.
    * @return the resource bundle message source instance
    */
   @Bean
   @ConditionalOnProperty(prefix = "birch.i18n", name = "resource-bundle-base-name")
   MessageSource messageSource(final BirchProperties theProperties) {
      return Stub.of(ResourceBundleMessageSource.class, resourceBundleMessageSource -> {
         resourceBundleMessageSource.setBasename(theProperties.getI18n().getResourceBundleBaseName());
      });
   }
}