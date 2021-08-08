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

import java.util.Optional;
import javax.ws.rs.core.Response;
import org.birchframework.framework.jaxrs.Responses;
import org.birchframework.framework.marshall.MarshallingError;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

/**
 * Base interface for Actuator {@link HealthIndicator} implementations.
 *
 * @author Keivan Khalichi
 */
public interface BaseHealthIndicator extends HealthIndicator {

   /**
    * Default implementation to be used by implementations.  Overloads {@link #health()} from {@link HealthIndicator}.
    *
    * @param theActuatorResource instance of {@link ActuatorResource} specific for the health check being conducted
    * @return health of target {@link ActuatorResource}
    */
   default Health health(final ActuatorResource theActuatorResource) {
      final Response aHealthResponse = theActuatorResource.health();
      switch (aHealthResponse.getStatusInfo().toEnum()) {
         case OK:
            return Responses.of(aHealthResponse).map(
               String.class,
               e -> this.unmarshall(e).map(healthWrapper -> {
                  final Health aHealth = healthWrapper.build();
                  if (Status.UP.equals(aHealth.getStatus())) {
                     return Health.up().build();
                  }
                  else if (Status.DOWN.equals(aHealth.getStatus())) {
                     return Health.down().build();
                  }
                  else {
                     return Health.unknown().build();
                  }
               }).orElseGet(() -> Health.unknown().build())
            );
         case NOT_FOUND:
            return Health.down().build();
         case INTERNAL_SERVER_ERROR:
            return Health.outOfService().build();
         case NO_CONTENT:
         default:
            return Health.unknown().build();
      }
   }

   /**
    * Provides a hook for implementers to unmarshall the an entity using injected mechanisms.
    * @param theEntity the entity to be unmarshelled
    * @return the unmarshalled health wrapper
    * @throws MarshallingError when there the implementer encounters any errors
    */
   Optional<HealthWrapper> unmarshall(String theEntity);
}