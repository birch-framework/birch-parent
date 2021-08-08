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

import org.birchframework.security.oauth2.BearerTokenOAuth2Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Test user details service.
 * @author Keivan Khalichi
 */
@Component
public class TestUserDetailsService implements UserDetailsService {

   @Override
   public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
      final var anAuth = (BearerTokenOAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
      return new User(username, anAuth.getCredentials().toString(), true, true, true, true, anAuth.getAuthorities());
   }
}