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

import java.util.Map;
import java.util.UUID;
import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.SpringBootCamelContext;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Test Spring Boot applications for ActiveMQ bridge and message producer.
 * @author Keivan Khalichi
 */
@EnableAutoConfiguration(excludeName = "com.ibm.mq.spring.boot.MQAutoConfiguration")
@EnableJMSKafkaBridge
public class ActiveMQBridgeApplication {

   /**
    * Main method.
    * @param theArgs command line arguments
    */
   @SuppressWarnings("VariableArgumentMethod")
   public static void main(final String... theArgs) {
      if (ArrayUtils.isNotEmpty(theArgs)) {
         SpringApplication.run(MessageProducerApplication.class, theArgs);
      }
      else{
         SpringApplication.run(ActiveMQBridgeApplication.class, theArgs);
      }
   }

   @EnableAutoConfiguration(excludeName = "com.ibm.mq.spring.boot.MQAutoConfiguration")
   @Import(ActiveMQAutoConfiguration.class)
   static class MessageProducerApplication extends BaseMessageProducerApplication {

      public MessageProducerApplication(final CamelContext theContext) {
         super((SpringBootCamelContext) theContext);
      }

      @Override
      @SuppressWarnings("AutoBoxing")
      protected Map<String, Object> messageProperties(final int i, final String theMessage) {
         return Map.of("corrID", UUID.randomUUID().toString(), "key", "test-key", "index", i, "isTrue", true);
      }
   }
}