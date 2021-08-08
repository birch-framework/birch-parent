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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * When applied to JAX-RS {@link javax.ws.rs.Path} annotated interfaces, creates a client proxy bean for such interfaces, which can then be auto-wired by type.
 * @see ResourceProxyBeanAutoConfiguration
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface AutoProxy {

   /** Base URI used to create proxy of the annotated class */
   String baseURI();

   /** List of JAX-RS/CXF provider or feature classes (i.e. interceptors, filters, etc.) which are applied to the proxy */
   Class<?>[] providers() default {};

   /** Create thread-safe proxy */
   boolean threadSafe() default true;
}