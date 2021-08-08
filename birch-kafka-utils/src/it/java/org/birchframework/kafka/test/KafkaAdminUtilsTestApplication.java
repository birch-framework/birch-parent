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

package org.birchframework.kafka.test;

import org.birchframework.framework.kafka.TestConfiguration;
import org.birchframework.framework.kafka.TestConsumer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;

import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

/**
 * Test Spring Boot application for testing {@link org.birchframework.framework.kafka.KafkaAdminUtils}.
 * @author Keivan Khalichi
 */
@SpringBootApplication
@ComponentScan(basePackages = "org.birchframework",
               excludeFilters = @Filter(classes = {TestConfiguration.class, TestConsumer.class}, type = ASSIGNABLE_TYPE))
@SuppressWarnings("VariableArgumentMethod")
public class KafkaAdminUtilsTestApplication {

   public static void main(final String... theArgs) {
      SpringApplication.run(KafkaAdminUtilsTestApplication.class, theArgs);
   }
}