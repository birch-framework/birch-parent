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

package org.birchframework.framework.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.birchframework.dto.payload.Payload;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

/**
 * Payload serializer.
 * @author Keivan Khalichi
 */
@SuppressFBWarnings("DM_DEFAULT_ENCODING")
public class PayloadSerializer implements Serializer<Payload<?>> {

   private static final Logger log = LoggerFactory.getLogger(PayloadSerializer.class);
   private static final Jackson2ObjectMapperBuilder objectMapperBuilder = Jackson2ObjectMapperBuilder.json();

   @Override
   public byte[] serialize(final String theTopic, final Payload<?> theData) {
      final var anObjectMapper = objectMapperBuilder.build();
      if (theData == null) {
         return null;
      }
      byte[] aSerializedPayload = null;
      try {
         aSerializedPayload = anObjectMapper.writeValueAsBytes(theData);
         if (log.isDebugEnabled()) {
            log.debug("Data: {}", ToStringBuilder.reflectionToString(theData, JSON_STYLE));
            log.debug("Serialized payload: {}", aSerializedPayload);
         }
      }
      catch (JsonProcessingException e) {
         log.error("An error occurred.", e);
      }
      return aSerializedPayload == null ? new byte[]{} : aSerializedPayload;
   }
}
