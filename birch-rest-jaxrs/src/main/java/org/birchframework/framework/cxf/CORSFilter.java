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
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import org.birchframework.configuration.BirchProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

import static com.google.common.net.HttpHeaders.*;
import static java.lang.Boolean.*;

/**
 * CXF filter that globally allows Cross-Origin Resource Sharing (CORS), but only when the configuration property {@code birch.security.cxf.cors.allow = true}.
 * @author Keivan Khalichi
 */
@Provider
@Slf4j
@SuppressWarnings("AutoBoxing")
public class CORSFilter implements ContainerResponseFilter {

   private static final Map<String, List<Object>> ACCESS_CONTROL_HEADERS;

   private final boolean allow;

   static {
      ACCESS_CONTROL_HEADERS = Map.of(ACCESS_CONTROL_EXPOSE_HEADERS,    List.of("origin", "content-type", "accept", "authorization"),
                                      ACCESS_CONTROL_ALLOW_CREDENTIALS, List.of(TRUE),
                                      ACCESS_CONTROL_ALLOW_METHODS,     Stream.of(HttpMethod.values()).map(Enum::name).collect(Collectors.toList()),
                                      ACCESS_CONTROL_MAX_AGE,           List.of(Duration.ofSeconds(5).toSeconds()));
   }

   public CORSFilter(final BirchProperties theProperties) {
      this.allow = theProperties.getSecurity().getCxf().getCors().isAllow();
      if (this.allow) {
         log.warn("This filter is configured to allow Cross-Origin Resource Sharing (CORS); this configuration should be disabled in production environments");
      }
   }

   @Override
   public void filter(final ContainerRequestContext theRequestContext, final ContainerResponseContext theResponseContext) throws IOException {
      if (this.allow) {
         final var aResponseHeaders = theResponseContext.getHeaders();
         aResponseHeaders.putAll(ACCESS_CONTROL_HEADERS);
         aResponseHeaders.add(ACCESS_CONTROL_ALLOW_ORIGIN, theRequestContext.getHeaderString(ORIGIN));
      }
   }
}