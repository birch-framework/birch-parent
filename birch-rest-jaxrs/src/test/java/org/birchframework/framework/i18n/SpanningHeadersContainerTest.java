/*===============================================================
 = Copyright (c) 2022 Birch Framework
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
import org.birchframework.framework.cxf.SpanHeadersContainerBean;
import org.birchframework.framework.jaxrs.Responses;
import org.birchfw.test.i18n.TestApplication;
import org.birchfw.test.i18n.TestResource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.*;
import static org.birchframework.framework.cxf.SpanHeadersContainerBean.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Tests {@link SpanHeadersContainerBean} in an integrated fashion.
 * @author Keivan Khalichi
 */
@SpringBootTest(classes = TestConfiguration.class, webEnvironment = RANDOM_PORT,
                properties = {"spring.cloud.config.enabled=false",
                              "birch.i18n.enabled=true",
                              "spring.main.web-application-type=NONE",
                              "cxf.jaxrs.client.address=http://localhost:8080/api",
                              "birch.i18n.resource-bundle-base-name=messages"})
@SuppressWarnings("StaticVariableMayNotBeInitialized")
public class SpanningHeadersContainerTest {

   private static final UUID                           TEST_UUID   = UUID.randomUUID();
   private static final String                         TEST_LOCALE = "fr_CA";
   private static       ConfigurableApplicationContext application;

   @Autowired
   private TestResource testResource;

   @Autowired
   private SpanHeadersContainerBean spanHeadersContainerBean;

   @BeforeAll
   static void beforeAll() {
      application = SpringApplication.run(TestApplication.class,
                                          "--server.port=8080",
                                          "--spring.cloud.config.enabled=false",
                                          "--cxf.path=/api",
                                          "--cxf.jaxrs.component-scan=true",
                                          "--cxf.jaxrs.classes-scan-packages=org.birchframework, com.fasterxml.jackson.jaxrs.json, org.apache.cxf.metrics, org.birchfw.test.i18n",
                                          "--birch.i18n.enabled=true",
                                          "--birch.i18n.resource-bundle-base-name=messages");
   }

   @AfterAll
   static void afterAll() {
      application.close();
   }

   @Test
   void testInvoke() {
      this.spanHeadersContainerBean.setLocale(TEST_LOCALE);
      this.spanHeadersContainerBean.setCorrelationID(TEST_UUID);
      Responses.of(this.testResource.execute()).ifOKOrElse(
         new TypeReference<HashMap<String, String>>(){},
         map -> {
            assertThat(map.containsKey(CORRELATION_ID_HEADER)).isTrue();
            assertThat(map.get(CORRELATION_ID_HEADER)).isEqualTo(TEST_UUID.toString());
            assertThat(map.containsKey(LOCALE_HEADER)).isTrue();
            assertThat(map.get(LOCALE_HEADER)).isEqualTo(TEST_LOCALE);
         },
         () -> fail("Web service call failed")
      );
   }
}