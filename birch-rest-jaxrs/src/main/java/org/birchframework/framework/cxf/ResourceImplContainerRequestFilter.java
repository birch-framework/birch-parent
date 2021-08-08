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
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import org.birchframework.dto.Spannable;
import org.slf4j.MDC;

/**
 * Intercepts any requests to any JAX-RS resource implementation in order to add all {@link Spannable} values that are available in the request headers to the
 * thread's {@link MDC} context.
 * @see Spannable
 * @see ResourceClientRequestFilter
 * @author Keivan Khalichi
 */
@Provider
public class ResourceImplContainerRequestFilter implements ContainerRequestFilter {

   @Override
   public void filter(final ContainerRequestContext theRequestContext) throws IOException {
      final var aHeaders = theRequestContext.getHeaders();
      Spannable.keys.stream().filter(aHeaders::containsKey).forEach(key -> MDC.put(key, aHeaders.getFirst(key)));
   }
}