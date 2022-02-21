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
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.apache.camel.Exchange;
import org.apache.camel.builder.LambdaRouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.apache.commons.lang3.StringUtils;
import org.birchframework.bridge.dataformat.PayloadDataFormat;
import org.birchframework.configuration.BirchProperties.BridgeProperties;
import org.birchframework.configuration.BirchProperties.BridgesGlobalConfigs;
import org.birchframework.configuration.ConfigurationException;
import org.birchframework.framework.beans.Beans;
import org.birchframework.dto.payload.DestinationType;

import static org.apache.camel.LoggingLevel.INFO;
import static org.birchframework.bridge.TransactedPolicyType.*;
import static org.birchframework.configuration.BirchProperties.BridgeProperties.BridgeSource.KAFKA;
import static org.birchframework.dto.BirchErrorCode.*;

/**
 * Factory that creates Kafka/JMS message bridges.  Supports ActiveMQ, IBM MQ, and Tibco EMS JMS brokers.  Requires dependencies on exactly
 * one of the 3 supported JMS providers.  Each of the supported JMS providers are configured via their own auto-configurations.  This
 * programmatic configuration merely provides the source/destination configuration, including their respective listeners.
 * @author Keivan Khalichi
 */
public class KafkaToJMSBridgeFactory extends AbstractBridgeFactory {

   private final SpringCamelContext context;
   private final MeterRegistry      meterRegistry;

   public KafkaToJMSBridgeFactory(final SpringCamelContext theContext, final MeterRegistry theMeterRegistry) {
      super(KAFKA);
      this.context       = theContext;
      this.meterRegistry = theMeterRegistry;
   }

   /** {@inheritDoc} */
   @Override
   @SuppressWarnings({"AutoBoxing", "unchecked", "DuplicatedCode"})
   public LambdaRouteBuilder createBridge(final String theName, final BridgeProperties theProperties, final BridgesGlobalConfigs theGlobalConfigs)
                             throws Exception {
      // Configure bridge consumer
      if (StringUtils.isBlank(theProperties.getKafka().getTopic())) {
         throw new ConfigurationException(B31040);
      }
      if (theProperties.getKafka().getTopic().equals(theProperties.getKafka().getDeadLetterTopic())) {
         throw new ConfigurationException(B31043);
      }
      final var anIsTopicTarget = theProperties.getJms().destination().getType() == DestinationType.TOPIC;
      final var aRouteCF = anIsTopicTarget
                         ? Arrays.stream(context.getApplicationContext().getBeanNamesForType(TopicConnectionFactory.class)).findFirst().orElse(null)
                         : Arrays.stream(context.getApplicationContext().getBeanNamesForType(QueueConnectionFactory.class)).findFirst().orElse(null);

      // CPD-OFF
      final var anInGauge = this.registerGauge(theProperties, theName, String.format("%s.rate", METRIC_PREFIX),
                                               "Rate of incoming messages received per second, since last sampling",
                                               this.meterRegistry, Tag.of("state", "received"));
      final var anOutGauge = this.registerGauge(theProperties, theName, String.format("%s.rate", METRIC_PREFIX),
                                                "Rate of outgoing messages sent per second, since last sampling",
                                                this.meterRegistry, Tag.of("state", "sent"));
      final var anErrorGauge = this.registerGauge(theProperties, theName, String.format("%s.rate", METRIC_PREFIX),
                                                  "Rate of errors per second, since last sampling",
                                                  this.meterRegistry, Tag.of("state", "error"));

      final var aFilterPredicate       = (Predicate<Exchange>) Beans.findBeanOrCreateInstance(theProperties.getFilterPredicate());
      final var anAfterReceiveConsumer = (Consumer<Exchange>) Beans.findBeanOrCreateInstance(theProperties.getAfterReceiveConsumer());
      final var aBeforeSendConsumer    = (Consumer<Exchange>) Beans.findBeanOrCreateInstance(theProperties.getBeforeSendConsumer());
      final var anErrorConsumer        = (Consumer<Exchange>) Beans.findBeanOrCreateInstance(theProperties.getErrorConsumer());

      final var aTXPolicyBeanRef       = anIsTopicTarget ? TOPIC.getPolicyBeanName() : QUEUE.getPolicyBeanName();
      final var aDeadLetterTopic       = theProperties.getKafka().getDeadLetterTopic();
      final var anErrorHandlerBuilder  = this.errorHandlerBuilder(
         theGlobalConfigs,
         theProperties.isTransacted() ? this.context.getApplicationContext().getBean(aTXPolicyBeanRef, SpringTransactionPolicy.class) : null,
         anErrorGauge,
         anErrorConsumer,
         StringUtils.isBlank(aDeadLetterTopic) ? null : () -> String.format("kafka:%s", aDeadLetterTopic)
      );

      final String aPropertiesFilterPattern = this.propertiesFilterPattern(theProperties.getFilterProperties());

      final var aSourceProcessor = this.createSourceProcessor(theName, theProperties);

      final Supplier<String> aFromURI = () -> {
         var aURI = String.format("kafka:%1$s?groupId=%2$s&consumersCount=%3$d",
                                  theProperties.getKafka().getTopic(), StringUtils.defaultIfBlank(theProperties.getKafka().getGroupId(), theName),
                                  theProperties.getConcurrentConsumers());
         if (StringUtils.isNotBlank(theProperties.getKafka().getListenerId())) {
            aURI = String.format("%s&clientId=%s", aURI, theProperties.getKafka().getListenerId());
         }
         return aURI;
      };

      return rb -> {
         // Route definition
         ProcessorDefinition<?> route = rb.from(aFromURI.get())
                                          .routeId(theName)
                                          .autoStartup(theGlobalConfigs.isAutoStart())
                                          .errorHandler(anErrorHandlerBuilder);
         route = Beans.invokeIfNotNull(aTXPolicyBeanRef, route::transacted, route);
         route = aFilterPredicate == null ? route : route.filter(aFilterPredicate::test);
         route = route.process().body(body -> anInGauge.increment())
                      .log(INFO, "Incoming message: Body: ${bodyOneLine}");
         route = Beans.invokeIfNotNull(anAfterReceiveConsumer, route.process()::exchange, route);
         route = route.unmarshal().custom(PayloadDataFormat.BEAN_NAME)
                      .process().message(aSourceProcessor::processCorrelationID)
                      .process(aSourceProcessor);
         route = Beans.invokeIfNotNull(aPropertiesFilterPattern, route::removeHeaders, route);
         route = Beans.invokeIfNotNull(aBeforeSendConsumer, route.process()::exchange, route);
         route.log(INFO, "Outgoing message: Headers: ${headers}; Body: ${bodyOneLine}")
              .toF("jms:%s:%s?connectionFactory=%s&deliveryMode=2",
                   theProperties.getJms().destination().getDestinationType(), theProperties.getJms().destination().getName(), aRouteCF)
              .process().body(body -> anOutGauge.increment())
              .stop();
      };
      // CPD-ON
   }

   @Override
   protected SourceProcessor createSourceProcessor(final String theName, final BridgeProperties theProperties) {
      return new KafkaSourceProcessor(theProperties);
   }
}