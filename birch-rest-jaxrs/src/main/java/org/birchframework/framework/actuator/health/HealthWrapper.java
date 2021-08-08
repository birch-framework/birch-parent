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

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.util.CollectionUtils;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.springframework.boot.actuate.health.Status.UNKNOWN;

/**
 * A wrapper class for {@link Health}.
 * @author Keivan Khalichi
 */
@JsonInclude(NON_EMPTY)
public class HealthWrapper {

   /** Wrapper for {@link Health#status }*/
   private Status status;

   /** Wrapper for {@link Health#details }*/
  	private Map<String, Object> details;

   public void setStatus(final String theStatus) {
      this.status = Strings.isNullOrEmpty(theStatus) ? UNKNOWN : new Status(theStatus);
   }

   public void setDetails(final Map<String, Object> theDetails) {
      this.details = theDetails;
   }

   @JsonIgnore
   public Health build() {
      return new Health.Builder(this.status, CollectionUtils.isEmpty(this.details) ? ImmutableMap.of() : this.details).build();
   }
}
