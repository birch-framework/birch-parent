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

import java.net.URI;
import javax.ws.rs.client.ClientRequestFilter;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.birchframework.configuration.BirchProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * Test integration application
 * @author Keivan Khalichi
 */
@SpringBootApplication
@ComponentScan({"org.birchframework.framework.i18n",
                "org.birchframework.framework.cxf",
                "org.birchframework.configuration",
                "org.birchframework.framework.spring",
                "org.apache.cxf.spring.boot.autoconfigure.openapi",
                "org.springframework.boot.autoconfigure.jackson"})
@EnableConfigurationProperties(BirchProperties.class)
public class TestApplication {

   @Bean
   @ConditionalOnMissingBean(ClientRequestFilter.class)
   TestResource testResource(@Value("${cxf.jaxrs.client.address}") final URI theBaseURI) {
      return JAXRSClientFactory.create(theBaseURI.toString(), TestResource.class);
   }
}