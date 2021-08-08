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

package org.birchframework.framework.logging;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Auto-configuration for logging aspect {@link LoggingAspect}.
 * @author Keivan Khalichi
 */
@Configuration
@EnableAutoConfiguration
@ConditionalOnProperty(prefix = "birch.aspect", name = "logging-trace", havingValue = "enabled")
@EnableAspectJAutoProxy
@SuppressWarnings("SpringFacetCodeInspection")
public class LoggingAspectAutoConfiguration {

   @Bean
   @ConditionalOnMissingBean
   LoggingAspect loggingAspect() {
      return new LoggingAspect();
   }
}