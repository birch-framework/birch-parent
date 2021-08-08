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
import com.ibm.mq.spring.boot.MQAutoConfiguration;
import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.SpringBootCamelContext;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

import static org.apache.camel.component.jms.JmsMessageType.Bytes;

/**
 * Test Spring Boot applications for EMS bridge and message producer.
 * @author Keivan Khalichi
 */
@EnableAutoConfiguration(excludeName = "org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration",
                         exclude = MQAutoConfiguration.class)
@EnableJMSKafkaBridge
public class EMSBridgeApplication {

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
         SpringApplication.run(EMSBridgeApplication.class, theArgs);
      }
   }

   @EnableAutoConfiguration(excludeName = "org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration",
                            exclude = MQAutoConfiguration.class)
   @Import(EMSAutoConfiguration.class)
   static class MessageProducerApplication extends BaseMessageProducerApplication {

      public MessageProducerApplication(final CamelContext theContext) {
         super(Bytes, (SpringBootCamelContext) theContext);
      }

      @SuppressWarnings("AutoBoxing")
      @Override
      protected Map<String, Object> messageProperties(final int i, final String theMessage) {
         return Map.of("corrID", UUID.randomUUID().toString(), "key", "test-key", "index", i, "isTrue", true);
      }
   }
}