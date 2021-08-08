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

import org.birchframework.configuration.BirchProperties;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.spring.boot.SpringBootCamelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests for {@link JMSSourceProcessor}.
 * @author Keivan Khalichi
 */
@SpringBootTest(classes = MQTestConfiguration.class)
@ActiveProfiles("mq")
public class MQConsumerIT {

   public static final int MESSAGE_ITERATIONS = 300;

   @Autowired
   @Spy
   private BirchProperties properties;

   @Autowired
   @Spy
   private CamelContext context;

   @BeforeEach
   public void before() {
      MockitoAnnotations.openMocks(this);
   }

   /**
    * Tests {@link JMSSourceProcessor#process(Exchange)} by sending some messages via the embedded ActiveMQ broker.
    */
   @Test
   public void testSendJMSTextMessage() {
      JMSTestUtils.executeSendMessage(MESSAGE_ITERATIONS, this.properties, (SpringBootCamelContext) this.context, false);
   }
}