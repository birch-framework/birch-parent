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
package org.birchframework.framework.i18n;

import java.util.UUID;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.birchframework.framework.cxf.SpanHeadersContainerBean;
import org.birchframework.framework.cxf.SpanningContainerRequestFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.annotation.DirtiesContext;

import static org.birchframework.framework.cxf.SpanHeadersContainerBean.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link SpanningContainerRequestFilter}.
 * @author Keivan Khalichi
 */
@SpringBootTest(classes = InternationalizationAutoConfiguration.class,
                properties = {"cxf.jaxrs.classes-scan: true",
                              "cxf.jaxrs.classes-scan-packages: org.birchframework.framework"})
@DirtiesContext
@SuppressWarnings("AutoBoxing")
class SpanningContainerRequestFilterTest {

   private static final UUID   TEST_UUID    = UUID.randomUUID();
   private static final String LOCALE_FR_CA = "fr_CA";

   @Mock
   private SpanHeadersContainerBean       spanHeadersContainer;
   @Mock
   private ContainerRequestContext        containerRequestContext;
   @InjectMocks
   private SpanningContainerRequestFilter spanningContainerRequestFilter;

   @BeforeEach
   void before() {
      MockitoAnnotations.openMocks(this);
      LocaleContextHolder.setLocale(null);
   }

   /**
    * Tests {@link SpanningContainerRequestFilter#filter(ContainerRequestContext)} when the {@link SpanHeadersContainerBean} has already been
    * created for the thread scope.
    */
   @Test
   void testFilterWhenContainerHasData() {
      when(this.spanHeadersContainer.hasData()).thenReturn(true);
      final var aHeadersMap = this.createHeaders(TEST_UUID, LOCALE_FR_CA);
      when(this.containerRequestContext.getHeaders()).thenReturn(aHeadersMap);
      this.spanningContainerRequestFilter.filter(this.containerRequestContext);
      verify(this.spanHeadersContainer, times(0)).setLocale(anyString());
      verify(this.spanHeadersContainer, times(0)).setCorrelationID(any(UUID.class));
      verify(this.spanHeadersContainer, times(0)).setCorrelationID(anyString());
   }

   /**
    * Tests {@link SpanningContainerRequestFilter#filter(ContainerRequestContext)} when the {@link SpanHeadersContainerBean} has not already been
    * created for the thread scope.
    */
   @Test
   void testFilterWhenContainerDoesNotHaveData() {
      when(this.spanHeadersContainer.hasData()).thenReturn(false);
      final var aHeadersMap = this.createHeaders(TEST_UUID, LOCALE_FR_CA);
      when(this.containerRequestContext.getHeaders()).thenReturn(aHeadersMap);
      this.spanningContainerRequestFilter.filter(this.containerRequestContext);
      verify(this.spanHeadersContainer).setLocale(LOCALE_FR_CA);
      verify(this.spanHeadersContainer).setCorrelationID(TEST_UUID.toString());
   }

   @Test
   void testFilterWithEN_USLocale() {
      when(this.spanHeadersContainer.hasData()).thenReturn(false);
      final var aHeadersMap = this.createHeaders(TEST_UUID, "en-US");
      when(this.containerRequestContext.getHeaders()).thenReturn(aHeadersMap);
      this.spanningContainerRequestFilter.filter(this.containerRequestContext);
      verify(this.spanHeadersContainer).setLocale("en_US");
   }

   @Test
   @Disabled
   void testFilterWithInvalidLocale() {
      when(this.spanHeadersContainer.hasData()).thenReturn(false);
      final var aHeadersMap = this.createHeaders(TEST_UUID, "sw");
      when(this.containerRequestContext.getHeaders()).thenReturn(aHeadersMap);
      this.spanningContainerRequestFilter.filter(this.containerRequestContext);
      verify(this.spanHeadersContainer).setLocale("en_US");
   }

   @Test
   @Disabled
   void testFilterWithNoLocale() {
      when(this.spanHeadersContainer.hasData()).thenReturn(false);
      final var aHeadersMap = this.createHeaders(TEST_UUID);
      when(this.containerRequestContext.getHeaders()).thenReturn(aHeadersMap);
      this.spanningContainerRequestFilter.filter(this.containerRequestContext);
      verify(this.spanHeadersContainer).setLocale("en_US");
   }

   @SuppressWarnings({"VariableArgumentMethod", "SameParameterValue"})
   private MultivaluedHashMap<String, String> createHeaders(final UUID theCorrelationID, final String... theLocales) {
      final var aHeadersMap = new MultivaluedHashMap<String, String>();
      if (ArrayUtils.isNotEmpty(theLocales)) {
         aHeadersMap.addAll(LOCALE_HEADER, theLocales);
      }
      aHeadersMap.add(CORRELATION_ID_HEADER, theCorrelationID.toString());
      return aHeadersMap;
   }
}