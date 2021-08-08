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

import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.birchframework.framework.kafka.KafkaAdminUtils;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link KafkaAdminUtilsResource}.
 * @author Keivan Khalichi
 */
@Service
@ConditionalOnClass({Response.class, Jackson2ObjectMapperBuilder.class})
@ConditionalOnBean(KafkaAdminUtils.class)
@OpenAPIDefinition(info = @Info(title = "Kafka Admin Utilities", version = "1", description = "APIs for programmatic access to Kafka admin utilities"))
@RequiredArgsConstructor
public class KafkaAdminUtilsResourceImpl implements KafkaAdminUtilsResource {

   private static final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

   private final KafkaAdminUtils kafkaAdminUtils;

   @Operation(summary = "Returns topic to consumer lag mappings", responses = {
      @ApiResponse(description = "Topic/Lag mappings",
                   content = @Content(array = @ArraySchema(arraySchema = @Schema(implementation = Map.class, description = "Topic/Lag mappings"),
                                                           schema = @Schema(implementation = Entry.class, description = "Tuple: {topic,lag}"),
                                                           uniqueItems = true)))
   })
   @Override
   public Response topicLags() {
      final StreamingOutput aStream = output -> objectMapper.writeValue(output, this.kafkaAdminUtils.topicLags());
      return Response.ok(aStream).build();
   }
}