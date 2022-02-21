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

import io.micrometer.core.instrument.MeterRegistry;
import org.apache.camel.CamelContext;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring {@link Configuration} that creates the bridge factories, one per source/sink combination.  If custom factories are required, then create a similar
 * configuration in your Spring Boot application, returning subclasses of {@link JMSToKafkaBridgeFactory} and {@link KafkaToJMSBridgeFactory}, and this
 * configuration will not create the default factories.
 * @author Keivan Khalichi
 */
@Configuration
class BridgeFactoriesConfiguration {

   @Bean
   @ConditionalOnMissingBean(JMSToKafkaBridgeFactory.class)
   JMSToKafkaBridgeFactory jmsToKafkaBridgeFactory(final CamelContext theCamelContext,
                                                   final MeterRegistry theMeterRegistry) {
      return new JMSToKafkaBridgeFactory((SpringCamelContext) theCamelContext, theMeterRegistry);
   }

   @Bean
   @ConditionalOnMissingBean(KafkaToJMSBridgeFactory.class)
   KafkaToJMSBridgeFactory kafkaToJMSBridgeFactory(final CamelContext theCamelContext,
                                                   final MeterRegistry theMeterRegistry) {
      return new KafkaToJMSBridgeFactory((SpringCamelContext) theCamelContext, theMeterRegistry);
   }
}