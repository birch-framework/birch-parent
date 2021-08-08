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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

/**
 * {@link KafkaAdminUtils} metrics bean that obtains and publishes Kafka metrics supplied by the utility class, to Micrometer.  Registers metrics upon startup
 * and re-registers them at configurable intervals.  Samples metrics upon startup and at configurable intervals.
 * @author Keivan Khalichi
 */
@Slf4j
public class KafkaAdminUtilsMetrics {

   private final KafkaAdminUtils                    kafkaAdminUtils;
   private final MeterRegistry                      meterRegistry;
   private final Map<String, AtomicReference<Long>> topicLags = new HashMap<>();
   private final Map<String, Gauge>                 gauges    = new Hashtable<>();

   KafkaAdminUtilsMetrics(final KafkaAdminUtils theKafkaAdminUtils, final MeterRegistry theMeterRegistry) {
      this.kafkaAdminUtils = theKafkaAdminUtils;
      this.meterRegistry   = theMeterRegistry;
   }

   @Scheduled(fixedRateString = "#{${birch.kafka.admin.sample-interval-ms:} ?: T(java.time.Duration).ofSeconds(5).toMillis()}")
   void sampleGauges() {
      final var aTopicLags = this.kafkaAdminUtils.topicLags();
      aTopicLags.forEach((topic, lag) -> {
         if (this.topicLags.containsKey(topic)) {
            this.topicLags.get(topic).set(lag);
         }
         else {
            this.topicLags.put(topic, new AtomicReference<>(lag));
         }
      });
      if (log.isDebugEnabled()) {
         log.debug("Sampled gauges from data: {}", this.topicLags.toString());
      }
   }

   /**
    * Re-registers gauges from the latest topic lags sampling.  This is done so that new topics are picked up.
    */
   @Scheduled(initialDelay = 2_000, fixedRateString = "#{${birch.kafka.admin.re-register-interval-ms:} ?: T(java.time.Duration).ofHours(6).toMillis()}")
   void registerGauges() {
      if (!CollectionUtils.isEmpty(this.gauges)) {
         this.gauges.forEach((topic, gauge) -> this.meterRegistry.remove(gauge.getId()));
         this.gauges.clear();
      }
      this.topicLags.forEach((topic, atomicLag) -> {
                       final var aGauge = Gauge.builder("birch.kafka.consumer.lag", atomicLag::get)
                                               .description(String.format("%s consumer lag", topic))
                                               .tag("topic", topic)
                                               .register(this.meterRegistry);
                       log.info("Registered consumer lag gauge: {}", aGauge.getId());
                       this.gauges.put(topic, aGauge);
                    });
   }
}