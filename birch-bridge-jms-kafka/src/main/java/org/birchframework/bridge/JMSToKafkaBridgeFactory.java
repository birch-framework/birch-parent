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
import org.birchframework.dto.payload.DestinationType;
import org.birchframework.framework.beans.Beans;

import static org.apache.camel.LoggingLevel.INFO;
import static org.birchframework.bridge.TransactedPolicyType.*;
import static org.birchframework.configuration.BirchProperties.BridgeProperties.BridgeSource.JMS;
import static org.birchframework.dto.BirchErrorCode.B31031;

/**
 * Factory that creates JMS/Kafka message bridges.  Supports ActiveMQ, IBM MQ, and Tibco EMS.  Requires dependencies on exactly one of the 3
 * supported JMS providers.  Each of the supported JMS providers are configured via their own auto-configurations.  This programmatic
 * configuration merely provides the source/destination configuration, including their respective listeners.
 * @author Keivan Khalichi
 */
public class JMSToKafkaBridgeFactory extends AbstractBridgeFactory {

   private final SpringCamelContext context;
   private final MeterRegistry      meterRegistry;

   public JMSToKafkaBridgeFactory(final SpringCamelContext theContext, final MeterRegistry theMeterRegistry) {
      super(JMS);
      this.context       = theContext;
      this.meterRegistry = theMeterRegistry;
   }

   /** {@inheritDoc} */
   @Override
   @SuppressWarnings({"AutoBoxing", "unchecked", "DuplicatedCode"})
   public LambdaRouteBuilder createBridge(final String theName, final BridgeProperties theProperties, final BridgesGlobalConfigs theGlobalConfigs)
                             throws Exception {
      // Configure bridge consumer
      if (StringUtils.isNotBlank(theProperties.getJms().getQueue()) && theProperties.getJms().getQueue().equals(theProperties.getJms().getDeadLetterQueue()))
         throw new ConfigurationException(B31031);
      final var aQueueCF = Arrays.stream(this.context.getApplicationContext().getBeanNamesForType(QueueConnectionFactory.class)).findFirst().orElse(null);
      final var anIsTopicSource = theProperties.getJms().destination().getType() == DestinationType.TOPIC;
      final var aRouteCF = anIsTopicSource
                         ? Arrays.stream(this.context.getApplicationContext().getBeanNamesForType(TopicConnectionFactory.class)).findFirst().orElse(null)
                         : aQueueCF;

      final var anInGauge = this.registerGauge(theProperties, theName, String.format("%s.rate", METRIC_PREFIX),
                                               "Rate of incoming messages received per second, since last sampling",
                                               this.meterRegistry, Tag.of("state", "received"));
      final var anOutGauge = this.registerGauge(theProperties, theName, String.format("%s.rate", METRIC_PREFIX),
                                                "Rate of outgoing messages sent per second, since last sampling",
                                                this.meterRegistry, Tag.of("state", "sent"));
      final var anErrorGauge = this.registerGauge(theProperties, theName, String.format("%s.rate", METRIC_PREFIX),
                                                  "Rate of errors per second, since last sampling",
                                                  this.meterRegistry, Tag.of("state", "error"));

      final var aBridgeRoutePolicy = new BridgeRoutePolicy(anInGauge, anOutGauge);

      final var aFilterPredicate       = (Predicate<Exchange>) Beans.findBeanOrCreateInstance(theProperties.getFilterPredicate());
      final var anAfterReceiveConsumer = (Consumer<Exchange>) Beans.findBeanOrCreateInstance(theProperties.getAfterReceiveConsumer());
      final var aBeforeSendConsumer    = (Consumer<Exchange>) Beans.findBeanOrCreateInstance(theProperties.getBeforeSendConsumer());
      final var anErrorConsumer        = (Consumer<Exchange>) Beans.findBeanOrCreateInstance(theProperties.getErrorConsumer());

      final var aTXPolicyBeanRef       = anIsTopicSource ? TOPIC.getPolicyBeanName() : QUEUE.getPolicyBeanName();
      final var aDeadLetterQueue       = theProperties.getJms().getDeadLetterQueue();
      final var anErrorHandlerBuilder  = this.errorHandlerBuilder(
         theGlobalConfigs,
         theProperties.isTransacted() ? this.context.getApplicationContext().getBean(aTXPolicyBeanRef, SpringTransactionPolicy.class) : null,
         anErrorGauge,
         anErrorConsumer,
         StringUtils.isBlank(aDeadLetterQueue) ? null : () -> String.format("jms:queue:%s?connectionFactory=%s", aDeadLetterQueue, aQueueCF)
      );

      final String aPropertiesFilterPattern = this.propertiesFilterPattern(theProperties.getFilterProperties());

      final var aSourceProcessor = this.createSourceProcessor(theName, theProperties);

      final Supplier<String> aFromURI = () -> {
         var aURI = String.format("jms:%s:%s?connectionFactory=%s&acknowledgementModeName=CLIENT_ACKNOWLEDGE&disableReplyTo=true&maxConcurrentConsumers=%d",
                                  theProperties.getJms().destination().getDestinationType(), theProperties.getJms().destination().getName(),
                                  aRouteCF, theProperties.getConcurrentConsumers());

         if (StringUtils.isNotBlank(theProperties.getJms().getSelector())) {
            aURI = String.format("%s&selector=%s", aURI, theProperties.getJms().getSelector());
         }
         return aURI;
      };

      return rb -> {
         // Route definition
         ProcessorDefinition<?> route = rb.from(aFromURI.get())
                                          .routeId(theName)
                                          .autoStartup(theGlobalConfigs.isAutoStart())
                                          .errorHandler(anErrorHandlerBuilder)
                                          .routePolicy(aBridgeRoutePolicy);
         route = Beans.invokeIfNotNull(aTXPolicyBeanRef, route::transacted, route);
         route = aFilterPredicate == null ? route : route.filter(aFilterPredicate::test);
         route = route.process().message(aSourceProcessor::processCorrelationID)
                      .log(INFO, "Incoming message: Headers: ${headers}; Body: ${bodyOneLine}");
         route = Beans.invokeIfNotNull(aPropertiesFilterPattern, route::removeHeaders, route);
         route = Beans.invokeIfNotNull(anAfterReceiveConsumer, route.process()::exchange, route);
         route = route.process(aSourceProcessor)
                      .marshal().custom(PayloadDataFormat.BEAN_NAME);
         route = Beans.invokeIfNotNull(aBeforeSendConsumer, route.process()::exchange, route);
         route.log(INFO, "Outgoing message: Body: ${bodyOneLine}")
              .toF("kafka:%s", theProperties.getKafka().getTopic())
              .stop();
      };
   }

   @Override
   protected SourceProcessor createSourceProcessor(final String theName, final BridgeProperties theProperties) {
      return new JMSSourceProcessor(theProperties);
   }
}