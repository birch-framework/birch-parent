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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.jms.ConnectionFactory;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.LambdaRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.zookeeper.ZooKeeperMessage;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.birchframework.bridge.dataformat.ServiceMessageDataFormat;
import org.birchframework.configuration.BirchProperties;
import org.birchframework.configuration.BirchProperties.BridgeProperties;
import org.birchframework.configuration.BirchProperties.BridgeProperties.BridgeSource;
import org.birchframework.configuration.BirchProperties.BridgesGlobalConfigs;
import org.birchframework.configuration.ConfigurationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.CollectionUtils;

import static org.apache.camel.LoggingLevel.DEBUG;
import static org.birchframework.dto.BirchErrorCode.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Auto-configuration for the bridge(s).  Supports ActiveMQ, IBM MQ, and Tibco EMS.  Requires dependencies on exactly one of the 3
 * supported JMS providers.  Each of the supported JMS providers are configured via their own auto-configurations.  This auto-configuration merely
 * provides the source/destination configuration, including their respective listeners.
 * The configuration supports multiple bridges, as long as the underlying JMS provider for all bridges is one of the aforementioned.
 * <p/>
 * Available configurations are:
 * <pre>
 * birch:
 *   kafka:
 *     admin:
 *       zookeeper-servers: zk1.aws.com:2181             # Comma separated set of Zookeeper server address:port(s); when defined, active affinity is persisted in Zookeeper
 *   bridges-global-configs:                             # Global configs for bridges
 *     max-redeliveries: 0                               # Maximum number of redelivery attempts when the bridge route encounters an error; defaults to 0, which means no redelivery attempt; has no effect if DLQ/T is not specified in bridge
 *     redelivery-delay: 1s                              # Fixed interval of time between redelivery retries when the bridge route encounters an error; defaults to 1 second; has no effect if DLQ/T is not specified in bridge
 *     max-redelivery-delay: 1m                          # Max interval of time between redelivery retries when exponential-backoff is true; defaults to 1 minute; has no effect if DLQ/T is not specified in bridge
 *     exponential-backoff: false                        # Exponentially increase delay interval of time between retries by factor of 2.0 up to max-redelivery-delay; defaults to false
 *     affinity: region-1                                # Specifies an affinity to which the bridges defined by this microservice are associated, which dictates that bridges react to service actions for this affinity, only
 *     service-topic: common-service-topic               # Topic for sending and receiving service actions, such as bridge start/stop
 *     auto-start: true                                  # Determines whether or not bridges are to be started upon Spring Boot application startup; defaults to true
 *     zookeeper-base-path: /birch/bridges               # Base path under which the 'active-affinity' znode will be stored within Zookeeper; defaults to '/birch/bridges'
 *   bridges:                                            # Definition of bridges
 *     my-jms-bridge:                                    # Bridge name; bridge definition defined below is for this bridge
 *       enabled: true                                   # When true, bridge is created by the service, when false, bridge is not created; defaults to true
 *       source: jms                                     # Bridge source type: jms or kafka
 *       strip-newline: true                             # when true, strips newline from the entire payload; defaults to true
 *       filter-properties: prop1,prop2                  # Comma separated set of header properties of the incoming JMS message to forward to the target; all other properties are removed; optional
 *       after-receive-consumer: io.jms.AfterReceive     # FQCN of a {@code Consumer<Exchange>} implementation that provides a hook to perform pre-processing of an exchange after being received by the bridge; optional
 *       before-send-consumer: io.jms.BeforeSend         # FQCN of a {@code Consumer<Exchange>} implementation that provides a hook to perform pre-processing of an exchange before being sent by the bridge; optional
 *       error-consumer: io.jms.WhenError                # FQCN of a {@code Consumer<Exchange>} implementation that provides a hook to perform processing of an exchange when an exception occurs within the bridge route; optional
 *       concurrent-consumers: 1                         # Number of concurrent JMS listeners; defaults to 1 (note, min value is always 1)
 *       transacted: true                                # When true, the bridge operates in a transactional fashion; defaults to true
 *       jms:                                            # Since source is JMS, this JMS definition is for a JMS consumer
 *         queue: test-queue-in                          # Queue name from which this consumer receives messages; also specifying topic will throw an exception
 *         topic: test-topic-in                          # Topic name from which this consumer receives messages; also specifying queue will throw an exception
 *         key-property: key                             # JMS property to use for the Kafka topic key; also specifying key-regex will throw an exception; optional
 *         key-regex: .*((World)!)$                      # Extracts Kafka topic key value from payload via regular expression; also specifying key-property will throw an exception; optional
 *         key-regex-capture: 2                          # Extracts value in key-regex from the specified capture group; defaults to 0, which returns the entire matched regex
 *         correlation-id-property:                      # JMS property from which to obtain the correlation ID
 *         override-correlation-id: true                 # When true, overrides JMS correlation ID with a UUID generated at the time of message consumption; defaults to true
 *         selector: someProperty IS NOT NULL            # JMS message selector; see this <a href="https://timjansen.github.io/jarfiller/guide/jms/selectors.xhtml">documentation<a/> on how to work with message selectors; optional
 *         message-type: text                            # Type of the incoming JMS message body; supported values are: text, bytes, object, map; defaults to text
 *         dead-letter-queue: dlq-1                      # When specified, messages that produce exceptions will be sent to this JMS queue; optional
 *       kafka:                                          # Since source is JMS, this Kafka definition is for a Kafka producer
 *         topic: test-topic-in                          # Kafka topic
 *     my-kafka-bridge:                                  # Bridge name; bridge definition defined below is for this bridge
 *       source: kafka                                   # Bridge source type: jms or kafka
 *       strip-newline: true                             # When true, strips newline from the entire payload; defaults to true
 *       after-receive-consumer: io.kafka.AfterReceive   # FQCN of a {@code Consumer<Exchange>} implementation that provides a hook to perform pre-processing of an exchange after being received by the bridge; optional
 *       before-send-consumer: io.kafka.BeforeSend       # FQCN of a {@code Consumer<Exchange>} implementation that provides a hook to perform pre-processing of an exchange before being sent by the bridge; optional
 *       error-consumer: io.jms.WhenError                # FQCN of a {@code Consumer<Exchange>} implementation that provides a hook to perform processing of an exchange when an exception occurs within the bridge route; optional
 *       concurrent-consumers: 1                         # Number of Kafka consumers; should match the number of partitions on the Kafka topic; defaults to 1
 *       kafka:                                          # Since source is Kafka, this Kafka definition is for a Kafka consumer
 *         topic: test-topic-out                         # Kafka topic from which this consumer receives messages
 *         listener-id: bridge1Consumer                  # Optionally sets listener ID of this Kafka consumer; optional; providing this value in a multi-consumer configuration can be problematic
 *         group-id: bridge-kafka                        # Optionally sets the group ID, defaults to the listener-id (e.g. bridge1Consumer in this example)
 *         dead-letter-topic: dlt-1                      # When specified, messages that produce exceptions will be sent to this Kafka topic; optional
 *       jms:                                            # Since source is Kafka, this JMS definition is for a JMS producer
 *         queue: test-queue-out                         # Queue name to which this produces sends messages; also specifying topic will throw an exception
 *         topic: test-topic-in                          # Topic name to which this producer sends messages; also specifying queue will throw an exception
 *         key-property: key                             # Optionally sets the Kafka topic key to the outgoing JMS property as a String, when the key value is not null
 *         override-correlation-id: true                 # When true, overrides JMS correlation ID with a UUID generated at the time of message production; defaults to true
 * </pre>
 * @author Keivan Khalichi
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackages = {
   "org.birchframework.bridge", "org.birchframework.bridge.dataformat",
   "org.birchframework.framework.spring",
   "org.springframework.boot.autoconfigure.jackson"})
@EnableAutoConfiguration
@EnableConfigurationProperties(BirchProperties.class)
@AutoConfigureBefore(CamelAutoConfiguration.class)
@AutoConfigureAfter(name = {"org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration",
                            "com.ibm.mq.spring.boot.MQAutoConfiguration", "org.birchframework.ems.EMSAutoConfiguration"
})
@Slf4j
public class BridgeAutoConfiguration {

   private static final Pattern znodeBasePathPattern         = Pattern.compile("/(.*)");
   private static final String  KAFKA_BROADCAST_DIRECT_ROUTE = "direct:kafka-broadcast";
   private static final String  ZOOKEEPER_SET_DIRECT_ROUTE   = "direct:zookeeper-set";

   protected static final String START_ACTION = "start";
   protected static final String STOP_ACTION  = "stop";

   private final Map<String, BridgeProperties>                bridges;
   private final Set<String>                                  zookeeperEndpoints;
   private final EnumMap<BridgeSource, AbstractBridgeFactory> bridgeFactories = new EnumMap<>(BridgeSource.class);
   private final BridgesGlobalConfigs                         bridgesGlobalConfigs;
   private final GenericApplicationContext                    context;
   private final CamelContext                                 camelContext;

   public BridgeAutoConfiguration(final BirchProperties theProperties, final GenericApplicationContext theContext, final CamelContext theCamelContext) {
      this.bridges              = theProperties.getBridges();
      this.bridgesGlobalConfigs = theProperties.getBridgesGlobalConfigs();
      this.zookeeperEndpoints   = theProperties.getKafka().getAdmin().getZookeeperServers();
      this.context              = theContext;
      this.camelContext         = theCamelContext;
   }

   /**
    * Create bridges.
    */
   @PostConstruct
   @SuppressWarnings("unchecked")
   protected void init() {
      this.registerTransactedPolicy(QueueConnectionFactory.class, TopicConnectionFactory.class);

      final var aBridgeFactoryBeans = this.context.getBeansOfType(AbstractBridgeFactory.class);
      if (CollectionUtils.isEmpty(aBridgeFactoryBeans)) {
         log.error("No bridge factory beans found");
      }
      else {
         aBridgeFactoryBeans.forEach((k, v) -> this.bridgeFactories.put(v.getSource(), v));

         this.bridges.forEach((name, properties) -> {
            if (properties.isEnabled()) {
               if (properties.getSource() == null) {
                  throw new ConfigurationException(B31010);
               }
               final var aBridgeFactory = this.bridgeFactories.get(properties.getSource());
               if (aBridgeFactory == null) {
                  throw new ConfigurationException(B31000);
               }
               try {
                  RouteBuilder.addRoutes(this.camelContext, aBridgeFactory.createBridge(name, properties, this.bridgesGlobalConfigs));
                  log.info("Created bridge {} with source {} and properties {}", name, aBridgeFactory.getSource(), properties);
               }
               catch (Exception e) {
                  throw new ConfigurationException(B31048, e);
               }
            }
            else {
               log.info("Bridge {} is disabled", name);
            }
         });
      }
   }

   @Bean
   @ConditionalOnExpression("!'${birch.bridges-global-configs.affinity:}'.isEmpty() && !'${birch.bridges-global-configs.service-topic:}'.isEmpty()")
   @SuppressWarnings("AutoBoxing")
   protected LambdaRouteBuilder bridgesRESTEndpoint(final BirchProperties theProperties) {
      final var aZKBasePath = normalizeBasePath(theProperties.getBridgesGlobalConfigs().getZookeeperBasePath());
      final var aZKServers = String.join(",", this.zookeeperEndpoints);
      final var anAffinity = theProperties.getBridgesGlobalConfigs().getAffinity();
      final var aZKReadURI = String.format("zookeeper://%s/%s/active-affinity", aZKServers, aZKBasePath);
      final var aZKWriteURI = String.format("zookeeper://%s/%s/active-affinity?create=true&createMode=PERSISTENT", aZKServers, aZKBasePath);

      return rb -> {
         rb.rest("/routes")
           .id("bridges-endpoints")
           .post("/{action}")
              .route()
              .routeId("service-message-producer")
              .autoStartup(true)
              .filter(exchange -> StringUtils.equalsAny((String) exchange.getIn().getHeader("action"), START_ACTION, STOP_ACTION))
              .process(exchange -> {
                 final var aMessage = exchange.getIn();
                 aMessage.setBody(new ServiceMessageDTO(anAffinity, aMessage.getHeader("action", String.class)));
              })
              .choice()
                 .when(exchange -> StringUtils.isBlank(aZKServers))
                    .to(KAFKA_BROADCAST_DIRECT_ROUTE)
                 .otherwise()
                    .multicast().to(KAFKA_BROADCAST_DIRECT_ROUTE, ZOOKEEPER_SET_DIRECT_ROUTE)
              .end()
           .endRest()
           .get("/isActive")
              .produces(APPLICATION_JSON_VALUE)
              .route()
              .routeId("active-reader-producer")
              .choice()
                 .when(exchange -> StringUtils.isBlank(aZKServers))
                    .transform().body(body -> new SimpleEntry<>("error", "Affinity state maintenance is not configured"))
                 .endChoice()
                 .otherwise()
                    .setHeader(ZooKeeperMessage.ZOOKEEPER_OPERATION).constant("READ")
                    .to(aZKReadURI)
                    .transform().message(message -> new SimpleEntry<>("isActive", StringUtils.equals(message.getBody(String.class), anAffinity)))
                 .endChoice()
              .end()
              .marshal().json()
              .log(DEBUG, "${body}");

         rb.from(KAFKA_BROADCAST_DIRECT_ROUTE)
           .marshal().custom(ServiceMessageDataFormat.BEAN_NAME)
           .toF("kafka:%s", theProperties.getBridgesGlobalConfigs().getServiceTopic());

         if (StringUtils.isNotBlank(aZKServers)) {
            rb.from(ZOOKEEPER_SET_DIRECT_ROUTE)
              .setHeader(ZooKeeperMessage.ZOOKEEPER_OPERATION).constant("WRITE")
              .choice()
                 .when(exchange -> StringUtils.equals(exchange.getIn().getBody(ServiceMessageDTO.class).getAction(), START_ACTION))
                    .transform().body(ServiceMessageDTO.class, ServiceMessageDTO::getAffinity)
                    .to(aZKWriteURI)
                    .log(DEBUG, "${header[CamelZooKeeperNode]}: ${bodyOneLine}")
                 .when(exchange -> StringUtils.equals(exchange.getIn().getBody(ServiceMessageDTO.class).getAction(), STOP_ACTION))
                    .setBody(exchange -> "")
                    .to(aZKWriteURI)
                    .log(DEBUG, "${header[CamelZooKeeperNode]}: ${bodyOneLine}")
              .end();
         }
      };
   }

   @Bean
   @ConditionalOnExpression("!'${birch.bridges-global-configs.affinity:}'.isEmpty() && !'${birch.bridges-global-configs.service-topic:}'.isEmpty()")
   protected LambdaRouteBuilder serviceMessageConsumer(final BirchProperties theProperties)
                                throws UnknownHostException {
      final var aBridgeNamesList = new ArrayList<>(this.bridges.keySet());
      final var aGroupID = String.format("%s-%s", InetAddress.getLocalHost().getHostName(), RandomStringUtils.random(5, true, false).toLowerCase());
      return rb -> rb.fromF("kafka:%s?consumersCount=1&groupId=%s", theProperties.getBridgesGlobalConfigs().getServiceTopic(), aGroupID)
                     .routeId("service-message-consumer")
                     .autoStartup(true)
                     .unmarshal().custom(ServiceMessageDataFormat.BEAN_NAME)
                     .filter(exchange -> {
                        final var aPayload = exchange.getMessage().getBody(ServiceMessageDTO.class);
                        return StringUtils.equals(aPayload.getAffinity(), theProperties.getBridgesGlobalConfigs().getAffinity());
                     })
                     .setHeader("actionText").body(ServiceMessageDTO.class, body -> StringUtils.capitalize(body.getAction()))
                     .log("${header.actionText} bridges...")
                     .process(exchange -> {
                        final var anAction = exchange.getIn().getBody(ServiceMessageDTO.class).getAction();
                        aBridgeNamesList.parallelStream().forEach(bridge -> {
                           final var aURI = String.format("controlbus:route?routeId=%s&action=%s", bridge, anAction);
                           exchange.getContext().createProducerTemplate().send(aURI, exchange);
                        });
                     })
                     .log("${header.actionText} completed.");
   }

   /**
    * Tries to find or creates a {@link JmsTransactionManager} only if the connection factory of the type provided in this method's parameter is
    * found within the application context.  Once the transaction manager is obtained, registers a transaction policy within the bean registry using
    * the that transaction manager.
    * @param theConnectionFactoryTypes the connection factory types for which to register a transaction policy
    */
   @SuppressWarnings({"VariableArgumentMethod", "unchecked"})
   private void registerTransactedPolicy(final Class<? extends ConnectionFactory>... theConnectionFactoryTypes) {
      Stream.of(theConnectionFactoryTypes).forEach(connectionFactoryClass -> {
         final var aTransactedPolicyType = TopicConnectionFactory.class.isAssignableFrom(connectionFactoryClass) ? TransactedPolicyType.TOPIC : TransactedPolicyType.QUEUE;
         try {
            final var aConnectionFactory = this.context.getBean(connectionFactoryClass);
            final Supplier<PlatformTransactionManager> aTransactionManagerSupplier = () -> {
               try {
                  return this.context.getBean(aTransactedPolicyType.getTxManagerBeanName(), PlatformTransactionManager.class);
               }
               catch (NoSuchBeanDefinitionException e) {
                  log.info("No transaction manager found for {}; registering one", connectionFactoryClass.getName());
                  this.context.registerBean(aTransactedPolicyType.getTxManagerBeanName(), JmsTransactionManager.class,
                                            () -> new JmsTransactionManager(aConnectionFactory));
                  return this.context.getBean(aTransactedPolicyType.getTxManagerBeanName(), PlatformTransactionManager.class);
               }
            };
            this.context.registerBean(aTransactedPolicyType.getPolicyBeanName(), SpringTransactionPolicy.class,
                                      () -> {
                                         final var aTXPolicy = new SpringTransactionPolicy(aTransactionManagerSupplier.get());
                                         aTXPolicy.setPropagationBehaviorName("PROPAGATION_REQUIRED");
                                         return aTXPolicy;
                                      });
         }
         catch (NoSuchBeanDefinitionException e){
            log.info("No {} beans defined; not creating {} bean", connectionFactoryClass.getName(), aTransactedPolicyType.getPolicyBeanName());
         }
      });
   }

   private static String normalizeBasePath(final String theBasePath) {
      final var aMatcher = znodeBasePathPattern.matcher(theBasePath);
      if (aMatcher.matches()) {
         return aMatcher.group(1);
      }
      return theBasePath;
   }
}