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
package org.birchframework.bridge.dataformat;

import java.io.InputStream;
import java.io.OutputStream;
import org.birchframework.bridge.ServiceMessageDTO;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;

/**
 * Service message payload data format.
 * @author Keivan Khalichi
 */
@Component(ServiceMessageDataFormat.BEAN_NAME)
public class ServiceMessageDataFormat implements DataFormat {

   public static final String BEAN_NAME = "service-message";

   private final Jackson2ObjectMapperBuilder objectMapperBuilder = Jackson2ObjectMapperBuilder.json();

   @Override
   public void marshal(final Exchange theExchange, final Object theGraph, final OutputStream theOutputStream) throws Exception {
      final var anObjectMapper = this.objectMapperBuilder.build();
      anObjectMapper.writeValue(theOutputStream, theGraph);
   }

   @Override
   public Object unmarshal(final Exchange theExchange, final InputStream theInputStream) throws Exception {
      final var anObjectMapper = this.objectMapperBuilder.build();
      return anObjectMapper.readValue(theInputStream, ServiceMessageDTO.class);
   }

   @Override
   public void start() {
      // No-op
   }

   @Override
   public void stop() {
      // No-op
   }
}