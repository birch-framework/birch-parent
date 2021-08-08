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
import lombok.RequiredArgsConstructor;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.birchframework.framework.cxf.EnableREST;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;

import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

/**
 * Test configuration.
 * @author Keivan Khalichi
 */
@EnableREST
@ComponentScan(excludeFilters = @Filter(value = TestApplication.class, type = ASSIGNABLE_TYPE))
@RequiredArgsConstructor
public class TestConfiguration {

   @Bean
   TestResource testResource(@Value("${cxf.jaxrs.client.address}") final URI theBaseURI) {
      return JAXRSClientFactory.create(theBaseURI, TestResource.class);
   }
}