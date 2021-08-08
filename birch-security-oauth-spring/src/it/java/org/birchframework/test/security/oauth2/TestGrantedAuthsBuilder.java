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

import java.util.Collection;
import java.util.List;
import org.birchframework.security.oauth2.GrantedAuthoritiesBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Custom implementation of {@link GrantedAuthoritiesBuilder} for the pupose of testing.
 * @author Keivan Khalichi
 */
public class TestGrantedAuthsBuilder implements GrantedAuthoritiesBuilder {

   @Override
   public Collection<GrantedAuthority> build(final String theRoleClaim, final Jwt theJWT) {
      return List.of(new SimpleGrantedAuthority("Domain Users"));
   }
}