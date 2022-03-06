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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import com.fasterxml.jackson.core.type.TypeReference;
import org.birchframework.dto.ErrorCode;
import org.junit.jupiter.api.Test;

import static javax.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.*;
import static org.birchframework.dto.BirchErrorCode.B10120;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link Responses}.
 * @author Keivan Khalichi
 */
@SuppressWarnings("AutoBoxing")
public class ResponsesTest {

   private static final String TEST_ENTITY = "test string";
   private static final URI TEST_CREATED_URI = URI.create("/test");

   /**
    * Tests {@link Responses#getErrorCodeClass()}
    */
   @Test
   public void testErrorCodeClass() {
      var aClass = Responses.getErrorCodeClass();
      assertThat(aClass).isNotNull();
      assertThat(ErrorCode.class).isAssignableFrom(aClass);
   }

   /**
    * Tests {@link Responses#of(Response)}.
    */
   @Test
   public void testOf() {
      final Response aResponse = mock(Response.class);
      final Responses aResponses = Responses.of(aResponse);
      assertThat(aResponses).isNotNull();
      assertThat(aResponses.getResponse()).isSameAs(aResponse);
   }

   /**
    * Tests {@link Responses#ok()}.
    */
   @Test
   public void testOK() {
      Optional.of(Responses.of(Response.ok().build())).ifPresent(r -> {
         assertThat(r.ok()).isTrue();
         assertThat(r.notFound()).isFalse();
         assertThat(r.internalServerError()).isFalse();
         assertThat(r.notModified()).isFalse();
      });
   }

   /**
    * Tests {@link Responses#created()}.
    */
   @Test
   public void testWasCreated() {
      Optional.of(Responses.of(Response.created(TEST_CREATED_URI).build())).ifPresent(r -> {
         assertThat(r.created()).isTrue();
         assertThat(r.notFound()).isFalse();
         assertThat(r.internalServerError()).isFalse();
         assertThat(r.notModified()).isFalse();
      });
   }

   /**
    * Tests {@link Responses#notFound()}.
    */
   @Test
   public void testNotFound() {
      Optional.of(Responses.of(Response.status(NOT_FOUND).build())).ifPresent(r -> {
         assertThat(r.ok()).isFalse();
         assertThat(r.notFound()).isTrue();
         assertThat(r.internalServerError()).isFalse();
         assertThat(r.notModified()).isFalse();
      });
   }

   /**
    * Tests {@link Responses#notModified()}.
    */
   @Test
   public void testNotModified() {
      Optional.of(Responses.of(Response.status(NOT_MODIFIED).build())).ifPresent(r -> {
         assertThat(r.ok()).isFalse();
         assertThat(r.notFound()).isFalse();
         assertThat(r.internalServerError()).isFalse();
         assertThat(r.notModified()).isTrue();
      });
   }

   /**
    * Tests {@link Responses#internalServerError()}.
    */
   @Test
   public void testInternalServerError() {
      Optional.of(Responses.of(Response.status(INTERNAL_SERVER_ERROR).build())).ifPresent(r -> {
         assertThat(r.ok()).isFalse();
         assertThat(r.notFound()).isFalse();
         assertThat(r.internalServerError()).isTrue();
         assertThat(r.notModified()).isFalse();
      });
   }

   /**
    * Tests {@link Responses#ifOK(Class, Consumer)}.
    */
   @Test
   public void testIfOKGivenClass() {
      Responses.of(Response.ok(TEST_ENTITY).build()).ifOK(
         String.class,
         e -> {
            assertThat(e).isNotNull();
            assertThat(e).isEqualTo(TEST_ENTITY);
         }
      );
   }

   /**
    * Tests {@link Responses#ifOK(GenericType, Consumer)}.
    */
   @Test
   public void testIfOKGivenGenericType() {
      Responses.of(Response.ok(new ArrayList<>(List.of(TEST_ENTITY))).build()).ifOK(
         new GenericType<ArrayList<String>>(){},
         e -> {
            assertThat(e).isNotNull();
            assertThat(e).contains(TEST_ENTITY);
         }
      );
   }

   /**
    * Tests {@link Responses#ifOK(TypeReference, Consumer)}.
    */
   @Test
   public void testIfOKGivenTypeReference() {
      Responses.of(Response.ok(new ArrayList<>(List.of(TEST_ENTITY))).build()).ifOK(
         new TypeReference<ArrayList<String>>(){},
         e -> {
            assertThat(e).isNotNull();
            assertThat(e).contains(TEST_ENTITY);
         }
      );
   }

   /**
    * Tests {@link Responses#ifOK(GenericType, Consumer)}.
    */
   @Test
   public void testIfOKGivenMapGenericType() {
      Responses.of(Response.ok(new HashMap<>(Map.of("1", TEST_ENTITY))).build()).ifOK(
         new GenericType<HashMap<String,String>>(){},
         e -> {
            assertThat(e).isNotNull();
            assertThat(e).containsValue(TEST_ENTITY);
         }
      );
   }

   /**
    * Tests {@link Responses#ifOK(TypeReference, Consumer)}.
    */
   @Test
   public void testIfOKGivenMapTypeReference() {
      Responses.of(Response.ok(new HashMap<>(Map.of("1", TEST_ENTITY))).build()).ifOK(
         new TypeReference<HashMap<String,String>>(){},
         e -> {
            assertThat(e).isNotNull();
            assertThat(e).containsValue(TEST_ENTITY);
         }
      );
   }

   /**
    * Tests {@link Responses#ifOKOrElse(Class, Consumer, Consumer)}.
    */
   @Test
   public void testIfOKOrElseGivenClass() {
      Responses.of(Response.ok(TEST_ENTITY).build()).ifOKOrElse(
         String.class,
         e -> {
            assertThat(e).isNotNull();
            assertThat(e).isEqualTo(TEST_ENTITY);
         },
         ec -> fail("Expected a value but none was returned.")
      );
      Responses.of(Response.ok(TEST_ENTITY).build()).ifOKOrElse(
         String.class,
         e -> {
            assertThat(e).isNotNull();
            assertThat(e).isEqualTo(TEST_ENTITY);
         },
         () -> fail("Expected a value but none was returned.")
      );
   }

   /**
    * Tests {@link Responses#ifOKOrElse(GenericType, Consumer, Consumer)}.
    */
   @Test
   public void testIfOKOrElseGivenGenericType() {
      final var aTestList = List.of(TEST_ENTITY);
      Responses.of(Response.ok(new ArrayList<>(aTestList)).build()).ifOKOrElse(
         new GenericType<ArrayList<String>>(){},
         e -> {
            assertThat(e).isNotNull();
            assertThat(e).contains(TEST_ENTITY);
         },
         ec -> fail("Expected a value but none was returned.")
      );
      Responses.of(Response.ok(new ArrayList<>(aTestList)).build()).ifOKOrElse(
         new GenericType<ArrayList<String>>(){},
         e -> {
            assertThat(e).isNotNull();
            assertThat(e).contains(TEST_ENTITY);
         },
         () -> fail("Expected a value but none was returned.")
      );
   }

   /**
    * Tests {@link Responses#ifOKOrElse(TypeReference, Consumer, Consumer)}.
    */
   @Test
   public void testIfOKOrElseGivenTypeReference() {
      final var aTestList = List.of(TEST_ENTITY);
      Responses.of(Response.ok(new ArrayList<>(aTestList)).build()).ifOKOrElse(
         new TypeReference<ArrayList<String>>(){},
         e -> {
            assertThat(e).isNotNull();
            assertThat(e).contains(TEST_ENTITY);
         },
         ec -> fail("Expected a value but none was returned.")
      );
      Responses.of(Response.ok(new ArrayList<>(aTestList)).build()).ifOKOrElse(
         new TypeReference<ArrayList<String>>(){},
         e -> {
            assertThat(e).isNotNull();
            assertThat(e).contains(TEST_ENTITY);
         },
         () -> fail("Expected a value but none was returned.")
      );
   }

   /**
    * Tests {@link Responses#ifCreated(Class, Consumer)}.
    */
   @Test
   public void testIfCreatedGivenClass() {
      Responses.of(Response.created(TEST_CREATED_URI).entity(TEST_ENTITY).build()).ifCreated(
         String.class,
         e -> {
            assertThat(e).isNotNull();
            assertThat(e).isEqualTo(TEST_ENTITY);
         }
      );
   }

   /**
    * Tests {@link Responses#ifCreated(GenericType, Consumer)}.
    */
   @Test
   public void testIfCreatedGivenGenericType() {
      Responses.of(Response.created(TEST_CREATED_URI).entity(new ArrayList<>(List.of(TEST_ENTITY))).build()).ifCreated(
         new GenericType<ArrayList<String>>(){},
         e -> {
            assertThat(e).isNotNull();
            assertThat(e).contains(TEST_ENTITY);
         }
      );
   }

   /**
    * Tests {@link Responses#ifCreatedOrElse(Class, Consumer, Consumer)}.
    */
   @Test
   public void testIfCreatedOrElseGivenClass() {
      Responses.of(Response.created(TEST_CREATED_URI).entity(TEST_ENTITY).build()).ifCreatedOrElse(
         String.class,
         e -> {
            assertThat(e).isNotNull();
            assertThat(e).isEqualTo(TEST_ENTITY);
         },
         ec -> fail("Expected a value but none was returned.")
      );
      Responses.of(Response.created(TEST_CREATED_URI).entity(TEST_ENTITY).build()).ifCreatedOrElse(
         String.class,
         e -> {
            assertThat(e).isNotNull();
            assertThat(e).isEqualTo(TEST_ENTITY);
         },
         () -> fail("Expected a value but none was returned.")
      );
   }

   /**
    * Tests {@link Responses#ifCreatedOrElse(GenericType, Consumer, Consumer)}.
    */
   @Test
   public void testIfCreatedOrElseGivenGenericType() {
      final var aTestList = List.of(TEST_ENTITY);
      Responses.of(Response.created(TEST_CREATED_URI).entity(new ArrayList<>(aTestList)).build()).ifCreatedOrElse(
         new GenericType<ArrayList<String>>(){},
         e -> {
            assertThat(e).isNotNull();
            assertThat(e).contains(TEST_ENTITY);
         },
         ec -> fail("Expected a value but none was returned.")
      );
      Responses.of(Response.created(TEST_CREATED_URI).entity(new ArrayList<>(aTestList)).build()).ifCreatedOrElse(
         new GenericType<ArrayList<String>>(){},
         e -> {
            assertThat(e).isNotNull();
            assertThat(e).contains(TEST_ENTITY);
         },
         () -> fail("Expected a value but none was returned.")
      );
   }

   /**
    * Tests {@link Responses#map(Class, Function)}.
    */
   @Test
   public void testMapGivenClass() {
      final var aValue = Responses.of(Response.ok(TEST_ENTITY).build()).map(
         String.class,
         e -> {
            assertThat(e).isNotNull();
            assertThat(e).isEqualTo(TEST_ENTITY);
            return 5;
         }
      );
      assertThat(aValue).isEqualTo(5);
   }

   /**
    * Tests {@link Responses#map(GenericType, Function)}.
    */
   @Test
   public void testMapGivenGenericType() {
      final var aValue = Responses.of(Response.ok(new ArrayList<>(List.of(TEST_ENTITY))).build()).map(
         new GenericType<ArrayList<String>>(){},
         e -> {
            assertThat(e).isNotNull();
            assertThat(e).contains(TEST_ENTITY);
            return e.size();
         }
      );
      assertThat(aValue).isEqualTo(1);
   }

   /**
    * Tests {@link Responses#map(TypeReference, Function)}.
    */
   @Test
   public void testMapGivenTypeReference() {
      final var aValue = Responses.of(Response.ok(new ArrayList<>(List.of(TEST_ENTITY))).build()).map(
         new TypeReference<ArrayList<String>>(){},
         e -> {
            assertThat(e).isNotNull();
            assertThat(e).contains(TEST_ENTITY);
            return e.size();
         }
      );
      assertThat(aValue).isEqualTo(1);
   }

   /**
    * Tests {@link Responses#ifNotFound(Consumer)}.
    */
   @Test
   public void testIfNotFound() {
      Responses.of(Response.status(NOT_FOUND).entity(B10120).build()).ifNotFound(e -> {
         assertThat(e).isNotNull();
         assertThat(e).isInstanceOf(ErrorCode.class);
         assertThat(e).isEqualTo(B10120);
      });
   }

   /**
    * Tests {@link Responses#ifNotModified(Consumer)}.
    */
   @Test
   public void testIfNotModified() {
      Responses.of(Response.status(NOT_MODIFIED).entity(B10120).build()).ifNotModified(e -> {
         assertThat(e).isNotNull();
         assertThat(e).isInstanceOf(ErrorCode.class);
         assertThat(e).isEqualTo(B10120);
      });
   }

   /**
    * Tests {@link Responses#ifInternalServerError(Consumer)} .
    */
   @Test
   public void testIfInternalServerError() {
      Responses.of(Response.status(INTERNAL_SERVER_ERROR).entity(B10120).build()).ifInternalServerError(e -> {
         assertThat(e).isNotNull();
         assertThat(e).isInstanceOf(ErrorCode.class);
         assertThat(e).isEqualTo(B10120);
      });
   }

   /**
    * Tests {@link Responses#ifBadRequest(Consumer)}.
    */
   @Test
   public void testIfBadRequest() {
      Responses.of(Response.status(BAD_REQUEST).entity(B10120).build()).ifBadRequest(e -> {
         assertThat(e).isNotNull();
         assertThat(e).isInstanceOf(ErrorCode.class);
         assertThat(e).isEqualTo(B10120);
      });
   }

   /**
    * Tests {@link Responses#execute(Class, Consumer)}.
    */
   @Test
   public void testGoGivenClass() {
      Responses.of(Response.ok(TEST_ENTITY).build()).execute(
         String.class,
         e -> {
            assertThat(e).isNotNull();
            assertThat(e).isEqualTo(TEST_ENTITY);
         }
      );
      Responses.of(Response.status(NOT_FOUND).entity(B10120).build()).execute(ErrorCode.class, e -> assertThat(e).isEqualTo(B10120));
   }

   /**
    * Tests {@link Responses#execute(GenericType, Consumer)}.
    */
   @Test
   public void testGoGivenGenericType() {
      Responses.of(Response.ok(new ArrayList<>(List.of(TEST_ENTITY))).build()).execute(
         new GenericType<ArrayList<String>>(){},
         e -> {
            assertThat(e).isNotNull();
            assertThat(e).contains(TEST_ENTITY);
         }
      );
      Responses.of(Response.status(NOT_FOUND).build()).execute(String.class, e -> assertThat(e).isNull());
   }

   /**
    * Tests {@link Responses#execute(TypeReference, Consumer)}.
    */
   @Test
   public void testGoGivenTypeReference() {
      Responses.of(Response.ok(new ArrayList<>(List.of(TEST_ENTITY))).build()).execute(
         new TypeReference<ArrayList<String>>(){},
         e -> {
            assertThat(e).isNotNull();
            assertThat(e).contains(TEST_ENTITY);
         }
      );
      Responses.of(Response.status(NOT_FOUND).build()).execute(String.class, e -> assertThat(e).isNull());
   }
}