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
import java.util.List;
import java.util.concurrent.Executor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;

/**
 * Application configuration for all tests within this package hierarchy.
 * @author Keivan Khalichi
 */
@Configuration
@ComponentScan(basePackages = {"org.birchframework.framework.kafka",
                               "org.birchframework.resource",
                               "org.birchframework.configuration",
                               "org.springframework.boot.autoconfigure.kafka"})
@EnableAsync
public class TestConfiguration {

   @Bean
   public Consumer<String, String> consumer(@Value("${birch.test.topic}") final String theTopic, final KafkaAdminUtils theKafkaAdminUtils) {
      final var aConfigs = new HashMap<>(theKafkaAdminUtils.getKafkaConfigs());
      aConfigs.put(GROUP_ID_CONFIG, "testConsumer");
      final var aConsumer =  new KafkaConsumer<String, String>(aConfigs);
      aConsumer.subscribe(List.of(theTopic));
      return aConsumer;
   }

   @Bean
   public Executor threadPoolTaskExecutor() {
       return new ThreadPoolTaskExecutor();
   }
}