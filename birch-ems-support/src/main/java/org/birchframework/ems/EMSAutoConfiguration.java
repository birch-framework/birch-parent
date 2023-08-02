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

package org.birchframework.ems;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import com.tibco.tibjms.TibjmsXAQueueConnectionFactory;
import com.tibco.tibjms.TibjmsXATopicConnectionFactory;
import com.tibco.tibjms.naming.TibjmsInitialContextFactory;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.JmsPoolConnectionFactoryFactory;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.jndi.JndiLocatorDelegate;
import org.springframework.transaction.PlatformTransactionManager;

import static com.tibco.tibjms.Tibjms.*;
import static com.tibco.tibjms.naming.TibjmsContext.*;
import static org.springframework.jms.listener.DefaultMessageListenerContainer.*;

/**
 * Auto-configuration for TIBCO EMS.  Requires TIBCO EMS dependencies.
 * <p/>
 * Available configurations and sample values:
 * <pre>
 *    tibco:
 *      ems:
 *        jndi:
 *          url: ${tibco.ems.factory.url}                          # JNDI URI; defaults to tibco.ems.factory.url value
 *          principal: ${tibco.ems.factory.username}               # JNDI user; defaults to tibco.ems.factory.username
 *          credentials: ${tibco.ems.factory.password}             # JNDI password (optional); defaults to tibco.ems.factory.password
 *          protocol: ${tibco.ems.factory.protocol}                # JNDI protocol (optional); defaults to tibco.ems.factory.protocol; see {@link javax.naming.Context#SECURITY_PROTOCOL}
 *          authentication: simple                                 # Type of JNDI authentication; see {@link javax.naming.Context#SECURITY_AUTHENTICATION}
 *        factory:
 *          url: tcp://localhost:7222                              # EMS server URL
 *          username: admin                                        # EMS server user
 *          password: passw0rd                                     # EMS server password (optional)
 *          protocol: ssl                                          # EMS protocol; (optional); valid value is "ssl"; see {@link javax.naming.Context#SECURITY_PROTOCOL}
 *        ssl:
 *          verify-host: true                                      # Verify EMS server host; defaults to true
 *          verify-hostname: true                                  # Verify EMS server hostname; defaults to true
 *          auth-only: false                                       # Determines if SSL is for authentication only; defaults to false
 *        queue-connection-factory-name: QueueConnectionFactory    # Queue connection factory lookup name
 *        topic-connection-factory-name: TopicConnectionFactory    # Topic connection factory lookup name
 *        pool:                                                    # Connection pooling configuration; applies to each configured connection factory
 *          enabled: false                                         # When true, enables connection pooling
 *          max-connections: 1                                     # Maximum number of connections created by pool
 *          max-sessions-per-connection: 500                       # Maximum number of sessions created per connection
 *          time-between-expiration-check: -1                      # Time to sleep between runs of the idle connection eviction thread; disabled if negative value
 *          idle-timeout: 30s                                      # Connection idle timeout
 *          block-if-full: true                                    # Block thread when a connection is requested and the pool is full; will throw JMSException if set to false and pool is full
 *          block-if-full-timeout: -1                              # Blocking period before throwing an exception if the pool is still full; disabled if negative value
 * </pre>
 * @author Keivan Khalichi
 */
@Configuration(proxyBeanMethods = false)
@EnableAutoConfiguration
@ConditionalOnClass(name = {"javax.jms.ConnectionFactory", "com.tibco.tibjms.TibjmsConnectionFactory"})
@ConditionalOnProperty(prefix = "tibco.ems.factory", name = "url")
@ConditionalOnMissingBean(ConnectionFactory.class)
@EnableConfigurationProperties({EMSProperties.class, JmsProperties.class})
@RequiredArgsConstructor
@SuppressWarnings("AutoBoxing")
public class EMSAutoConfiguration {

   @SuppressWarnings("unused")
   private static final Logger log = LoggerFactory.getLogger(EMSAutoConfiguration.class);

   private final EMSProperties                    properties;
   private final JmsProperties                    jmsProperties;
   private final ObjectProvider<MessageConverter> messageConverter;

   @Bean
   @SuppressWarnings("DuplicatedCode")
   public JndiLocatorDelegate jndiLocatorDelegate() {
      final var aMapper  = PropertyMapper.get();
      final var aJNDI    = this.properties.getJndi();
      final var aFactory = this.properties.getFactory();
      final var anSSL    = this.properties.getSsl();
      final var aMap     = new HashMap<String, Object>(Map.of(INITIAL_CONTEXT_FACTORY, TibjmsInitialContextFactory.class.getName(),
                                                              PROVIDER_URL, StringUtils.defaultIfBlank(aJNDI.getUrl(), aFactory.getUrl()),
                                                              SSL_ENABLE_VERIFY_HOST, Boolean.toString(anSSL.isVerifyHost()),
                                                              SSL_ENABLE_VERIFY_HOST_NAME, Boolean.toString(anSSL.isVerifyHostname())));

      aMapper.from(StringUtils.defaultIfBlank(aJNDI.getPrincipal(), aFactory.getUsername())).whenHasText().to(s -> aMap.put(SECURITY_PRINCIPAL, s));
      aMapper.from(StringUtils.defaultIfBlank(aJNDI.getCredentials(), aFactory.getPassword())).whenHasText().to(s -> aMap.put(SECURITY_CREDENTIALS, s));
      aMapper.from(StringUtils.defaultIfBlank(aJNDI.getProtocol(), aFactory.getProtocol())).whenHasText().to(s -> aMap.put(SECURITY_PROTOCOL, s));
      Stream.of(aJNDI.getProtocol(), aFactory.getProtocol()).filter(StringUtils::isNotBlank).findFirst().ifPresent(s -> aMap.put(SECURITY_PROTOCOL, s));
      aMapper.from(aJNDI::getAuthentication).whenHasText().to(s -> aMap.put(SECURITY_AUTHENTICATION, s));
      final var aProperties = new Properties(aMap.size());
      aProperties.putAll(aMap);
      return new EMSJNDILocatorDelegate(aProperties);
   }

   @SuppressWarnings("DuplicatedCode")
   protected Map<String, String> buildEMSProperties() {
      final var aMapper = PropertyMapper.get();
      final var aMap = new HashMap<>(Map.of(DEFAULT_FACTORY_USERNAME, this.properties.getFactory().getUsername()));
      aMapper.from(this.properties.getFactory().getPassword()).whenNonNull().to(s -> aMap.put(DEFAULT_FACTORY_PASSWORD, s));
      aMapper.from(this.properties.getFactory().getProtocol()).whenNonNull().to(s -> aMap.put(SECURITY_PROTOCOL, s));
      aMap.putAll(Map.of(FACTORY_CONNECT_ATTEMPT_COUNT,     Integer.toString(this.properties.getConnect().getAttemptCount()),
                         FACTORY_CONNECT_ATTEMPT_DELAY,     Long.toString(this.properties.getConnect().getAttemptDelay().toMillis()),
                         FACTORY_CONNECT_ATTEMPT_TIMEOUT,   Long.toString(this.properties.getConnect().getAttemptTimeout().toMillis()),
                         FACTORY_RECONNECT_ATTEMPT_COUNT,   Integer.toString(this.properties.getReconnect().getAttemptCount()),
                         FACTORY_RECONNECT_ATTEMPT_DELAY,   Long.toString(this.properties.getReconnect().getAttemptDelay().toMillis()),
                         FACTORY_RECONNECT_ATTEMPT_TIMEOUT, Long.toString(this.properties.getReconnect().getAttemptTimeout().toMillis())));
      final var anSSL = this.properties.getSsl();
      aMap.putAll(Map.of(SSL_ENABLE_VERIFY_HOST,      Boolean.toString(anSSL.isVerifyHost()),
                         SSL_ENABLE_VERIFY_HOST_NAME, Boolean.toString(anSSL.isVerifyHostname()),
                         SSL_TRACE,                   Boolean.toString(anSSL.isTrace()),
                         SSL_DEBUG_TRACE,             Boolean.toString(anSSL.isDebugTrace()),
                         SSL_AUTH_ONLY,               Boolean.toString(anSSL.isAuthOnly())));
      aMapper.from(anSSL::getVendor)             .whenNonNull().to(s -> aMap.put(SSL_VENDOR, s));
      aMapper.from(anSSL::getExpectedHostname)   .whenNonNull().to(s -> aMap.put(SSL_EXPECTED_HOST_NAME, s));
      aMapper.from(anSSL::getHostnameVerifier)   .whenNonNull().to(s -> aMap.put(SSL_HOST_NAME_VERIFIER, s));
      aMapper.from(anSSL::getIdentity)           .whenNonNull().to(s -> aMap.put(SSL_IDENTITY, s));
      aMapper.from(anSSL::getIdentityEncoding)   .whenNonNull().to(s -> aMap.put(SSL_IDENTITY_ENCODING, s));
      aMapper.from(anSSL::getPassword)           .whenNonNull().to(s -> aMap.put(SSL_PASSWORD, s));
      aMapper.from(anSSL::getIssuerCertificates) .whenNonNull().to(s -> aMap.put(SSL_ISSUER_CERTIFICATES, s));
      aMapper.from(anSSL::getTrustedCertificates).whenNonNull().to(s -> aMap.put(SSL_TRUSTED_CERTIFICATES, s));
      aMapper.from(anSSL::getPrivateKey)         .whenNonNull().to(s -> aMap.put(SSL_PRIVATE_KEY, s));
      aMapper.from(anSSL::getPrivateKeyEncoding) .whenNonNull().to(s -> aMap.put(SSL_PRIVATE_KEY_ENCODING, s));
      aMapper.from(anSSL::getCipherSuites)       .whenNonNull().to(s -> aMap.put(SSL_CIPHER_SUITES, s));
      return aMap;
   }

   @Bean
   @ConditionalOnMissingBean(JmsPoolConnectionFactoryFactory.class)
   @ConditionalOnProperty(prefix = "tibco.ems.pool", name = "enabled")
   protected JmsPoolConnectionFactoryFactory jmsPoolConnectionFactoryFactory() {
      return new JmsPoolConnectionFactoryFactory(this.properties.getPool());
   }

   @Bean
   @ConditionalOnMissingBean(QueueConnectionFactory.class)
   @ConditionalOnProperty(prefix = "tibco.ems", name = "queue-connection-factory-name")
   protected QueueConnectionFactory pooledQueueConnectionFactory(final JmsPoolConnectionFactoryFactory theFactoryFactory) {
      final var aMap = this.buildEMSProperties();
      final var aURL = this.properties.getFactory().getUrl();
      final QueueConnectionFactory aConnectionFactory = new QueueConnectionFactoryAdapter(
         theFactoryFactory.createPooledConnectionFactory(new TibjmsXAQueueConnectionFactory(aURL, null, aMap))
      );
      log.info("Created {} for URL: {} and properties {}", aConnectionFactory.getClass().getName(), aURL, aMap.toString());
      return aConnectionFactory;
   }

   @Bean
   @ConditionalOnMissingBean(TopicConnectionFactory.class)
   @ConditionalOnProperty(prefix = "tibco.ems", name = "topic-connection-factory-name")
   protected TopicConnectionFactory pooledTopicConnectionFactory(final JmsPoolConnectionFactoryFactory theFactoryFactory) {
      final var aMap = this.buildEMSProperties();
      final var aURL = this.properties.getFactory().getUrl();
      final TopicConnectionFactory aConnectionFactory = new TopicConnectionFactoryAdapter(
         theFactoryFactory.createPooledConnectionFactory(new TibjmsXATopicConnectionFactory(aURL, null, aMap))
      );
      log.info("Created {} for URL: {} and properties {}", aConnectionFactory.getClass().getName(), aURL, aMap.toString());
      return aConnectionFactory;
   }

   @Bean
   @ConditionalOnMissingBean(QueueConnectionFactory.class)
   @ConditionalOnExpression("'${tibco.ems.queue-connection-factory-name}' != null and !'${tibco.ems.pool.enabled}'")
   protected QueueConnectionFactory queueConnectionFactory() {
      final var aMap = this.buildEMSProperties();
      final var aURL = this.properties.getFactory().getUrl();
      final QueueConnectionFactory aConnectionFactory = new QueueConnectionFactoryAdapter(new TibjmsXAQueueConnectionFactory(aURL, null, aMap));
      log.info("Created {} for URL: {} and properties {}", aConnectionFactory.getClass().getName(), aURL, aMap.toString());
      return aConnectionFactory;
   }

   @Bean
   @ConditionalOnMissingBean(TopicConnectionFactory.class)
   @ConditionalOnExpression("'${tibco.ems.topic-connection-factory-name}' != null and !'${tibco.ems.pool.enabled}'")
   protected TopicConnectionFactory topicConnectionFactory() {
      final var aMap = this.buildEMSProperties();
      final var aURL = this.properties.getFactory().getUrl();
      final TopicConnectionFactory aConnectionFactory = new TopicConnectionFactoryAdapter(new TibjmsXATopicConnectionFactory(aURL, null, aMap));
      log.info("Created {} for URL: {} and properties {}", aConnectionFactory.getClass().getName(), aURL, aMap.toString());
      return aConnectionFactory;
   }

   @Bean
   @ConditionalOnClass(DestinationResolver.class)
   @ConditionalOnMissingBean(DestinationResolver.class)
   protected DestinationResolver destinationResolvers(final JndiLocatorDelegate theJNDILocatorDelegate) {
      final var aReturnValue = new JndiDestinationResolver();
      aReturnValue.setCache(true);
      aReturnValue.setJndiTemplate(theJNDILocatorDelegate.getJndiTemplate());
      return aReturnValue;
   }

   @Bean
   @ConditionalOnClass(JmsListenerContainerFactory.class)
   @ConditionalOnMissingBean(name = "queueListenerContainerFactory")
   @ConditionalOnBean(QueueConnectionFactory.class)
   @ConditionalOnProperty(prefix = "spring.jms", name = "listener")
   protected JmsListenerContainerFactory<? extends MessageListenerContainer> queueListenerContainerFactory(
                                 @Qualifier("kafkaTransactionManager") final PlatformTransactionManager theTransactionManager,
                                 final QueueConnectionFactory theConnectionFactory,
                                 final ObjectProvider<DestinationResolver> theDestinationResolver) {
      return this.newJMSListenerContainerFactory(theTransactionManager, theConnectionFactory, theDestinationResolver);
   }

   @Bean
   @ConditionalOnClass(JmsListenerContainerFactory.class)
   @ConditionalOnMissingBean(name = "topicListenerContainerFactory")
   @ConditionalOnBean(TopicConnectionFactory.class)
   @ConditionalOnProperty(prefix = "spring.jms", name = "listener")
   protected JmsListenerContainerFactory<? extends MessageListenerContainer> topicListenerContainerFactory(
                                 @Qualifier("kafkaTransactionManager") final PlatformTransactionManager theTransactionManager,
                                 final TopicConnectionFactory theConnectionFactory,
                                 final ObjectProvider<DestinationResolver> theDestinationResolver) {
      return this.newJMSListenerContainerFactory(theTransactionManager, theConnectionFactory, theDestinationResolver);
   }

   @SuppressWarnings("AutoUnboxing")
   protected JmsListenerContainerFactory<? extends MessageListenerContainer> newJMSListenerContainerFactory(
                                 final PlatformTransactionManager theTransactionManager,
                                 final ConnectionFactory theConnectionFactory,
                                 final ObjectProvider<DestinationResolver> theDestinationResolver) {
      final var aFactory       = new DefaultJmsListenerContainerFactory();
      final var aMapper        = PropertyMapper.get().alwaysApplyingWhenNonNull();
      final var aJMSProperties = this.jmsProperties;
      aMapper.from(theConnectionFactory).to(aFactory::setConnectionFactory);
      aMapper.from(theTransactionManager).to(aFactory::setTransactionManager);
      aMapper.from(theTransactionManager).as(Objects::nonNull).whenTrue().to(aFactory::setSessionTransacted);
      aMapper.from(theDestinationResolver::getIfUnique).to(aFactory::setDestinationResolver);
      aMapper.from(this.messageConverter::getIfUnique).to(aFactory::setMessageConverter);
      aMapper.from(aJMSProperties.getListener()::isAutoStartup).to(aFactory::setAutoStartup);
      aMapper.from(aJMSProperties.getListener()::getAcknowledgeMode).as(JmsProperties.AcknowledgeMode::getMode).to(aFactory::setSessionAcknowledgeMode);
      aMapper.from(aJMSProperties.getListener()::formatConcurrency).to(aFactory::setConcurrency);
      aMapper.from(aJMSProperties.getListener()::getReceiveTimeout).as(Duration::toMillis).to(aFactory::setReceiveTimeout);
      aMapper.from(aJMSProperties.getCache()::getSessionCacheSize).to(scs -> aFactory.setCacheLevel(scs == null || scs <= 0 ? CACHE_CONNECTION : CACHE_SESSION));
      aMapper.from(aJMSProperties.getCache()::isConsumers).whenTrue().toCall(() -> aFactory.setCacheLevel(CACHE_CONSUMER));  // overrides previous setting ^^^
      aMapper.from(theConnectionFactory instanceof TopicConnectionFactory).to(aFactory::setPubSubDomain);
      return aFactory;
   }

   protected static class EMSJNDILocatorDelegate extends JndiLocatorDelegate {

      public EMSJNDILocatorDelegate(final Properties theJndiProperties) {
         this.setJndiEnvironment(theJndiProperties);
      }
   }

   @RequiredArgsConstructor
   protected static abstract class AbstractConnectionFactoryAdapter implements ConnectionFactory {

      @Delegate
      protected final ConnectionFactory factory;
   }

   protected static class QueueConnectionFactoryAdapter extends AbstractConnectionFactoryAdapter implements QueueConnectionFactory {

      public QueueConnectionFactoryAdapter(final QueueConnectionFactory theFactory) {
         super(theFactory);
      }

      @Override
      public QueueConnection createQueueConnection() throws JMSException {
         return ((QueueConnectionFactory) this.factory).createQueueConnection();
      }

      @Override
      public QueueConnection createQueueConnection(final String theUserName, final String thePassword) throws JMSException {
         return ((QueueConnectionFactory) this.factory).createQueueConnection(theUserName, thePassword);
      }
   }

   protected static class TopicConnectionFactoryAdapter extends AbstractConnectionFactoryAdapter implements TopicConnectionFactory {

      public TopicConnectionFactoryAdapter(final TopicConnectionFactory theFactory) {
         super(theFactory);
      }

      @Override
      public TopicConnection createTopicConnection() throws JMSException {
         return ((TopicConnectionFactory) this.factory).createTopicConnection();
      }

      @Override
      public TopicConnection createTopicConnection(final String theUserName, final String thePassword) throws JMSException {
         return ((TopicConnectionFactory) this.factory).createTopicConnection(theUserName, thePassword);
      }
   }
}