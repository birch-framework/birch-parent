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
package org.birchframework.framework.cxf;

import java.io.IOException;
import java.util.List;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.birchframework.dto.Spannable;
import org.slf4j.MDC;

import static org.birchframework.framework.cxf.SpanHeadersContainerBean.*;

/**
 * Intercepts all client requests where it is defined and adds all {@link Spannable} values that are defined within the current {@link MDC} context to the
 * request headers.
 * @see Spannable
 * @see ResourceImplContainerRequestFilter
 * @author Keivan Khalichi
 */
@RequiredArgsConstructor
public class ResourceClientRequestFilter implements ClientRequestFilter {

   private final SpanHeadersContainerBean spanHeadersContainerBean;

   @Override
   public void filter(final ClientRequestContext theRequestContext) throws IOException {
      final var aHeaders = theRequestContext.getHeaders();
      Spannable.keys.forEach(key -> {
         final var anMDCValue = MDC.get(key);
         if (!(StringUtils.isBlank(anMDCValue) || aHeaders.containsKey(key))) {
            aHeaders.put(key, List.of(anMDCValue));
         }
      });
      if (this.spanHeadersContainerBean.hasData()) {
         if (!aHeaders.containsKey(LOCALE_HEADER)) {
            aHeaders.put(LOCALE_HEADER, List.of(this.spanHeadersContainerBean.getLocale()));
         }
         if (!aHeaders.containsKey(CORRELATION_ID_HEADER)) {
            aHeaders.put(CORRELATION_ID_HEADER, List.of(this.spanHeadersContainerBean.getCorrelationID().toString()));
         }
      }
   }
}