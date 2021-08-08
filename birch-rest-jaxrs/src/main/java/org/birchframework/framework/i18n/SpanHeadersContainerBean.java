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

import java.util.UUID;
import lombok.experimental.Delegate;
import org.birchframework.framework.spring.ThreadScope;
import org.springframework.beans.factory.annotation.Lookup;

/**
 * Wrapper around the {@link ThreadScope}ed {@link SpanHeadersContainer} bean.
 * This is done so that beans that want access to the {@link SpanHeadersContainer} are not forced to be declared {@code abstract} just so they can provide
 * an abstract {@link Lookup} method.
 * @author Keivan Khalichi
 */
public abstract class SpanHeadersContainerBean {

   @Lookup
   @Delegate
   abstract SpanHeadersContainer delegate();

   public void setLocale(final String theLocale) {
      this.delegate().setLocale(theLocale);
   }

   void setCorrelationID(final UUID theCorrelationID) {
      this.delegate().setCorrelationID(theCorrelationID);
   }

   public void setCorrelationID(final String theCorrelationID) {
      this.delegate().setCorrelationID(theCorrelationID);
   }

   public String toString() {
      return this.delegate().toString();
   }
}