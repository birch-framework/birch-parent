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
package org.birchframework.framework.beans;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Identifies a Model POJO as mappable to its superclass, which is often a DTO POJO.  This is useful when there are layered microservices wherein a Model POJO
 * that is part of a Spring Data mapping needs to be transfered to a DTO in order to traverse services (e.g. microservices) boundaries.  This annotation is
 * used by {@link MappingAutoConfiguration} to register such mapping for optimum peromance.
 * @see MappingAutoConfiguration
 * @author Keivan Khalichi
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface MappingModel {
   /** Whether to map null values, bidirectionally */
   boolean mapNulls() default true;
   /** Properties to exclude from mapping, bidirectionally */
   String[] exclude() default {};
}