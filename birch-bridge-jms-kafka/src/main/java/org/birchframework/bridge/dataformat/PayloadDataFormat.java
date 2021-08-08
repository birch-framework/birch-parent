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
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.birchframework.framework.bridge.Destination;
import org.birchframework.framework.bridge.Payload;
import org.birchframework.framework.bridge.PropertyValue;
import org.birchframework.framework.stub.Stub;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;

import static org.apache.camel.Exchange.CORRELATION_ID;
import static org.apache.commons.lang3.builder.ToStringStyle.SIMPLE_STYLE;

/**
 * {@link Payload} un/marshaller for Camel.
 * @author Keivan Khalichi
 */
@Component(PayloadDataFormat.BEAN_NAME)
@Slf4j
public class PayloadDataFormat implements DataFormat {

   public static final String BEAN_NAME = "payload";

   private final PropertyMapper mapper       = PropertyMapper.get();
   private final ObjectMapper   objectMapper = Jackson2ObjectMapperBuilder.json().build();

   @Override
   public void marshal(final Exchange theExchange, final Object theGraph, final OutputStream theOutputStream) throws Exception {
      final var anInMessage = theExchange.getIn();
      final var aPayload = new Payload<>((String) theGraph);
      aPayload.setCorrelationID((String) anInMessage.getHeader(CORRELATION_ID));
      anInMessage.getHeaders()
                 .entrySet()
                 .stream()
                 .filter(e -> e.getValue() != null && BeanUtils.isSimpleValueType(e.getValue().getClass()) && !e.getKey().startsWith("JMS"))
                 .forEach(e -> aPayload.setProperty(e.getKey(), new PropertyValue(e.getValue())));

      mapper.from(anInMessage.getHeader("JMSDeliveryMode")).whenNonNull().as(o -> (Integer) o).to(aPayload::setDeliveryMode);
      mapper.from(anInMessage.getHeader("JMSDestination")).whenNonNull().as(d -> this.toDestination((javax.jms.Destination) d)).to(aPayload::setDestination);
      mapper.from(anInMessage.getHeader("JMSExpiration")).whenNonNull().as(o -> (Long) o).to(aPayload::setExpiration);
      mapper.from(anInMessage.getHeader("JMSMessageID")).whenNonNull().as(o -> (String) o).to(aPayload::setMessageID);
      mapper.from(anInMessage.getHeader("JMSType")).whenNonNull().as(o -> (String) o).to(aPayload::setType);
      mapper.from(anInMessage.getHeader("JMSRedelivered")).whenNonNull().as(o -> (Boolean) o).to(aPayload::setRedelivered);
      mapper.from(anInMessage.getHeader("JMSTimestamp")).whenNonNull().as(o -> (Long) o).to(aPayload::setTimestamp);
      mapper.from(anInMessage.getHeader("JMSReplyTo")).whenNonNull().as(d -> this.toDestination((javax.jms.Destination) d)).to(aPayload::setReplyTo);

      this.objectMapper.writeValue(theOutputStream, aPayload);
   }

   @Override
   public Object unmarshal(final Exchange theExchange, final InputStream theInputStream) throws Exception {
      return this.objectMapper.readValue(theInputStream, Payload.class);
   }

   @Override
   public void start() {
      //No-op
   }

   @Override
   public void stop() {
      //No-op
   }

   private Destination toDestination(@NotNull final javax.jms.Destination theJMSDestination) {
      if (theJMSDestination == null) {
         // Null input; return null output
         return null;
      }
      return Stub.of(Destination.class, d -> {
         try {
            d.setDestinationType(theJMSDestination.getClass().getName());
            if (theJMSDestination instanceof Topic) {
               d.setName(((Topic) theJMSDestination).getTopicName());
            }
            else if (theJMSDestination instanceof Queue) {
               d.setName(((Queue) theJMSDestination).getQueueName());
            }
         }
         catch (JMSException e) {
            log.error("Unable to obtain JMS destination name from {}", ToStringBuilder.reflectionToString(theJMSDestination, SIMPLE_STYLE), e);
         }
      });
   }
}