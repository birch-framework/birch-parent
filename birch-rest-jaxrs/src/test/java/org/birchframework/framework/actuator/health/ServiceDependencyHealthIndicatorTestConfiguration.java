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
import javax.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomUtils;
import org.birchframework.configuration.BirchProperties;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.when;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

/**
 * Application context configuration for {@link ServiceDependencyHealthIndicatorTest}.
 * @author Keivan Khalichi
 */
@Configuration
@EnableConfigurationProperties(value = BirchProperties.class)
@ComponentScan(value = {"org.birchframework.framework.marshall",
                        "org.birchframework.framework.actuator",
                        "org.birchframework.configuration",
                        "org.springframework.boot.autoconfigure.jackson"},
               excludeFilters = @Filter(value = ActuatorResourceAutoConfiguration.class, type = ASSIGNABLE_TYPE))
@RequiredArgsConstructor
public class ServiceDependencyHealthIndicatorTestConfiguration {

   private static final String[] RESPONSES = {"{\"status\":\"UP\"}", "{\"status\":\"DOWN\"}", "{\"status\":\"UNKNOWN\"}"};

   @Autowired
   private final BirchProperties                 properties;
   @Autowired
   private final ConfigurableListableBeanFactory factory;

   @PostConstruct
   public void init()  {
      this.properties.getActuator().getUriMap().forEach(((k, v) -> {
         final var aResource = Mockito.mock(ActuatorResource.class);
         when(aResource.health()).thenReturn(Response.ok(RESPONSES[RandomUtils.nextInt(0,3)]).build());
         this.factory.registerSingleton(k, aResource);
      }));
   }
}