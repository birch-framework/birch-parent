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

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.birchframework.framework.dto.TestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests {@link KafkaAdminUtils}.
 * @author Keivan Khalichi
 */
@SpringBootTest(classes = TestConfiguration.class)
@EmbeddedKafka(partitions = 1, topics = "${birch.test.topic}")
@TestPropertySource(properties = {"logging.level.org.birchframework: info",
                                  "logging.level.kafka.server: fatal",
                                  "logging.level.kafka.utils: fatal",
                                  "spring.cloud.config.enabled: false",
                                  "birch.kafka.admin.bootstrap-servers: ${spring.embedded.kafka.brokers}",
                                  "birch.kafka.admin.key-serializer: org.apache.kafka.common.serialization.StringSerializer",
                                  "birch.kafka.admin.value-serializer: org.apache.kafka.common.serialization.StringSerializer",
                                  "spring.kafka.listener.missing-topics-fatal: false",
                                  "birch.test.topic: birch-topic",
                                  "birch.test.dto.key: test-dto"})
@Slf4j
public class KafkaAdminUtilsTest {

   private static final TestDTO      TEST_DTO     = new TestDTO(42, "Meaning of life and everything.");
   private static final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
   public static final  int          ITERATIONS   = 1000;

   @Value("${birch.test.topic}")
   private String testTopic;
   @Value("${birch.test.dto.key}")
   private String testKey;

   private KafkaProducer<String, String> kafkaProducer = null;

   @Autowired
   private KafkaAdminUtils adminUtils;

   @Autowired
   private TestConsumer testConsumer;

   @BeforeEach
   void before() {
      this.kafkaProducer = new KafkaProducer<>(this.adminUtils.getKafkaConfigs());
   }

   /**
    * Tests {@link KafkaAdminUtils#topicLags()}.  Must be first to run in parallel in order to setup the consumer.
    */
   @Test
   void testTopicLags() throws ExecutionException, InterruptedException {
      final var aConsumerFuture = this.testConsumer.run(ITERATIONS);
      Thread.sleep(Duration.ofSeconds(1).toMillis());
      IntStream.range(0, ITERATIONS).parallel().forEach(i -> {
         try {
            final var aProducerFuture = this.kafkaProducer.send(new ProducerRecord<>(this.testTopic, this.testKey, objectMapper.writeValueAsString(TEST_DTO)));
            assertThat(aProducerFuture).isNotNull();
         }
         catch (JsonProcessingException e) {
            fail("Encountered unexpected exception", e);
         }
      });
      Thread.sleep(Duration.ofSeconds(3).toMillis());
      assertThat(aConsumerFuture).isNotNull();
      assertThat(aConsumerFuture.get()).isTrue();
   }
}