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

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import org.birchframework.configuration.BirchProperties;
import org.apache.camel.spring.boot.SpringBootCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.birchframework.configuration.BirchProperties.BridgeProperties.BridgeSource.JMS;
import static org.birchframework.dto.payload.DestinationType.TOPIC;

/**
 * Utility class for JMS bridge tests.
 * @author Keivan Khalichi
 */
@SuppressWarnings("AutoBoxing")
public class JMSTestUtils {

   private static final String PRODUCER_OPTIONS = "deliveryPersistent=false&deliveryMode=1&priority=1";
   private static final Logger log = LoggerFactory.getLogger(JMSTestUtils.class);

   static void executeSendMessage(final int theIterations, final BirchProperties theProperties, final SpringBootCamelContext theContext,
                                  final boolean theIsBytesMessage) {
      final var aSampleMessage = "Hello World!";
      final var aProducerTemplate = theContext.createProducerTemplate();
      final var aQueueCF = Arrays.stream(theContext.getApplicationContext().getBeanNamesForType(TopicConnectionFactory.class)).findFirst().orElse(null);
      final var aTopicCF = Arrays.stream(theContext.getApplicationContext().getBeanNamesForType(QueueConnectionFactory.class)).findFirst().orElse(null);
      IntStream.range(0, theIterations).forEach(
         i -> theProperties.getBridges()
                           .entrySet()
                           .stream()
                           .filter(e -> e.getValue().getSource() == JMS)
                           .forEach(e -> {
                              final var aRouteCF = e.getValue().getJms().destination().getType() == TOPIC ? aTopicCF : aQueueCF;
                              final var aDestination = e.getValue().getJms().destination();
                              final var aURI = String.format("jms:%s:%s?connectionFactory=%s&%s",
                                                             aDestination.getDestinationType(), aDestination.getName(), aRouteCF, PRODUCER_OPTIONS);
                              aProducerTemplate.sendBodyAndHeaders(
                                    aURI,
                                    theIsBytesMessage ? aSampleMessage.getBytes() : aSampleMessage,
                                    Map.of(
                                       "corrID", UUID.randomUUID().toString(),
                                       "key", "test-key",
                                       "index", i,
                                       "isTrue", true
                                    )
                              );
                              log.info("Message {} sent to {}: {}",
                                       i, e.getValue().getJms().destination().getDestinationType(), e.getValue().getJms().destination().getName());
                           })
      );
   }
}