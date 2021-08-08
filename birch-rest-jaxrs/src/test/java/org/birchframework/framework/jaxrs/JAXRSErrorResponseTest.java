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
package org.birchframework.framework.jaxrs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.birchframework.framework.marshall.MarshallUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link JAXRSErrorResponse}.
 * @author Keivan Khalichi
 */
@SpringBootTest(classes = TestConfiguration.class)
@TestPropertySource(properties = {"spring.jackson.parser.ALLOW_UNQUOTED_FIELD_NAMES: true"})
@SuppressWarnings("WeakerAccess")
public class JAXRSErrorResponseTest {

   public static final String TEST_JSON_FILE = "src/test/resources/jaxrs-error.json";

   @Autowired
   private MarshallUtils marshallUtils;

   @Test
   public void test() throws IOException, URISyntaxException {
      final var aJSON = new String(Files.readAllBytes(Paths.get(TEST_JSON_FILE)));
      final var aResponseError = this.marshallUtils.deserialize(aJSON, JAXRSErrorResponse.class).orElse(null);
      assertThat(aResponseError).isNotNull();
      assertThat(aResponseError.getError()).isEqualTo("InternalServerError");
      assertThat(aResponseError.getMessage()).isEqualTo("A null pointer exception has occurred");
      assertThat(aResponseError.getPath()).isEqualTo(new URI("/api/some/uri"));
      assertThat(aResponseError.getStatusCode()).isEqualTo(500);
   }
}