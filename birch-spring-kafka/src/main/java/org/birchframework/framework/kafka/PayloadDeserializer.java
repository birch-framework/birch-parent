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

import java.io.IOException;
import org.birchframework.framework.bridge.Payload;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

/**
 * Payload deserializer.
 * @author Keivan Khalichi
 */
@SuppressFBWarnings("DM_DEFAULT_ENCODING")
public class PayloadDeserializer implements Deserializer<Payload<?>> {

   private static final Logger log = LoggerFactory.getLogger(PayloadDeserializer.class);
   private static final Jackson2ObjectMapperBuilder objectMapperBuilder = Jackson2ObjectMapperBuilder.json();

   @Override
   public Payload<?> deserialize(final String theTopic, final byte[] theData) {
      final var anObjectMapper = objectMapperBuilder.build();
      if (theData != null && theData.length > 0) {
         Payload<?> aPayload = null;
         try {
            aPayload = anObjectMapper.readValue(theData, Payload.class);
            if (log.isDebugEnabled()) {
               log.debug("Data: {}", new String(theData));
               log.debug("Deserialized payload: {}", ToStringBuilder.reflectionToString(aPayload, JSON_STYLE));
            }
         }
         catch (IOException e) {
            log.error("An error occurred.", e);
         }
         return aPayload;
      }
      return null;
   }
}