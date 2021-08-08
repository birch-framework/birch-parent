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

import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.birchframework.security.oauth2.BearerTokenOAuth2Authentication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import static javax.ws.rs.core.MediaType.*;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.*;

/**
 * A CXF resource to test CXF OAuth2 and OIDC auto-configurations.
 * @author Keivan Khalichi
 */
@Service
@Path("/test")
@Produces(APPLICATION_JSON)
@Slf4j
public class TestResource {

   @PreAuthorize("hasAuthority('Domain Users')")
   @Path("/idpTypes")
   @GET
   public Response findIDPTypes() {
      final var anAuth = (BearerTokenOAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
      final var aUserName= anAuth.getName();
      final var aFirstName = anAuth.getTokenAttributes().get(GIVEN_NAME);
      final var aLastName = anAuth.getTokenAttributes().get(FAMILY_NAME);
      final var anIdPRealmKey = anAuth.getIdPRealmKey();
      final var anIdPRealmName = anAuth.getIdPRealmName();
      final var anAuthorities = anAuth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));
      log.info("Username: {} | First Name: {} | Last Name: {} | IdP Realm Key: {} | IdP Realm: {} | Granted Authorities: {}",
               aUserName, aFirstName, aLastName, anIdPRealmKey, anIdPRealmName, anAuthorities);
      final var aTypes = Stream.of(IdentityProviderType.values()).collect(Collectors.toMap(Enum::name, Enum::name));
      return Response.ok(aTypes).build();
   }

   @Path("/config")
   @GET
   @Produces(TEXT_PLAIN)
   public Response configure() {
      return Response.ok("Configured").build();
   }
}