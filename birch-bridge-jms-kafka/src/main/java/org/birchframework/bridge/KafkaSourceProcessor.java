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
package org.birchframework.bridge;

import java.util.UUID;
import java.util.function.Consumer;
import org.birchframework.dto.ContextMapKeys;
import org.birchframework.dto.payload.Payload;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.lang3.StringUtils;
import org.birchframework.configuration.BirchProperties;
import org.slf4j.MDC;

import static org.apache.camel.Exchange.CORRELATION_ID;
import static org.apache.camel.component.kafka.KafkaConstants.*;

/**
 * Custom Kafka consumer.  Must be subclassed for use.  This class contains the complete logic of bridging Kafka to JMS.
 * @author Keivan Khalichi
 */
public class KafkaSourceProcessor implements SourceProcessor {

   private final Consumer<Message> keyConsumer;
   private final Consumer<Message> correlationIDConsumer;

   /**
    * Dynamically configures a Kafka consumer.
    * @param theProperties the directional properties of the bridge
    */
   public KafkaSourceProcessor(final BirchProperties.BridgeProperties theProperties) {
      this.keyConsumer           = this.keyConsumer(theProperties);
      this.correlationIDConsumer = this.correlationIDConsumer(theProperties);
   }

   @Override
   @SuppressWarnings({"DuplicatedCode", "unchecked"})
   public void process(final Exchange theExchange) {
      final var anInMessage = theExchange.getIn();
      final var aPayload = (Payload<String>) anInMessage.getBody();
      aPayload.propertyNames().stream().filter(name -> !name.startsWith("JMS")).forEach(name -> anInMessage.setHeader(name, aPayload.getProperty(name).value()));
      anInMessage.removeHeaders("JMS*");
      anInMessage.removeHeaders(HEADERS);
      this.keyConsumer.accept(anInMessage);
      anInMessage.setBody(aPayload.getText());
   }

   @Override
   public void processCorrelationID(final Message theMessage) {
      this.correlationIDConsumer.accept(theMessage);
      MDC.put(ContextMapKeys.CORRELATION_ID, theMessage.getHeader(CORRELATION_ID, () -> "", String.class));
   }

   private Consumer<Message> keyConsumer(final BirchProperties.BridgeProperties theProperties) {
      final var aKeyProperty = theProperties.getJms().getKeyProperty();
      if (StringUtils.isNotBlank(theProperties.getJms().getKeyProperty())) {
         return (message) -> message.setHeader(aKeyProperty, (String) message.getHeader(KEY));
      }
      else {
         return (message) -> {};
      }
   }

   @SuppressWarnings("unchecked")
   private Consumer<Message> correlationIDConsumer(final BirchProperties.BridgeProperties theProperties) {
      final var aCorrelationIDProperty = theProperties.getJms().getCorrelationIdProperty();
      if (StringUtils.isBlank(aCorrelationIDProperty)) {
         if (theProperties.getJms().isOverrideCorrelationID()) {
            return message -> message.setHeader(CORRELATION_ID, UUID.randomUUID().toString());
         }
         return message -> message.setHeader(CORRELATION_ID, ((Payload<String>) message.getBody()).getCorrelationID());
      }
      else {
         return message -> message.setHeader(CORRELATION_ID,  ((Payload<String>) message.getBody()).getProperty(aCorrelationIDProperty).value());
      }
   }
}