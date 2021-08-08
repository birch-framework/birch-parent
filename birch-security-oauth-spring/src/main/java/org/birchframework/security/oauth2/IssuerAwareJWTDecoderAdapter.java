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

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import com.fasterxml.jackson.core.type.TypeReference;
import org.birchframework.framework.jaxrs.Responses;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.jaxrs.client.WebClient;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * An implementation of {@link JwtDecoder} that is issuer aware.  This decoder validates the issuer, obtains configurations from the issuer, and creates
 * a delegate decoder that uses the issuer specified validator based on the issuer configuration when decoding JWT tokens.
 * @author Keivan Khalichi
 */
public class IssuerAwareJWTDecoderAdapter implements JwtDecoder {

   public static final String OIDC_METADATA_PATH = "/.well-known/openid-configuration";

   protected static final TypeReference<HashMap<String, Object>> ISSUER_RESPONSE_TYPE = new TypeReference<>(){};

   private final JwtDecoder delegate;

   public IssuerAwareJWTDecoderAdapter(final String theIssuerURI) {
      this(theIssuerURI, false);
   }

   public IssuerAwareJWTDecoderAdapter(final String theIssuerURI, final boolean theIsDisableSSLValidation) {
      final var aWebClient = WebClient.create(theIssuerURI);
      if (theIsDisableSSLValidation) {
         final var aConduit = WebClient.getConfig(aWebClient).getHttpConduit();
         var aParams = aConduit.getTlsClientParameters();
         if (aParams == null) {
            aParams = new TLSClientParameters();
            aConduit.setTlsClientParameters(aParams);
         }
         aParams.setTrustManagers(new TrustManager[]{new DummyX509TrustManager()});
         aParams.setDisableCNCheck(true);
      }
      final var anAtomicDecoder = new AtomicReference<JwtDecoder>();
      Responses.of(aWebClient.path(OIDC_METADATA_PATH).get()).ifOKOrElse(
         ISSUER_RESPONSE_TYPE,
         configuration -> {
            final String anIssuer = (String) configuration.getOrDefault("issuer", "(none)");
            if (!StringUtils.equals(anIssuer, theIssuerURI)) {
               throw new IllegalStateException(String.format("The issuer %s configuration did not match %s", anIssuer, theIssuerURI));
            }
            final var aJWTValidator = JwtValidators.createDefaultWithIssuer(theIssuerURI);
            final var aDecoder = NimbusJwtDecoder.withJwkSetUri(configuration.get("jwks_uri").toString()).build();
            aDecoder.setJwtValidator(aJWTValidator);
            anAtomicDecoder.set(aDecoder);
         },
         errorCode -> {
            throw new IllegalStateException(String.format("Unable to obtain configuration from the issuer %s", theIssuerURI));
         }
      );
      this.delegate = anAtomicDecoder.get();
   }

   @Override
   public Jwt decode(final String token) throws JwtException {
      return this.delegate.decode(token);
   }

   protected static class DummyX509TrustManager implements X509TrustManager {

      @Override
      public X509Certificate[] getAcceptedIssuers() {
         return null;
      }

      @Override
      public void checkClientTrusted(X509Certificate[] certs, String authType) {
         // no check
      }

      @Override
      public void checkServerTrusted(X509Certificate[] certs, String authType) {
         // no check
      }
   }
}