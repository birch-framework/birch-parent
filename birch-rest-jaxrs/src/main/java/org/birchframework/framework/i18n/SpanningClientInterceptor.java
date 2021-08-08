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

import java.util.List;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

import static org.apache.cxf.message.Message.PROTOCOL_HEADERS;
import static org.apache.cxf.phase.Phase.PRE_PROTOCOL;

/**
 * Applies custom headers to CXF JAX-RS clients.
 * @author Keivan Khalichi
 */
public class SpanningClientInterceptor extends AbstractPhaseInterceptor<Message> {

   private final SpanHeadersContainerBean spanHeadersContainer;

   public SpanningClientInterceptor(final SpanHeadersContainerBean theSpanHeadersContainerBean) {
      super(PRE_PROTOCOL);
      this.spanHeadersContainer = theSpanHeadersContainerBean;
   }

   @Override
   @SuppressWarnings("unchecked")
   public void handleMessage(final Message theMessage) throws Fault {
      final MetadataMap<String, Object> aHeaders = (MetadataMap<String, Object>) theMessage.get(PROTOCOL_HEADERS);
      if (aHeaders != null && this.spanHeadersContainer.hasData()) {
         if (!aHeaders.containsKey(SpanHeadersContainer.LOCALE_HEADER)) {
            aHeaders.put(SpanHeadersContainer.LOCALE_HEADER, List.of(this.spanHeadersContainer.getLocale()));
         }
         if (!aHeaders.containsKey(SpanHeadersContainer.CORRELATION_ID_HEADER)) {
            aHeaders.put(SpanHeadersContainer.CORRELATION_ID_HEADER, List.of(this.spanHeadersContainer.getCorrelationID().toString()));
         }
      }
   }
}