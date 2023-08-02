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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.NotifyBuilder;
import org.assertj.core.util.Throwables;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import static java.lang.Boolean.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.*;
import static org.birchframework.bridge.TestConfiguration.MESSAGE_RECEIVED_URI;

/**
 * Integration test of ActiveMQ bridge using EmbeddedKafka and EmbeddedActiveMQ.
 * @author Keivan Khalichi
 */
@SpringBootTest(classes = TestConfiguration.class)
@EmbeddedKafka(partitions = 1)
@ActiveProfiles("activemq")
@SuppressWarnings("AutoBoxing")
@Slf4j
class ActiveMQBridgeTest {

   public static final int ITERATIONS = 5;

   @Value("${birch.bridges.test-1-in.jms.queue}")
   private String queue;

   @Autowired
   private CamelContext camelContext;

   @SuppressWarnings("StaticVariableMayNotBeInitialized")
   private static ConfigurableApplicationContext springContext;

   /**
    * Starts the bridge.
    */
   @BeforeEach
   void before() {
      springContext = SpringApplication.run(ActiveMQBridgeTestApplication.class,
                                            "--spring.profiles.active=activemq",
                                            String.format("--server.port=%d", 18081));
   }

   /**
    * Shuts down the bridge.
    */
   @AfterEach
   void after() {
      springContext.close();
   }

   @Test
   void testBridge() {
      try {
         final var aNotify = new NotifyBuilder(this.camelContext).from(MESSAGE_RECEIVED_URI).whenDone(ITERATIONS).create();
         this.sendMesages(ITERATIONS, "src/test/resources/dto-in.json");
         assertThat(aNotify.matches(10, SECONDS)).isTrue();
      }
      catch (Exception e) {
         final var aRootCause = Throwables.getRootCause(e);
         fail("Unexpected exception occured; Exception: %s; Message: %s", aRootCause.getClass().getName(), aRootCause.getMessage());
      }
   }

   @SuppressWarnings({"SameParameterValue", "AutoBoxing"})
   private void sendMesages(final int theIterations, final String theFilePath) throws IOException {
      final var aPayload = Files.readString(Paths.get(theFilePath));
      IntStream.range(0, theIterations).forEach(i -> {
         final var aProducerTemplate = this.camelContext.createProducerTemplate();
         aProducerTemplate.sendBodyAndHeaders(String.format("jms:queue:%s", this.queue),
                                              aPayload,
                                              Map.of("corrID", UUID.randomUUID().toString(),
                                                     "key", "test-key",
                                                     "index", i,
                                                     "isTrue", TRUE));
      });
   }
}
