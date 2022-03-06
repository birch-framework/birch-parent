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
package org.birchfw.test.i18n;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.birchframework.framework.cxf.SpanHeadersContainerBean;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import static org.birchframework.framework.cxf.SpanHeadersContainerBean.*;

/**
 * Implements {@link TestResource}.
 * @author Keivan Khalichi
 */
@Service
@OpenAPIDefinition(info = @Info(title = "Test resource", version = "1", description = "Test JAX-RS resource used for testing"))
@RequiredArgsConstructor
@Slf4j
public class TestResourceImpl implements TestResource {

   private final ApplicationContext       context;
   private       SpanHeadersContainerBean spanHeadersContainer;


   @PostConstruct
   void init() {
      try {
         this.spanHeadersContainer = this.context.getBean(SpanHeadersContainerBean.class);
      }
      catch (BeansException e) {
         log.warn("Did not find SpanHeadersContainer");
      }
   }
   /**
    * Tests {@link TestResource#execute()}.
    * @return always OK
    */
   @Operation(summary = "Execute", responses = {
      @ApiResponse(description = "Map of locale and correlation-id headers", content = @Content(schema = @Schema(implementation = Map.class)))
   })
   @Override
   public Response execute() {
      if (this.spanHeadersContainer == null) {
         return Response.ok(String.format("%s is not configured", SpanHeadersContainerBean.class.getSimpleName())).build();
      }
      log.info("Method invoked; SpanHeadersContainer: {}", this.spanHeadersContainer.toString());
      final var aMap = Map.of(LOCALE_HEADER, this.spanHeadersContainer.getLocale(),
                              CORRELATION_ID_HEADER, this.spanHeadersContainer.getCorrelationID().toString());
      return Response.ok(new HashMap<>(aMap)).build();
   }
}