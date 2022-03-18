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

package org.birchframework.bridge;

import org.apache.camel.builder.LambdaRouteBuilder;
import org.birchframework.dto.payload.Payload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Spring configuration for {@link ActiveMQBridgeTest}.
 * @author Keivan Khalichi
 */
@EnableAutoConfiguration
@Import(ActiveMQAutoConfiguration.class)
public class TestConfiguration {

   public static final String MESSAGE_RECEIVED_URI = "activemq:queue:message-received";

   @SuppressWarnings({"unchecked", "AutoBoxing"})
   @Bean
   LambdaRouteBuilder kafkaConsumerRoute(@Value("${spring.embedded.kafka.brokers}") final String theBrokers,
                                         @Value("${birch.bridges.test-1-in.kafka.topic}") final String theKafkaTopic) {

      return rb -> {
         rb.fromF("kafka:%s?brokers=%s", theKafkaTopic, theBrokers)
           .routeId("test-kafka-consumer")
           .log("Incoming Kafka message: key: ${headers[kafka.KEY]}, payload: ${body}")
           .unmarshal().json(Payload.class)
           .transform().body(payload -> ((Payload<String>) payload).getText())
           .transform().jsonpath("$.id")
           .choice()
             .when(exchange -> exchange.getIn().getBody(Integer.class).equals(-40))
               .to(MESSAGE_RECEIVED_URI)
             .otherwise()
               .log("Not the message we were expecting")
           .end();

         rb.from(MESSAGE_RECEIVED_URI)
           .routeId("message-received")
           .log("Message received");
      };
   }
}
