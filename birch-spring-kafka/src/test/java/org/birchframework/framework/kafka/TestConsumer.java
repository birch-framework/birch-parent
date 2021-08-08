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
import org.birchframework.framework.dto.ResultDTO;
import org.birchframework.framework.dto.TestDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.*;

/**
 * Test consumer for {@link KafkaSender} testing.
 * @author Keivan Khalichi
 */
@Component
public class TestConsumer {

   @Value("${birch.test.dto.key}")
   private String testKey;

   @KafkaListener(id = "testConsumer", topics = "${birch.test.topic}")
   @SuppressWarnings("AutoBoxing")
   public ResultDTO onReceive(final ConsumerRecord<String, Serializable> record) {
      assertThat(record).isNotNull();
      final var aKey = record.key();
      final var aValue = record.value();
      if (StringUtils.isNotBlank(aKey)) {
         assertThat(aKey).isEqualTo(this.testKey);
      }
      assertThat(aValue).isNotNull();
      assertThat(aValue).isInstanceOf(TestDTO.class);
      final TestDTO aTestDTO = (TestDTO) record.value();
      assertThat(aTestDTO).hasNoNullFieldsOrProperties();
      assertThat(aTestDTO.getId()).isEqualTo(42);
      return new ResultDTO(String.format("Received message with id: %s", aTestDTO.getId()));
   }
}