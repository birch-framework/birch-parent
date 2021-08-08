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

import org.birchframework.configuration.BirchProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * Test configuration for {@link ResourceClientProxyTest}.
 * @author Keivan Khalichi
 */
@ComponentScan({"org.birchframework.configuration",
                "org.birchframework.framework.spring",
                "org.birchframework.framework.cxf",
                "org.springframework.boot.autoconfigure.jackson",
                "org.birchfw.test.framework.cxf"})
@EnableConfigurationProperties(BirchProperties.class)
public class TestConfiguration {
}