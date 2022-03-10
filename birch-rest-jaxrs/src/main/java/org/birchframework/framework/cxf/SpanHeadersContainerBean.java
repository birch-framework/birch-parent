/*===============================================================
 = Copyright (c) 2022 Birch Framework
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Delegate;
import org.apache.commons.lang3.StringUtils;
import org.birchframework.framework.spring.ThreadScope;
import org.springframework.beans.factory.annotation.Lookup;

/**
 * Wrapper around the {@link ThreadScope}ed {@link SpanHeadersContainer} bean.
 * This is done so that beans that want access to the {@link SpanHeadersContainer} are not forced to be declared {@code abstract} just so they can provide
 * an abstract {@link Lookup} method.
 * @author Keivan Khalichi
 */
public class SpanHeadersContainerBean {

   public static final String LOCALE_HEADER          = HttpHeaders.ACCEPT_LANGUAGE;
   public static final String CORRELATION_ID_HEADER  = "Correlation-ID";

   private final ThreadLocal<SpanHeadersContainer> spanHeadersContainer = ThreadLocal.withInitial(SpanHeadersContainer::new);

   @Delegate
   @SuppressWarnings("unused")
   private SpanHeadersContainer delegate() {
      return this.spanHeadersContainer.get();
   }

   /**
    * Always call this method at the end of your processing thread in order to prevent memory leaks.
    */
   public void unload() {
      this.spanHeadersContainer.remove();
   }

   @Override
   public String toString() {
      return this.delegate().toString();
   }

   /**
    * A container for HTTP headers used when spanning multiple request/response cycles across multiple microservices.
    * @author Keivan Khalichi
    */
   @Getter
   @ToString
   @SuppressWarnings({"InstanceVariableMayNotBeInitialized", "unused"})
   static class SpanHeadersContainer {

      @Setter
      private       String                    locale;
      private       UUID                      correlationID;
      /** Container for arbitrary HTTP headers */
      @Getter
      private final Map<String, Serializable> map = new HashMap<>();

      public void setCorrelationID(final UUID theCorrelationID) {
         this.correlationID = theCorrelationID;
      }

      public void setCorrelationID(final String theCorrelationID) {
         this.correlationID = UUID.fromString(theCorrelationID);
      }

      public Serializable put(final String theKey, final Serializable theValue) {
         return this.map.put(theKey, theValue);
      }

      public Serializable get(final String theKey) {
         return this.map.get(theKey);
      }

      public boolean containsKey(final String theKey) {
         return this.map.containsKey(theKey);
      }

      public Serializable remove(final String theKey) {
         return this.map.remove(theKey);
      }

      public Set<Map.Entry<String, Serializable>> entrySet() {
         return this.map.entrySet();
      }

      public boolean hasData() {
         return StringUtils.isNotBlank(this.locale) && this.correlationID != null;
      }
   }
}