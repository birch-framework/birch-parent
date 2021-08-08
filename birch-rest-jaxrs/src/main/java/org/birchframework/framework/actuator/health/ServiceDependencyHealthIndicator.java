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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.birchframework.configuration.BirchProperties;
import org.birchframework.framework.marshall.MarshallUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Composite Actuator health indicator that checks all of of a service's dependencies.
 * @author Keivan Khalichi
 */
@Component
@RequiredArgsConstructor
public class ServiceDependencyHealthIndicator implements BaseHealthIndicator {

   /** Auto-configuration properties */
   private final BirchProperties properties;

   /** Injection of marshall utils */
   private final MarshallUtils marshallUtils;

   /** Spring application context */
   private final ApplicationContext applicationContext;


   /**
    * Checks {@link Health} of all dependencies and reports on {@link Health} of this service/aggregate.
    * @return composite health assessment of all dependencies
    */
   @Override
   public Health health() {
      final Health aReturnValue;
      if (CollectionUtils.isEmpty(this.properties.getActuator().getUriMap())) {
         aReturnValue = Health.unknown().build();
      }
      else {
         final Map<String, Health> aHealthMap = new HashMap<>();
         this.properties.getActuator().getUriMap().forEach((key,uri) -> {
            Health aHealth;
            try {
               aHealth = this.health((ActuatorResource) this.applicationContext.getBean(key));
            }
            catch (Exception e) {
               aHealth = Health.unknown().withException(e).build();
            }
            aHealthMap.put(key, aHealth);
         });
         Health.Builder aHealthBuilder;
         if (aHealthMap.entrySet().stream().anyMatch(e -> e.getValue().getStatus().equals(Status.DOWN))) {
            aHealthBuilder = Health.down();
         }
         else if (aHealthMap.entrySet().stream().anyMatch(e -> e.getValue().getStatus().equals(Status.UNKNOWN))) {
            aHealthBuilder = Health.unknown();
         }
         else {
            aHealthBuilder = Health.up();
         }
         aHealthMap.forEach(aHealthBuilder::withDetail);
         aReturnValue = aHealthBuilder.build();
      }
      return aReturnValue;
   }

   /** {@inheritDoc} */
   @Override
   public Optional<HealthWrapper> unmarshall(final String theEntity) {
      return this.marshallUtils.deserialize(theEntity, HealthWrapper.class);
   }
}