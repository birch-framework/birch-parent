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

package org.birchframework.framework.i18n;

import java.util.HashMap;
import java.util.UUID;
import com.fasterxml.jackson.core.type.TypeReference;
import org.birchframework.framework.jaxrs.Responses;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.*;
import static org.birchframework.framework.i18n.SpanHeadersContainer.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Tests {@link SpanHeadersContainerBean} in an integrated fashion.
 * @author Keivan Khalichi
 */
@SpringBootTest(classes = TestConfiguration.class, webEnvironment = RANDOM_PORT)
@SuppressWarnings("StaticVariableMayNotBeInitialized")
public class SpanningHeadersContainerIT {

   private static final UUID   TEST_UUID   = UUID.randomUUID();
   private static final String TEST_LOCALE = "fr_CA";
   private static ConfigurableApplicationContext application;

   @Autowired
   TestResource testResource;

   @Autowired
   SpanHeadersContainerBean spanHeadersContainerBean;

   @BeforeAll
   static void beforeAll() {
      application = SpringApplication.run(TestApplication.class);
   }

   @BeforeEach
   void before() {
      this.spanHeadersContainerBean.setLocale(TEST_LOCALE);
      this.spanHeadersContainerBean.setCorrelationID(TEST_UUID);
   }

   @AfterAll
   static void afterAll() {
      application.close();
   }

   @Test
   void testInvoke() {
      Responses.of(this.testResource.execute()).ifOKOrElse(
         new TypeReference<HashMap<String, String>>(){},
         map -> {
            assertThat(map.containsKey(LOCALE_HEADER)).isTrue();
            assertThat(map.get(LOCALE_HEADER)).isEqualTo(TEST_LOCALE);
            assertThat(map.containsKey(CORRELATION_ID_HEADER)).isTrue();
            assertThat(map.get(CORRELATION_ID_HEADER)).isEqualTo(TEST_UUID.toString());
         },
         () -> fail("Web service call failed")
      );
   }
}