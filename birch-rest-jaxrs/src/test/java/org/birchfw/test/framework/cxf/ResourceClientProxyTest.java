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

import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.assertj.core.util.Throwables;
import org.birchframework.framework.cxf.AutoProxy;
import org.birchframework.framework.jaxrs.Responses;
import org.birchfw.test.dto.Bitcoin;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests {@link org.birchframework.framework.cxf.ResourceProxyBeanAutoConfiguration}.
 * @author Keivan Khalichi
 */
public class ResourceClientProxyTest {

   /**
    * Tests <a href="https://www.coindesk.com/coindesk-api">free online API</a>.
    */
   @Test
   void testExecute() {
      try (var aContext = this.runSpringBoot("https://api.coindesk.com/v1")) {
         final var aCoinDeskResource = aContext.getBean(CoinDeskResource.class);
         assertThat(Proxy.isProxyClass(aCoinDeskResource.getClass())).isTrue();
         Responses.of(aCoinDeskResource.currentPrice()).ifOKOrElse(
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

   /**
    * Tests with invalid {@link AutoProxy#baseURI()}.
    */
   @Test
   void testInvalidURL() {
      try (ConfigurableApplicationContext ignore = this.runSpringBoot("htps://api.coindesk.com/v1")) {
         fail("Expected exception was not thrown");
      }
      catch (Exception e) {
         assertThat(Throwables.getRootCause(e)).isInstanceOf(MalformedURLException.class);
      }
   }

   private ConfigurableApplicationContext runSpringBoot(final String theCoinDeskURL) {
      return SpringApplication.run(TestConfiguration.class,
                                   "--spring.cloud.config.enabled=false",
                                   "--server.port=18080",
                                   "--cxf.jaxrs.component-scan=false",
                                   "--cxf.jaxrs.classes-scan=false",
                                   String.format("--services.coindesk.address=%s", theCoinDeskURL));
   }
}