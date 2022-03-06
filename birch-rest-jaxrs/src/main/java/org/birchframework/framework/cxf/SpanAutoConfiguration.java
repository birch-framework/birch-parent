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

package org.birchframework.framework.cxf;

import org.apache.cxf.bus.spring.SpringBus;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Keivan Khalichi
 */
@Configuration
@EnableAutoConfiguration
public class SpanAutoConfiguration {

   @Bean
   SpanningClientInterceptor spanningClientInterceptor(final SpanHeadersContainerBean theSpanHeadersContainerBean,
                                                       final SpringBus theBus) {
      final var anInterceptor = new SpanningClientInterceptor(theSpanHeadersContainerBean);
      theBus.getOutInterceptors().add(anInterceptor);
      theBus.getOutFaultInterceptors().add(anInterceptor);
      return anInterceptor;
   }

   @Bean
   SpanHeadersContainerBean spanHeadersContainerBean() {
      return new SpanHeadersContainerBean();
   }

   @Bean
   ResourceClientRequestFilter resourceClientRequestFilter(final SpanHeadersContainerBean theSpanHeadersContainerBean) {
      return new ResourceClientRequestFilter(theSpanHeadersContainerBean);
   }
}
