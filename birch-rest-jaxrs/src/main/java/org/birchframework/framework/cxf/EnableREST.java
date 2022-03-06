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
package org.birchframework.framework.cxf;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.birchframework.configuration.BirchProperties;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Imports necessary auto-configurations in order to register JAX-RS services and client proxies.
 * @see org.apache.cxf.spring.boot.autoconfigure.CxfAutoConfiguration
 * @see org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
 * @see ResourceProxyBeanAutoConfiguration
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@EnableConfigurationProperties(BirchProperties.class)
@Import({JacksonAutoConfiguration.class, SpanAutoConfiguration.class, ResourceProxyBeanAutoConfiguration.class})
@ImportResource({"classpath:META-INF/cxf/cxf.xml", "classpath:META-INF/cxf/cxf-servlet.xml"})
public @interface EnableREST {
}