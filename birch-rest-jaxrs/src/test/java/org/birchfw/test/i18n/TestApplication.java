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
package org.birchfw.test.i18n;

import org.birchframework.framework.cxf.EnableREST;
import org.birchframework.framework.i18n.EnableI18N;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Test integration application
 * @author Keivan Khalichi
 */
@SpringBootApplication(scanBasePackages = "org.birchfw.test.i18n")
@EnableREST
@EnableI18N
public class TestApplication {

   @SuppressWarnings("VariableArgumentMethod")
   public static void main(String... theArgs) {
      SpringApplication.run(TestApplication.class, theArgs);
   }
}