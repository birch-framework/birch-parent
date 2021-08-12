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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import static lombok.AccessLevel.PACKAGE;

/**
 * A container for HTTP headers that span multiple request/response cycles across multiple microservices.
 * @author Keivan Khalichi
 */
@Getter
@ToString
@NoArgsConstructor(access = PACKAGE)
@SuppressWarnings({"InstanceVariableMayNotBeInitialized", "unused"})
class SpanHeadersContainer {

   public static final String LOCALE_HEADER          = HttpHeaders.ACCEPT_LANGUAGE;
   public static final String CORRELATION_ID_HEADER  = "Correlation-ID";

   @Setter(PACKAGE)
   private       String                    locale;
   private       UUID                      correlationID;
   /** Container for arbitrary HTTP headers */
   @Getter(PACKAGE)
   private final Map<String, Serializable> map = new HashMap<>();

   SpanHeadersContainer(final SpanHeadersContainer theOtherSpanContainer) {
      this.locale        = theOtherSpanContainer.locale;
      this.correlationID = theOtherSpanContainer.correlationID;
      this.map.putAll(theOtherSpanContainer.map);
   }

   void setCorrelationID(final UUID theCorrelationID) {
      this.correlationID = theCorrelationID;
   }

   void setCorrelationID(final String theCorrelationID) {
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

   public Set<Entry<String, Serializable>> entrySet() {
      return this.map.entrySet();
   }

   public boolean hasData() {
      return StringUtils.isNotBlank(this.locale) && this.correlationID != null;
   }
}