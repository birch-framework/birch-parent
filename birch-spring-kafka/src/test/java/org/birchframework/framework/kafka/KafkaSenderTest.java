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

import java.io.Serializable;
import java.util.Optional;
import org.birchframework.framework.dto.TestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.SuccessCallback;

import static org.assertj.core.api.Assertions.*;


/**
 * Tests {@link KafkaSender}.
 * @author Keivan Khalichi
 */
@SpringBootTest(classes = KafkaSenderTestConfiguration.class)
@EmbeddedKafka(partitions = 1)
@TestPropertySource(properties = {"logging.level.org.birchframework: info",
                                  "logging.level.kafka.server: fatal",
                                  "logging.level.kafka.utils: fatal",
                                  "spring.cloud.config.enabled: false",
                                  "spring.kafka.bootstrap-servers: ${spring.embedded.kafka.brokers}",
                                  "spring.kafka.producer.key-serializer: org.apache.kafka.common.serialization.StringSerializer",
                                  "spring.kafka.producer.value-serializer: org.springframework.kafka.support.serializer.JsonSerializer",
                                  "spring.kafka.producer.properties.spring.json.trusted.packages: *",
                                  "spring.kafka.consumer.key-deserializer: org.apache.kafka.common.serialization.StringDeserializer",
                                  "spring.kafka.consumer.value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer",
                                  "spring.kafka.consumer.properties.spring.json.trusted.packages: *",
                                  "spring.kafka.listener.missing-topics-fatal: false",
                                  "birch.test.topic: birch-topic",
                                  "birch.test.dto.key: test-dto"})
@DirtiesContext
public class KafkaSenderTest {

   private static final TestDTO TEST_DTO = new TestDTO(42, "Meaning of life and everything.");

   @Value("${birch.test.topic}")
   private String testTopic;

   @Value("${birch.test.dto.key}")
   private String testKey;

   @Autowired
   private KafkaSender<String, TestDTO> kafkaSender;

   /**
    * Tests {@link KafkaSender#sendAsync(String, Serializable, SuccessCallback, FailureCallback)}
    */
   @Test
   public void sendAsyncWithKeyPayloadAndCallback() {
      this.kafkaSender.sendAsync(
         testTopic, testKey, TEST_DTO, result -> assertThat(result).isNotNull(), e -> fail("Unexpected error", e)
         );
   }

   /**
    * Tests {@link KafkaSender#sendAsync(String, String, Serializable)}
    */
   @Test
   public void sendAsyncWithKeyAndPayload() throws InterruptedException {
      this.kafkaSender.sendAsync(testTopic, testKey, TEST_DTO);
      Thread.sleep(1000L);
   }

   /**
    * Tests {@link KafkaSender#send(String, String, Serializable)}
    */
   @Test
   public void sendWithKeyAndData() {
      final Optional<KafkaSendResult<String, TestDTO>> anOptionalResult;
      try {
         anOptionalResult = this.kafkaSender.send(testTopic, testKey, TEST_DTO);
         this.assertOptionalResult(anOptionalResult);
      }
      catch (InterruptedException e) {
         assertThat(e).isNotNull();
         assertThat(e).isInstanceOf(InterruptedException.class);
      }
   }

   /**
    * Tests {@link KafkaSender#sendAsync(String, Serializable, SuccessCallback, FailureCallback)}
    */
   @Test
   public void sendAsyncWithDataAndCallback() {
      this.kafkaSender.sendAsync(
         testTopic, TEST_DTO, result -> assertThat(result).isNotNull(), e -> fail("Unexpected error", e));
   }

   /**
    * Tests {@link KafkaSender#sendAsync(String, Serializable)}
    */
   @Test
   public void sendAsyncWithData() {
      this.kafkaSender.sendAsync(testTopic, TEST_DTO);
   }

   /**
    * Tests {@link KafkaSender#send(String, Serializable)}
    */
   @Test
   public void sendWithPayload() {
      final Optional<KafkaSendResult<String, TestDTO>> anOptionalResult;
      try {
         anOptionalResult = this.kafkaSender.send(testTopic, TEST_DTO);
         this.assertOptionalResult(anOptionalResult);
      }
      catch (InterruptedException e) {
         assertThat(e).isNotNull();
         assertThat(e).isInstanceOf(InterruptedException.class);
      }
   }

   @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
   private void assertOptionalResult(final Optional<KafkaSendResult<String, TestDTO>> theOptionalResult) {
      assertThat(theOptionalResult.isPresent()).isTrue();
      final var aSendResult = theOptionalResult.get();
      assertThat(aSendResult).isNotNull();
      assertThat(aSendResult.result).isNotNull();
      assertThat(aSendResult.getResult()).hasNoNullFieldsOrProperties();
   }
}