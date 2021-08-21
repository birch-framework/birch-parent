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
package org.birchframework.configuration;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.PostConstruct;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.birchframework.dto.DisplayableOption;
import org.birchframework.framework.beans.Beans;
import org.birchframework.framework.bridge.Destination;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Delegate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties.Provider;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties.Registration;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.birchframework.configuration.BirchProperties.OAuth2Mode.STANDARD;
import static org.birchframework.dto.BirchErrorCode.*;
import static lombok.AccessLevel.NONE;
import static org.birchframework.framework.bridge.DestinationType.*;

/**
 * Birch Framework properties.
 * @author Keivan Khalichi
 */
@ConfigurationProperties(prefix = "birch")
@Getter
@ToString
public class BirchProperties {

   private final Internationalization          i18n                 = new Internationalization();
   private final Actuator                      actuator             = new Actuator();
   private final Kafka                         kafka                = new Kafka();
   private final OpenAPI                       openapi              = new OpenAPI();
   private final BridgesGlobalConfigs          bridgesGlobalConfigs = new BridgesGlobalConfigs();
   private final Map<String, BridgeProperties> bridges              = new HashMap<>(1);
   private final Security                      security             = new Security();

   @PostConstruct
   void init() {
      this.security.oauth2.realms.forEach((k, v) -> v.key = k);
   }

   @Getter
   @Setter
   @ToString
   public static class Internationalization {
      private boolean enabled = false;
      private String  resourceBundleBaseName;
   }

   /**
    * Returns a bridge properties given its name
    * @param theBridgeName the name of the bridge
    * @return the bridge properties
    */
   public BridgeProperties bridge(final String theBridgeName) {
      return StringUtils.isBlank(theBridgeName) ? null : this.bridges.get(theBridgeName);
   }

   @Getter
   @ToString
   public static class Actuator {
      private final Map<String, URI> uriMap = new HashMap<>();
   }

   @Getter
   @ToString
   public static class Kafka {

      /** Sender properties */
      private final Sender     sender = new Sender();
      /** KafkaAdminUtils properties */
      private final KafkaAdmin admin  = new KafkaAdmin();

      /**
       * Kafka properties.
       */
      @Setter
      @Getter
      @ToString
      public static class Sender {

         /** Milliseconds to wait for synchronous send responses; defaults to 2 seconds */
         private Duration waitTime = Duration.ofSeconds(2);

         /** Whether sender's use of Kafka template allows non-transactional send operations */
         private boolean allowNonTransactional = true;
      }

      @Setter
      @Getter
      @ToString
      public static class KafkaAdmin {
         private final Set<String> bootstrapServers = new HashSet<>(3);
         private final Set<String> zookeeperServers = new HashSet<>(3);
         private       String      sslProtocol;
         private       String      securityProtocol;
         private final SASL        sasl             = new SASL();
         private       Class<?>    keySerializer;
         private       Class<?>    valueSerializer;
         private       Class<?>    keyDeserializer;
         private       Class<?>    valueDeserializer;
      }

      @Getter
      @Setter
      @ToString
      public static class SASL {
         private String jaasConfig;
         private String mechanism;
      }
   }

   @Getter
   @ToString
   public static class OpenAPI {
      private final Feature feature = new Feature();

      @Getter
      @Setter
      @ToString
      public static class Feature {
         private String basePath;
         private String title;
         private String description;
         private String contactName;
         private String contactUrl;
         private String license;
         private String licenseUrl;
         private String version;
         private boolean prettyPrint;
         private boolean readAllResources;
         private boolean supportSwaggerUi;
      }
   }

   @Getter
   @Setter
   public static class BridgesGlobalConfigs {
      private int      maxRedeliveries        = 0;
      private Duration redeliveryDelay        = Duration.ofSeconds(1);
      private Duration maximumRedeliveryDelay = Duration.ofMinutes(1);
      private boolean  exponentialBackOff     = false;
      private String   affinity;
      private String   serviceTopic;
      private boolean  autoStart              = true;
      private String   zookeeperBasePath      = "/birch/bridges";
   }

   @Getter
   @Setter
   @ToString
   public static class BridgeProperties {

      private       boolean                       enabled              = true;
      private       boolean                       stripNewline         = true;
      private       BridgeType                    bridgeType;
      private       BridgeSource                  source;
      private       Class<? extends Predicate<?>> filterPredicate;
      private       Class<? extends Consumer<?>>  afterReceiveConsumer;
      private       Class<? extends Consumer<?>>  beforeSendConsumer;
      private       Class<? extends Consumer<?>>  errorConsumer;
      private       Set<String>                   filterProperties;
      private       int                           concurrentConsumers  = 1;
      private       boolean                       transacted           = true;
      private final JMSSourceProperties           jms                  = new JMSSourceProperties();
      private final KafkaSourceProperties         kafka                = new KafkaSourceProperties();

      public enum BridgeType {
         ACTIVE_MQ,
         EMS,
         MQ
      }

      public enum BridgeSource {
         JMS,
         KAFKA
      }
   }

   @Getter
   @Setter
   @ToString
   public static class JMSSourceProperties {

      private String         queue;
      private String         topic;
      private String         keyProperty;
      private String         keyRegex;
      private int            keyRegexCapture        = 0;
      private String         correlationIdProperty;
      private boolean        overrideCorrelationID  = true;
      private String         selector;
      private JMSMessageType messageType            = JMSMessageType.TEXT;
      private String         deadLetterQueue;

      @SuppressWarnings("unused")
      public enum JMSMessageType {
          BYTES,
          MAP,
          OBJECT,
          STREAM,
          TEXT,
      }

      public Destination destination() {
         if (StringUtils.isNoneBlank(this.queue, this.topic)) {
            throw new ConfigurationException(B31021);
         }
         if (StringUtils.isNotBlank(this.queue)) {
            return new Destination(this.queue, QUEUE);
         }
         else if (StringUtils.isNotBlank(this.topic)) {
            return new Destination(this.topic, TOPIC);
         }
         else {
            throw new ConfigurationException(B31020);
         }
      }
   }

   @Getter
   @Setter
   @ToString
   public static class KafkaSourceProperties {
      private String topic;
      private String listenerId;
      private String groupId;
      private String deadLetterTopic;
   }

   @Getter
   public static class Security {
      private final OAuth2      oauth2               = new OAuth2();
      private final Set<String> unsecureContextPaths = new HashSet<>(Set.of("/config", "/openapi.json"));
      private final CXF         cxf                  = new CXF();

      @Getter
      @ToString
      public static class CXF {
         private final CORS cors = new CORS();
      }

      @Getter
      @Setter
      @ToString
      public static class CORS {
         private boolean allow = false;
      }
   }

   public enum OAuth2Mode {
      STANDARD,
      OIDC
   }

   @Getter
   @Setter
   @ToString
   public static class OAuth2 {
      private       boolean               enabled = false;
      private       OAuth2Mode            mode    = STANDARD;
      private final Map<String, IdPRealm> realms  = new HashMap<>(1);
   }

   @Getter
   @Setter
   @ToString
   @SuppressWarnings("unused")
   public static class IdPRealm implements DisplayableOption {

      public static final String DEFAULT_USER_NAME_CLAIM_NAME = "email";
      public static final String DEFAULT_GROUPS_CLAIM_NAME    = "role";

      private boolean            enabled                   = true;
      @JsonIgnore
      @Getter(NONE)
      @Setter(NONE)
      private String             key;
      private String             name;
      private String             description;
      private IdPClassifiable<?> type;
      @Delegate(excludes = ProviderExcludes.class)
      @Getter(NONE)
      @Setter(NONE)
      private Provider           provider                  = new Provider();
      private String             groupsClaimName           = DEFAULT_GROUPS_CLAIM_NAME;
      @Delegate
      @Getter(NONE)
      @Setter(NONE)
      private Registration       registration              = new Registration();
      private String             realmContextPath;
      private String             logoutUri                 = "/logout";
      private String             logoutRedirectUri         = this.getRedirectUri();
      private boolean            disableSSLValidation      = false;
      private Class<?>           grantedAuthoritiesBuilder = null;

      IdPRealm() {
         this.provider.setUserNameAttribute(DEFAULT_USER_NAME_CLAIM_NAME);
      }

      public String getUserNameClaimName() {
         return this.provider.getUserNameAttribute();
      }

      public void setUserNameClaimName(final String theClaimName) {
         if (StringUtils.isBlank(theClaimName)) {
            this.provider.setUserNameAttribute(DEFAULT_USER_NAME_CLAIM_NAME);
         }
         else {
            this.provider.setUserNameAttribute(theClaimName);
         }
      }

      String getIdpType() {
         return this.type == null ? null : this.type.name();
      }

      void setIdpType(final String theType) {
         if (StringUtils.isNotBlank(theType)) {
            this.type = null;
            Beans.findImplementation(IdPClassifiable.class).map(impl -> Beans.valueOf(impl, theType)).ifPresent(t -> this.type = (IdPClassifiable<?>) t);
         }
      }

      public String getIssuerID() {
         return this.key;
      }

      @JsonIgnore
      @Override
      public String getValue() {
         return this.key;
      }

      @JsonIgnore
      @Override
      public String getText() {
         return this.name;
      }

      private interface ProviderExcludes {
         void setUserNameAttribute(String userNameAttribute);
         String getUserNameAttribute();
      }
   }
}