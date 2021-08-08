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

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.jms.JmsPoolConnectionFactoryProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Tibco EMS configuration properties.
 * @author Keivan Khalichi
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "tibco.ems")
public class EMSProperties {

   private final Factory                            factory                    = new Factory();
   private final JNDI                               jndi                       = new JNDI();
   private final SSL                                ssl                        = new SSL();
   private final ConnectionParameters               connect;
   private final ConnectionParameters               reconnect;
   private       String                             queueConnectionFactoryName = null;
   private       String                             topicConnectionFactoryName = null;
   @NestedConfigurationProperty
   private final JmsPoolConnectionFactoryProperties pool                       = new JmsPoolConnectionFactoryProperties();

   {
      this.connect   = new ConnectionParameters(10, Duration.ofSeconds(1), Duration.ofSeconds(1));
      this.reconnect = new ConnectionParameters(20, Duration.ofSeconds(2), Duration.ofSeconds(2));
   }

   @Setter
   @Getter
   @SuppressWarnings({"InstanceVariableMayNotBeInitialized"})
   public static class Factory {

      private String url;
      private String username;
      private String password;
      private String protocol;
      private String sslPassword;

   }

   @Setter
   @Getter
   @SuppressWarnings("InstanceVariableMayNotBeInitialized")
   public static class JNDI {
      private String url;
      private String principal;
      private String credentials;
      private String protocol;
      private String authentication;

   }

   @Setter
   @Getter
   @SuppressWarnings("InstanceVariableMayNotBeInitialized")
   public static class SSL {

      private String  vendor;
      private boolean verifyHost     = true;
      private boolean verifyHostname = true;
      private boolean trace          = false;
      private boolean debugTrace     = false;
      private boolean authOnly       = false;
      private String  expectedHostname;
      private String  hostnameVerifier;
      private String  identity;
      private String  identityEncoding;
      private String  password;
      private String  issuerCertificates;
      private String  trustedCertificates;
      private String  privateKey;
      private String  privateKeyEncoding;
      private String  cipherSuites;

   }

   @Setter
   @Getter
   @AllArgsConstructor
   public static class ConnectionParameters {
      private int      attemptCount;
      private Duration attemptDelay;
      private Duration attemptTimeout;
   }
}