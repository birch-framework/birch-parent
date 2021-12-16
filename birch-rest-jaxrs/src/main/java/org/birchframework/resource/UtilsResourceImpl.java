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

package org.birchframework.resource;

import javax.ws.rs.core.Response;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.jaxrs.openapi.OpenApiFeature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

/**
 * {@link UtilsResource} implementation.
 * @author Keivan Khalichi
 */
@Service
@ConditionalOnClass({Response.class, OpenApiFeature.class})
@OpenAPIDefinition(info = @Info(title = "Utility endpoints", version = "1", description = "APIs for microservice utilities"))
@Slf4j
public class UtilsResourceImpl implements UtilsResource {

   @Operation(summary = "Force system garbage collection for this microservice", responses = {
      @ApiResponse(description = "Description of action performed", content = @Content(schema = @Schema(implementation = String.class)))
   })
   @Override
   public Response runGC() {
      log.info("Forcing system garbage collection");
      System.gc();
      log.info("Garbage collection completed");
      return Response.ok("Garbage collection completed").build();
   }
}