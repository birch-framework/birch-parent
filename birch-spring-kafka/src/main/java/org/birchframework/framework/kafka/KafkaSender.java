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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.birchframework.configuration.BirchProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.SuccessCallback;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Customized Kafka producer.  Supports:
 * <ul>
 *    <li>Synchronous</li>
 *    <li>Synchronous transactional</li>
 *    <li>Asynchronous</li>
 *    <li>Asynchronous transactional</li>
 * </ul>
 * <p/>
 * Available configurations are:
 * <pre>
 * birch:
 *   kafka:
 *     sender:
 *       wait-time: 2s                    # duration of wait time to wait for response when sending synchronously; default is 2 seconds
 *       allow-non-transactional: true    # whether to configure the Kafka template to allows sending outside of a transaction context; default is true
 * </pre>
 * @param <K> type of key
 * @param <V> type of value
 * @author Keivan Khalichi
 */
@SuppressWarnings({"ConstantConditions", "unused"})
@Component
@ConditionalOnClass(KafkaTemplate.class)
@EnableConfigurationProperties(BirchProperties.class)
public class KafkaSender<K extends String, V extends Serializable> {

   /** Instance of {@link KafkaTemplate} */
   private final KafkaTemplate<K,V> kafkaTemplate;

   private final long waitTimeMillis;

   /**
    * Construct this sender using the provided {@link KafkaTemplate} and {@link BirchProperties}.
    * By default, the instance is not transactional.
    */
   public KafkaSender(final KafkaTemplate<K,V> theTemplate, final BirchProperties theProperties) {
      this.kafkaTemplate = theTemplate;
      final var aSenderProperties = theProperties.getKafka().getSender();
      this.waitTimeMillis = aSenderProperties.getWaitTime().toMillis();
      this.kafkaTemplate.setAllowNonTransactional(aSenderProperties.isAllowNonTransactional());
   }

   /**
    * Send message, synchronously.
    * @param topic topic name
    * @param key message key
    * @param data a message payload implementing {@link Serializable}
    * @throws InterruptedException rethrow when waiting to get results throws the exception
    * @return result, if there are no exceptions
    */
   public Optional<KafkaSendResult<K,V>> send(final String topic, final K key, final V data) throws InterruptedException {
      return this.send(topic, null, key, data);
   }

   /**
    * Send message, synchronously.
    * @param topic topic name
    * @param data a message payload implementing {@link Serializable}
    * @throws InterruptedException rethrow when waiting to get results throws the exception
    * @return result, if there are no exceptions
    */
   public Optional<KafkaSendResult<K,V>> send(final String topic, final V data) throws InterruptedException {
      return this.send(topic, null, null, data);
   }

   /**
    * Send message, synchronously.
    * @param topic topic name
    * @param key message key
    * @param data a message payload implementing {@link Serializable}
    * @throws InterruptedException rethrow when waiting to get results throws the exception
    * @return result, if there are no exceptions
    */
   public Optional<KafkaSendResult<K,V>> send(@Nonnull final String topic, @Nullable final Integer partition, @Nullable final K key, @Nonnull final V data)
          throws InterruptedException {
      final var aResult = new KafkaSendResult<K,V>();
      try {
         aResult.result = this.kafkaTemplate.send(topic, partition, key, data).get(this.waitTimeMillis, MILLISECONDS);
      }
      catch (InterruptedException e) {
         aResult.hasError = true;
         aResult.exception = e;
         throw e;
      }
      catch (ExecutionException | TimeoutException e) {
         aResult.hasError = true;
         aResult.exception = e;
      }
      return Optional.ofNullable(aResult);
   }

   /**
    * Send message synchronously and within a transaction.
    * @param topic topic name
    * @param key message key
    * @param data a message payload implementing {@link Serializable}
    * @throws InterruptedException rethrow when waiting to get results throws the exception
    * @return result, if there are no exceptions
    */
   public Optional<KafkaSendResult<K,V>> sendTransactional(@Nonnull final String topic, @Nullable final Integer partition,
                                                           @Nullable final K key, @Nonnull final V data) {
      final var aReturnValue = new KafkaSendResult<K,V>();
      aReturnValue.result = this.kafkaTemplate.executeInTransaction(operations -> {
         SendResult<K,V> aResult;
         try {
            aResult = operations.send(topic, partition, key, data).get(this.waitTimeMillis, MILLISECONDS);
         }
         catch (InterruptedException | ExecutionException | TimeoutException e) {
            aReturnValue.hasError = true;
            aReturnValue.exception = e;
            aResult = new SendResult<>(null, null);
            Thread.currentThread().interrupt();
         }
         return aResult;
      });
      return Optional.of(aReturnValue);
   }

   /**
    * Sends message to topic asynchronously.
    * @param topic the topic
    * @param data a message payload implementing {@link Serializable}
    */
   public void sendAsync(final String topic, final V data) {
      this.sendAsync(topic, null, null, data, r -> {}, e -> {});
   }

   /**
    * Sends message to topic asynchronously with provided callbacks.
    * @param topic the topic
    * @param data a message payload implementing {@link Serializable}
    * @param successCallback the callback to call upon success
    */
   public void sendAsync(final String topic, final V data, final SuccessCallback<SendResult<K,V>> successCallback, final FailureCallback failureCallback) {
      this.sendAsync(topic, null, null, data, successCallback, failureCallback);
   }

   /**
    * Method to send message, asynchronously.
    * @param topic topic name
    * @param key message key
    * @param data a message payload implementing {@link Serializable}
    */
   public void sendAsync(final String topic, final K key, final V data) {
      this.sendAsync(topic, key, data, r -> {}, e -> {});
   }

   /**
    * Sends message to topic asynchronously with provided callbacks.
    * @param topic the topic
    * @param key the message key
    * @param data a message payload implementing {@link Serializable}
    * @param successCallback the callback to call upon success
    */
   public void sendAsync(final String topic, final K key, final V data,
                         final SuccessCallback<SendResult<K,V>> successCallback, final FailureCallback failureCallback) {
      this.sendAsync(topic, null, key, data, successCallback, failureCallback);
   }

   /**
    * Sends message to topic asynchronously, not transactional, and with provided callbacks.
    * @param topic the topic
    * @param partition the topic partition
    * @param key the key
    * @param data a message payload implementing {@link Serializable}
    * @param successCallback the callback to call upon success
    */
   public void sendAsync(@Nonnull final String topic, @Nullable final Integer partition, @Nullable final K key, @Nonnull final V data,
                         @Nonnull final SuccessCallback<SendResult<K,V>> successCallback, final FailureCallback failureCallback) {
      final var aFuture = this.kafkaTemplate.send(topic, partition, key, data);
      aFuture.addCallback(successCallback, failureCallback);
   }

   /**
    * Sends message to topic asynchronously and within a Kafka transaction, with provided callbacks.
    * @param topic the topic
    * @param partition the topic partition
    * @param key the key
    * @param data a message payload implementing {@link Serializable}
    * @param successCallback the callback to call upon success
    */
   public void sendAsyncTransactional(@Nonnull final String topic, @Nullable final Integer partition, @Nullable final K key, @Nonnull final V data,
                                      @Nonnull final SuccessCallback<SendResult<K,V>> successCallback, final FailureCallback failureCallback) {
      final var aFuture = this.kafkaTemplate.executeInTransaction(operations -> operations.send(topic, partition, key, data));
      Assert.notNull(aFuture, "Future returned by Kafka Template is null");
      aFuture.addCallback(successCallback, failureCallback);
   }
}