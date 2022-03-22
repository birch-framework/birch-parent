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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.jms.JMSException;
import com.google.common.base.Throwables;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.DeadLetterChannelBuilder;
import org.apache.camel.builder.DefaultErrorHandlerBuilder;
import org.apache.camel.builder.ErrorHandlerBuilder;
import org.apache.camel.builder.LambdaRouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.CamelLogger;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.apache.camel.spring.spi.TransactionErrorHandlerBuilder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.birchframework.configuration.BirchProperties.BridgeProperties;
import org.birchframework.configuration.BirchProperties.BridgesGlobalConfigs;
import org.birchframework.framework.metric.RateGauge;
import org.springframework.util.CollectionUtils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.camel.Exchange.*;
import static org.apache.camel.LoggingLevel.WARN;
import static org.apache.commons.lang3.builder.ToStringStyle.SIMPLE_STYLE;

/**
 * Interface for bridge factories for use by SPI.
 * @author Keivan Khalichi
 */
@Getter
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractBridgeFactory {

   public static final String METRIC_PREFIX = "birch.bridge.message";
   public static final String BRIDGE_TAG    = "bridge";

   private final BridgeProperties.BridgeSource source;

   /**
    * Create bridge route and register it with the Camel context.
    * @param theName the bean name
    * @param theProperties the bridge properties
    * @param theGlobalConfigs whether bridge is auto-started at creation
    * @return the newly created and registered route
    */
   @SuppressWarnings("UnusedReturnValue")
   public abstract LambdaRouteBuilder createBridge(String theName, BridgeProperties theProperties, BridgesGlobalConfigs theGlobalConfigs)
                   throws Exception;

   protected abstract SourceProcessor createSourceProcessor(String theName, BridgeProperties theProperties);

   @SuppressWarnings({"UnusedReturnValue", "VariableArgumentMethod"})
   protected RateGauge registerGauge(final BridgeProperties theProperties, final String theBridgeName,
                                     final String theMetricName, final String theMetricDescription, final MeterRegistry theMeterRegistry,
                                     final Tag... theAdditionalTags) {
      final List<Tag> aTags = new ArrayList<>(ArrayUtils.isEmpty(theAdditionalTags) ? Collections.emptyList() : Arrays.asList(theAdditionalTags));
      aTags.add(Tag.of(BRIDGE_TAG, theBridgeName));
      aTags.add(Tag.of("source.type", theProperties.getSource().toString()));
      if (theProperties.getBridgeType() != null) {
         aTags.add(Tag.of("bridge.type", theProperties.getBridgeType().toString()));
      }

      switch (theProperties.getSource()) {
         case JMS:
            aTags.add(Tag.of("source", theProperties.getJms().destination().getName()));
            aTags.add(Tag.of("target", theProperties.getKafka().getTopic()));
            break;
         case KAFKA:
            aTags.add(Tag.of("source", theProperties.getKafka().getTopic()));
            aTags.add(Tag.of("target", theProperties.getJms().destination().getName()));
            break;
      }

      return RateGauge.builder()
                      .withName(theMetricName)
                      .withDescription(theMetricDescription)
                      .withTags(aTags)
                      .withRegistry(theMeterRegistry)
                      .register();
   }

   protected ErrorHandlerBuilder errorHandlerBuilder(final BridgesGlobalConfigs theGlobalConfigs,
                                                     @Nullable SpringTransactionPolicy theTXPolicy,
                                                     final RateGauge theErrorGauge,
                                                     @Nullable final Consumer<Exchange> theErrorConsumer,
                                                     @Nullable final Supplier<String> theDeadLetterURISupplier) {
      final DefaultErrorHandlerBuilder anErrorHandlerBuilder;
      if (theDeadLetterURISupplier == null) {
         if (theTXPolicy == null) {
            anErrorHandlerBuilder = new DefaultErrorHandlerBuilder();
         }
         else {
            // Create transaction error handler
            final var aTXErrorHandlerBuilder = new TransactionErrorHandlerBuilder();
            aTXErrorHandlerBuilder.setSpringTransactionPolicy(theTXPolicy);
            anErrorHandlerBuilder = aTXErrorHandlerBuilder;
         }
      }
      else {
         // Create dead letter channel handler
         anErrorHandlerBuilder = new DeadLetterChannelBuilder(theDeadLetterURISupplier.get());
      }

      if (theGlobalConfigs.getMaxRedeliveries() == 0) {
         anErrorHandlerBuilder.onExceptionOccurred(exchange -> this.processRedelivery(exchange, theErrorGauge, theErrorConsumer, true));
      }
      else {
         anErrorHandlerBuilder.maximumRedeliveries(theGlobalConfigs.getMaxRedeliveries())
                              .redeliveryDelay(theGlobalConfigs.getRedeliveryDelay().toMillis())
                              .retriesExhaustedLogLevel(WARN)
                              .useOriginalMessage()
                              .logExhausted(true)
                              .logRetryAttempted(true)
                              .onRedelivery(exchange -> this.processRedelivery(exchange, theErrorGauge, theErrorConsumer, false));

         if (theGlobalConfigs.isExponentialBackOff()) {
            anErrorHandlerBuilder.useExponentialBackOff()
                                 .maximumRedeliveryDelay(theGlobalConfigs.getMaximumRedeliveryDelay().toMillis());
         }
      }

      anErrorHandlerBuilder.logExhaustedMessageHistory(false)
                           .logNewException(false)
                           .logHandled(false)
                           .logStackTrace(false)
                           .logRetryStackTrace(false)
                           .logger(new CamelLogger(log));

      return anErrorHandlerBuilder;
   }

   protected String propertiesFilterPattern(final Set<String> thePropertyNames) {
      if (CollectionUtils.isEmpty(thePropertyNames)) {
         return null;
      }
      // Match header keys except for the ones to filter; so it will remove all other headers
      return String.format("^(?!%s).*$", thePropertyNames.stream().map(prop -> String.format("%s$", prop.strip())).collect(Collectors.joining("|")));
   }

   private void processRedelivery(final Exchange theExchange, final RateGauge theErrorGauge,
                                  final Consumer<Exchange> theErrorConsumer, final boolean theIsFastFail) {
      final var aMessage = theExchange.getIn();
      if (theIsFastFail || aMessage.getHeader(REDELIVERY_COUNTER).equals(aMessage.getHeader(REDELIVERY_MAX_COUNTER))) {
         theErrorGauge.increment();
         final var anException = ObjectUtils.defaultIfNull(theExchange.getException(), aMessage.getHeader(EXCEPTION_CAUGHT, Throwable.class));
         if (anException == null) {
            log.error("Error in MessageId: {}; Redeliveries: {}; Headers: {}; Body: {}",
                      aMessage.getMessageId(),
                      theIsFastFail ? 0 : aMessage.getHeader(REDELIVERY_COUNTER),
                      aMessage.getHeaders().toString(),
                      this.bodyString(aMessage.getBody()));
         }
         else if (anException instanceof JMSException) {
            final var aLinkedException = ((JMSException) anException).getLinkedException();
            log.error("{}: {}; MessageId: {}; Linked exception: {}: {}; Redeliveries: {}; Headers: {}; Body: {}",
                      anException.getClass().getName(),
                      Throwables.getRootCause(anException).getMessage(),
                      aMessage.getMessageId(),
                      aLinkedException.getClass().getName(),
                      aLinkedException.getMessage(),
                      theIsFastFail ? 0 : aMessage.getHeader(REDELIVERY_COUNTER),
                      aMessage.getHeaders().toString(),
                      this.bodyString(aMessage.getBody()));
         }
         else {
            log.error("{}: {}; MessageId: {}; Redeliveries: {}; Headers: {}; Body: {}",
                      anException.getClass().getName(),
                      Throwables.getRootCause(anException).getMessage(),
                      aMessage.getMessageId(),
                      theIsFastFail ? 0 : aMessage.getHeader(REDELIVERY_COUNTER),
                      aMessage.getHeaders().toString(),
                      this.bodyString(aMessage.getBody()));
         }
         if (theErrorConsumer != null) {
            theErrorConsumer.accept(theExchange);
         }
      }
   }

   protected RouteDefinition addBridgePolicy(final RouteDefinition theRoute, final RateGauge theInGauge, final RateGauge theOutGauge) {
      try(var aBridgeRoutePolicy = new BridgeRoutePolicy(theInGauge, theOutGauge)) {
         return theRoute.routePolicy(aBridgeRoutePolicy);
      }
      catch (Exception e) {
         final var aRootCause = Throwables.getRootCause(e);
         log.warn("Exception occurred configuring route policy; Exception: {}; Message: {}", aRootCause.getClass().getName(), aRootCause.getMessage());
      }
      return theRoute;
   }

   private String bodyString(final Object aBody) {
      if (aBody == null) {
         return null;
      }
      if (aBody instanceof String) {
         return (String) aBody;
      }
      if (aBody instanceof byte[]) {
         return StringUtils.toEncodedString((byte[]) aBody, UTF_8);
      }
      if (aBody instanceof Map) {
         return aBody.toString();
      }
      return ToStringBuilder.reflectionToString(aBody, SIMPLE_STYLE);
   }
}