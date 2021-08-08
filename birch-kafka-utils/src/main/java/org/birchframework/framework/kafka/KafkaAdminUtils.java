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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import com.google.common.base.Throwables;
import org.birchframework.configuration.BirchProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.context.properties.PropertyMapper;

import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.apache.kafka.clients.producer.ProducerConfig.*;
import static org.apache.kafka.common.config.SaslConfigs.*;
import static org.apache.kafka.common.config.SslConfigs.SSL_PROTOCOL_CONFIG;

/**
 * Kafka administration utilities.
 * @author Keivan Khalichi
 */
@Slf4j
@SuppressWarnings("PMD.TooManyStaticImports")
public class KafkaAdminUtils {

   private static final PropertyMapper mapper = PropertyMapper.get();

   @Getter
   private final Map<String, Object>           kafkaConfigs;
   private final KafkaConsumer<Object, Object> kafkaConsumer;
   private final AdminClient                   adminClient;

   KafkaAdminUtils(final BirchProperties theProperties) {
      final var aKafkaProperties = theProperties.getKafka().getAdmin();

      final var aKafkaConfigs = new HashMap<String, Object>();
      aKafkaConfigs.put(BOOTSTRAP_SERVERS_CONFIG, String.join(",", aKafkaProperties.getBootstrapServers()));
      mapper.from(aKafkaProperties.getSslProtocol()).when(StringUtils::isNotBlank).to(v -> aKafkaConfigs.put(SSL_PROTOCOL_CONFIG, v));
      mapper.from(aKafkaProperties.getSecurityProtocol()).when(StringUtils::isNotBlank).to(v -> aKafkaConfigs.put(SECURITY_PROTOCOL_CONFIG, v));
      mapper.from(aKafkaProperties.getKeySerializer()).whenNonNull().to(v -> aKafkaConfigs.put(KEY_SERIALIZER_CLASS_CONFIG, v));
      mapper.from(aKafkaProperties.getValueSerializer()).whenNonNull().to(v -> aKafkaConfigs.put(VALUE_SERIALIZER_CLASS_CONFIG, v));
      aKafkaConfigs.put(KEY_DESERIALIZER_CLASS_CONFIG, ObjectUtils.defaultIfNull(aKafkaProperties.getKeyDeserializer(), StringDeserializer.class));
      aKafkaConfigs.put(VALUE_DESERIALIZER_CLASS_CONFIG, ObjectUtils.defaultIfNull(aKafkaProperties.getValueDeserializer(), StringDeserializer.class));
      mapper.from(aKafkaProperties.getSasl().getJaasConfig()).whenNonNull().to(v -> aKafkaConfigs.put(SASL_JAAS_CONFIG, v));
      mapper.from(aKafkaProperties.getSasl().getMechanism()).whenNonNull().to(v -> aKafkaConfigs.put(SASL_MECHANISM, v));

      this.kafkaConfigs  = Collections.unmodifiableMap(aKafkaConfigs);
      this.kafkaConsumer = new KafkaConsumer<>(aKafkaConfigs);
      this.adminClient   = AdminClient.create(aKafkaConfigs);
   }

   @PreDestroy
   void init() {
      this.kafkaConsumer.close();
   }

   /**
    * Calculates consumer lag for all active consumers.
    * <b>NOTE:</b> if a topic has no consumers, then it will not appear in the return mapping.
    * @return map of topic name to total consumer lag
    */
   @SuppressWarnings({"AutoBoxing", "AutoUnboxing"})
   public Map<String, Long> topicLags() {
      final StopWatch aStopWatch = log.isDebugEnabled() ? StopWatch.createStarted() : null;

      try {
         final var aConsumerGroupIDs = this.adminClient.listConsumerGroups()
                                                       .valid()
                                                       .thenApply(result -> result.stream().map(ConsumerGroupListing::groupId).collect(Collectors.toList()))
                                                       .get();
         final Map<TopicPartition, OffsetAndMetadata> aTopicAndOffsetMap = new HashMap<>();
         aConsumerGroupIDs.stream()
                          .map(id -> this.adminClient.listConsumerGroupOffsets(id).partitionsToOffsetAndMetadata())
                          .forEach(future -> {
                             try {
                                aTopicAndOffsetMap.putAll(future.get());
                             }
                             catch (InterruptedException | ExecutionException e) {
                                log.warn("Unable to retrieve topic offsets for a consumer group; error message: {}", Throwables.getRootCause(e).getMessage());
                             }
                          });
         final var aTopicEndOffsets = this.kafkaConsumer.endOffsets(aTopicAndOffsetMap.keySet());
         final var aTopicPartitionLagMap = aTopicAndOffsetMap.entrySet()
                                                             .stream()
                                                             .collect(Collectors.toMap(
                                                                Entry::getKey,
                                                                entry -> {
                                                                   final var anEndOffset = aTopicEndOffsets.get(entry.getKey());
                                                                   final var aLag = anEndOffset - entry.getValue().offset();
                                                                   return aLag < 0 ? 0 : aLag;
                                                                }
                                                             ));
         final var aTopicLags = new HashMap<String, Long>();
         aTopicPartitionLagMap.forEach((key, value) -> aTopicLags.compute(key.topic(), (k, v) -> v == null ? value : v + value));
         return aTopicLags;
      }
      catch (InterruptedException | ExecutionException e) {
         log.warn("Unable to retrieve consumer groups; error message: {}", Throwables.getRootCause(e).getMessage());
         return Collections.emptyMap();
      }
      finally {
         if (aStopWatch != null) {
            log.debug("Completed lag calculation in {} milliseconds", aStopWatch.getTime());
         }
      }
   }
}