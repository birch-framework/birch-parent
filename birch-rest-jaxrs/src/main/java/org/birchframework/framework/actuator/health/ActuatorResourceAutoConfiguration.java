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
package org.birchframework.framework.actuator.health;

import javax.annotation.PostConstruct;
import org.birchframework.configuration.BirchProperties;
import org.birchframework.configuration.BirchProperties.Actuator;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

/**
 * Auto configuration for {@link ActuatorResource}s.  Define properties as follows:
 * <pre>
 * birch:
 *   actuator:
 *     uri-map:
 *       service1: http://hostname:port
 *       service2: http://hostname:port
 *       ...
 * </pre>
 * @author Keivan Khalichi
 */
@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties(value = BirchProperties.class)
public class ActuatorResourceAutoConfiguration {

   private final Actuator                        properties;
   private final ConfigurableListableBeanFactory factory;

   public ActuatorResourceAutoConfiguration(final BirchProperties theProperties, final ConfigurableListableBeanFactory theFactory) {
      this.properties = theProperties.getActuator();
      this.factory    = theFactory;
   }

   @PostConstruct
   void init() {
      if (!CollectionUtils.isEmpty(this.properties.getUriMap())) {
         this.properties.getUriMap().forEach((k, v) -> this.factory.registerSingleton(k, JAXRSClientFactory.create(v, ActuatorResource.class)));
      }
   }
}