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

import org.birchframework.framework.spring.CustomScopesAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;

import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

/**
 * Test configuration for {@link BeansTest}.
 * @author Keivan Khalichi
 */
@Configuration
@ComponentScan(basePackages = {"org.birchframework.framework.spring",
                               "org.birchframework.framework.beans"},
               excludeFilters = {@Filter(classes = MappingAutoConfiguration.class, type = ASSIGNABLE_TYPE),
                                 @Filter(classes = CustomScopesAutoConfiguration.class, type = ASSIGNABLE_TYPE)})
public class TestConfiguration {
}