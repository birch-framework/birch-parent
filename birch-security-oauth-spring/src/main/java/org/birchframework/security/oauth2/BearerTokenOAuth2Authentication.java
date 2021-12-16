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
import org.birchframework.configuration.IdPClassifiable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import org.birchframework.configuration.BirchProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

import static lombok.AccessLevel.PACKAGE;

/**
 * Implementation of {@link org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal} which delegates to
 * {@link DefaultOAuth2AuthenticatedPrincipal} and also includes the {@link BirchProperties.IdPRealm} configuration.
 * @author Keivan Khalichi
 */
public class BearerTokenOAuth2Authentication extends BearerTokenAuthentication {

   @Getter
   @Setter(PACKAGE)
   private                 UserDetails              user;
   @Delegate(types = Includes.class)
   private transient final BirchProperties.IdPRealm idPRealm;

   /**
    * Constructs a {@link BearerTokenAuthentication} with the provided arguments
    * @param thePrincipal   The OAuth 2.0 attributes
    * @param theCredentials The verified token
    * @param theAuthorities The theAuthorities associated with the given token
    * @param theIdPRealm the IdP realm properties
    */
   public BearerTokenOAuth2Authentication(final OAuth2AuthenticatedPrincipal thePrincipal, final OAuth2AccessToken theCredentials,
                                          final Collection<? extends GrantedAuthority> theAuthorities, final BirchProperties.IdPRealm theIdPRealm) {
      super(thePrincipal, theCredentials, theAuthorities);
      this.idPRealm = theIdPRealm;
   }

   public String getIdPRealmKey() {
      return this.idPRealm.getValue();
   }

   public String getIdPRealmName() {
      return this.idPRealm.getName();
   }

   private interface Includes {
      String getDescription();
      IdPClassifiable<?> getType();
      String getText();
   }
}