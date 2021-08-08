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
package org.birchframework.test.security.oauth2;

import org.birchframework.security.oauth2.EnableOAuth2ResourceServerSecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

/**
 * Integration test integration application.
 * @author Keivan Khalichi
 */
@SpringBootApplication(scanBasePackages = {"org.birchframework.framework.spring",
                                           "org.birchframework.test.security.oauth2"})
@EnableOAuth2ResourceServerSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class OAuth2Application {

   @SuppressWarnings("VariableArgumentMethod")
   public static void main(final String... theArgs) {
      SpringApplication.run(OAuth2Application.class, theArgs);
   }
}