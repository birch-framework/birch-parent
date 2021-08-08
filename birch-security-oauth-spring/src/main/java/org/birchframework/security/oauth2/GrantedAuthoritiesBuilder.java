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
package org.birchframework.security.oauth2;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Defines a method to build granted authorities given the role claim name and the unmarshalled JWT token.  The implementation can choose to use the
 * parameters or retrieve and return the granted authorities in an entirely different way, for example via a library routine or from a web service.
 * @author Keivan Khalichi
 */
@FunctionalInterface
public interface GrantedAuthoritiesBuilder {

   Collection<GrantedAuthority> build(String theRoleClaim, Jwt theJWT);
}