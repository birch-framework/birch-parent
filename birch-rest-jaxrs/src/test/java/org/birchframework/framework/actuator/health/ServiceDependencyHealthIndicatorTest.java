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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link ServiceDependencyHealthIndicator}
 * @author Keivan Khalichi
 */
@SpringBootTest(classes = ServiceDependencyHealthIndicatorTestConfiguration.class)
public class ServiceDependencyHealthIndicatorTest {

   @Autowired
   private ServiceDependencyHealthIndicator healthIndicator;

   /**
    * Tests {@link ServiceDependencyHealthIndicator#health()}.
    */
   @Test
   public void testHealth() /*throws MarshallingError*/ {
      final Health aHealth = this.healthIndicator.health();
      assertThat(aHealth).isNotNull();
      assertThat(aHealth.getStatus()).isNotNull();
   }
}