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
import java.util.Map.Entry;
import java.util.concurrent.Future;
import javax.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.*;

/**
 * Test consumer for {@link KafkaAdminUtilsTest}.
 * @author Keivan Khalichi
 */
@Component
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings({"AutoBoxing", "AutoUnboxing"})
public class TestConsumer {

   private final Consumer<String, String> consumer;

   @Value("${birch.test.topic}")
   private String testTopic;

   @Autowired
   private KafkaAdminUtils adminUtils;

   @PreDestroy
   void destroy() {
      this.consumer.close();
   }

   @Async
   public Future<Boolean> run(final int theIterations) {
      int aProcessedCount = theIterations;
      boolean anIsFoundLag = false;
      do {
         final var aRecords = this.fetchRecords(Duration.ofMillis(100));
         aProcessedCount -= aRecords.count();
         final var aTopicLags = this.adminUtils.topicLags();
         assertThat(aTopicLags).isNotNull();
         final var aLag = aTopicLags.entrySet()
                                    .stream()
                                    .filter(e -> StringUtils.equals(this.testTopic, e.getKey()))
                                    .findFirst()
                                    .map(Entry::getValue)
                                    .orElse(0L);
         if (aLag > 0) {
            anIsFoundLag = true;
         }
         log.info("Topic: {} | Lag: {}", this.testTopic, aLag);
      } while(aProcessedCount > 0);
      return new AsyncResult<>(anIsFoundLag);
   }

   public ConsumerRecords<String, String> fetchRecords(final Duration thePollDuration) {
      final var aRecords = this.consumer.poll(thePollDuration);
      this.consumer.commitSync();
      return aRecords;
   }
}