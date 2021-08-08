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
package org.birchfw.test.framework.cxf;

import com.fasterxml.jackson.core.type.TypeReference;
import org.birchframework.framework.jaxrs.Responses;
import org.birchfw.test.dto.Bitcoin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests {@link org.birchframework.framework.cxf.ResourceProxyBeanAutoConfiguration}.
 * @author Keivan Khalichi
 */
@SuppressWarnings({"StaticVariableMayNotBeInitialized", "StaticVariableUsedBeforeInitialization"})
public class ResourceClientProxyTest {

   private static ConfigurableApplicationContext context;

   private CoinDeskResource coinDeskResource;

   /**
    * Because of classloader issues, this is the only way to start the Spring Boot test instead of the traditional way using
    * {@link org.springframework.boot.test.context.SpringBootTest} .
    */
   @BeforeAll
   static void beforeAll() {
      context = SpringApplication.run(TestConfiguration.class);
   }

   @AfterAll
   static void afterAll() {
      context.close();
   }

   @BeforeEach
   void before() {
      this.coinDeskResource = context.getBean(CoinDeskResource.class);
   }

   /**
    * Tests <a href="https://www.coindesk.com/coindesk-api">free online API</a>.
    */
   @Test
   void testExecute() {
      Responses.of(this.coinDeskResource.currentPrice()).ifOKOrElse(
         new TypeReference<Bitcoin>() {},
         bitcoin -> {
            assertThat(bitcoin).isNotNull();
            assertThat(bitcoin.getBpi()).isNotEmpty();
            assertThat(bitcoin.getBpi()).containsKey("USD");
            assertThat(bitcoin.getBpi().get("USD")).isNotNull();
         },
         () -> fail("Service call was not OK")
      );
   }
}