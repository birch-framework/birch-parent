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
package org.birchframework.framework.kafka;

import org.birchframework.configuration.BirchProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Auto-configuration for {@link KafkaAdminUtils} and its Micrometer registry metrics.
 * <p/>
 * Configurations are as follows:
 * <pre>
 * birch:
 *   kafka:
 *     admin:
 *       bootstrap-servers: broker-1:9092,broker-2:9092      # Comma separated list of Kafka broker hosts and ports; required
 *       zookeeper-servers: zk-1:2181,zk-2:2181              # Comma separated list of Zookeeper hosts and ports; optional
 *       ssl-protocol:                                       # SSL protocol: TLSv1.3 (default), TLSv1.2, TLSv1.1, SSLv3, SSLv2, or SSL; defaults to TLSv1.3
 *       security-protocol:                                  # Security protocol: SSL, PLAINT_TEXT, SASL_SSL, or SASL_PLAINTEXT: defaults to PLAINTEXT
 *       sasl:                                               # SASL configuration
 *         jaas-config:                                      # SASL JAAS configuration
 *         mechanism:                                        # SASL mechanism: PLAIN, SCRAM, GSSAPI, LDAP, or OAUTHBEARER; defaults to GSSAPI
 * </pre>
 * @author Keivan Khalichi
 */
@Configuration
@EnableConfigurationProperties(BirchProperties.class)
@EnableAutoConfiguration
@EnableScheduling
@Lazy(false)
public class KafkaAdminUtilsAutoConfiguration {

   @Bean
   @ConditionalOnClass({AdminClient.class, KafkaConsumer.class, ConsumerConfig.class, ProducerConfig.class})
   @ConditionalOnProperty(prefix = "birch.kafka.admin", name = "bootstrap-servers")
   KafkaAdminUtils kafkaAdminUtils(final BirchProperties theProperties) {
      return new KafkaAdminUtils(theProperties);
   }

   @Bean
   @ConditionalOnBean(KafkaAdminUtils.class)
   @ConditionalOnClass(MeterRegistry.class)
   KafkaAdminUtilsMetrics kafkaAdminUtilsGauge(final KafkaAdminUtils theKafkaAdminUtils,
                                               final MeterRegistry theMeterRegistry) {
      return new KafkaAdminUtilsMetrics(theKafkaAdminUtils, theMeterRegistry);
   }
}