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

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.birchframework.configuration.BirchProperties;
import org.birchframework.configuration.ConfigurationException;
import org.birchframework.dto.ContextMapKeys;
import org.slf4j.MDC;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static org.apache.camel.Exchange.CORRELATION_ID;
import static org.birchframework.dto.BirchErrorCode.*;

/**
 * Custom JMS consumer.  Must be subclassed for use.  This class contains the complete logic of bridging JMS to Kafka.
 * @author Keivan Khalichi
 */
public class JMSSourceProcessor implements SourceProcessor {

   private static final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

   private final Function<String, String>            bodyFormatFunction;
   private final BiFunction<Message, String, String> keyFunction;
   final         Consumer<Message>                   correlationIDConsumer;
   private final Function<Message, Optional<String>> extractJMSBodyFunction;

   /**
    * Dynamically configures a JMS consumer.
    * @param theProperties the directional properties of the bridge
    */
   public JMSSourceProcessor(final BirchProperties.BridgeProperties theProperties) {

      if (StringUtils.isNotBlank(theProperties.getJms().getKeyProperty()) && StringUtils.isNotBlank(theProperties.getJms().getKeyRegex())) {
         throw new ConfigurationException(B31032);
      }

      this.bodyFormatFunction     = this.formatFunction(theProperties);
      this.keyFunction            = this.keyFunction(theProperties);
      this.correlationIDConsumer  = this.correlationIDConsumer(theProperties);
      this.extractJMSBodyFunction = this.extractJMSBodyFunction(theProperties);
   }

   /**
    * Processes incoming JMS message and produces associated Kafka message.
    * @param theExchange
    */
   @Override
   @SuppressWarnings("DuplicatedCode")
   public void process(final Exchange theExchange) {
      // Log incoming message if configured to do so
      final var anInMessage = theExchange.getIn();

      final var aJMSBody = this.extractJMSBodyFunction.apply(anInMessage);
      aJMSBody.ifPresent(jmsBody -> {
         anInMessage.setBody(this.bodyFormatFunction.apply(jmsBody));

         // Determine if key is present; send message with or without it
         anInMessage.setHeader(KafkaConstants.KEY, this.keyFunction.apply(anInMessage, jmsBody));
      });
   }

   @Override
   public void processCorrelationID(final Message theMessage) {
      this.correlationIDConsumer.accept(theMessage);
      MDC.put(ContextMapKeys.CORRELATION_ID, theMessage.getHeader(CORRELATION_ID, () -> "", String.class));
   }

   private Function<String, String> formatFunction(final BirchProperties.BridgeProperties theProperties) {
      return theProperties.isStripNewline() ? body -> body.replaceAll("\\R", "") : body -> body;
   }

   private BiFunction<Message, String, String> keyFunction(final BirchProperties.BridgeProperties theProperties) {
      final var aKeyRegex = theProperties.getJms().getKeyRegex();
      if (StringUtils.isNotBlank(aKeyRegex)) {
         final var aKeyPattern  = Pattern.compile(aKeyRegex);
         final var aKeyRegexCapture = theProperties.getJms().getKeyRegexCapture();
         return (message, payload) -> {
            final var aMatcher = aKeyPattern.matcher(payload);
            if (aMatcher.matches()) {
               return aMatcher.group(aKeyRegexCapture);
            }
            return null;
         };
      }
      else {
         final var aKeyProperty = theProperties.getJms().getKeyProperty();
         if (StringUtils.isBlank(aKeyProperty)) {
            return (m, p) -> null;
         }
         return  (message, payload) -> (String) message.getHeader(aKeyProperty);
      }
   }

   private Consumer<Message> correlationIDConsumer(final BirchProperties.BridgeProperties theProperties) {
      final var aCorrelationIDProperty = theProperties.getJms().getCorrelationIdProperty();
      if (StringUtils.isBlank(aCorrelationIDProperty)) {
         if (theProperties.getJms().isOverrideCorrelationID()) {
            return message -> message.setHeader(CORRELATION_ID, UUID.randomUUID().toString());
         }
         else {
            return message -> {};
         }
      }
      if (theProperties.getJms().isOverrideCorrelationID()) {
         return message -> message.setHeader(CORRELATION_ID, UUID.randomUUID().toString());
      }
      return message -> message.setHeader(CORRELATION_ID, message.getHeader(aCorrelationIDProperty));
   }

   private Function<Message, Optional<String>> extractJMSBodyFunction(final BirchProperties.BridgeProperties theBridgeProperties) {
      switch (theBridgeProperties.getJms().getMessageType()) {
         case TEXT:
            return message -> {
               final var aBody = (String) message.getBody();
               return StringUtils.isEmpty(aBody) ? Optional.empty() : Optional.of(aBody);
            };
         case MAP:
         case OBJECT:
            return message -> {
               final var aBody = message.getBody();
               try {
                  return aBody == null ? Optional.empty() : Optional.of(objectMapper.writeValueAsString(aBody));
               }
               catch (JsonProcessingException e) {
                  throw new ProcessingException(B31130, e);
               }
            };
         case BYTES:
            return message -> {
               final var aBody = (byte[]) message.getBody();
               return ArrayUtils.isEmpty(aBody) ? Optional.empty() : Optional.of(new String(aBody, StandardCharsets.UTF_8));
            };
         default:
            throw new ConfigurationException(B31046);
      }
   }
}