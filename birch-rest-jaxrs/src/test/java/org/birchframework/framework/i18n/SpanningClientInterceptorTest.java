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

import java.util.List;
import java.util.UUID;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.message.Message;
import org.birchframework.framework.cxf.SpanHeadersContainerBean;
import org.birchframework.framework.cxf.SpanningClientInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.birchframework.framework.cxf.SpanHeadersContainerBean.*;
import static java.lang.Boolean.*;
import static org.apache.cxf.message.Message.PROTOCOL_HEADERS;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SpanningClientInterceptor}.
 * @author Keivan Khalichi
 */
class SpanningClientInterceptorTest {

   private static final UUID   TEST_UUID   = UUID.randomUUID();
   private static final String TEST_LOCALE = "fr_CA";

   @Mock
   private SpanHeadersContainerBean  spanHeadersContainer;
   @Mock
   private Message                   message;
   @InjectMocks
   private SpanningClientInterceptor spanningClientInterceptor;

   @BeforeEach
   @SuppressWarnings("AutoBoxing")
   void before() {
      MockitoAnnotations.openMocks(this);
      when(this.spanHeadersContainer.getLocale()).thenReturn(TEST_LOCALE);
      when(this.spanHeadersContainer.getCorrelationID()).thenReturn(TEST_UUID);
      when(this.spanHeadersContainer.hasData()).thenReturn(TRUE);
      when(this.message.get(PROTOCOL_HEADERS)).thenReturn(new MetadataMap<>());
   }

   /**
    * Tests {@link SpanningClientInterceptor#handleMessage(Message)}.
    * @author Keivan Khalichi
    */
   @Test
   @SuppressWarnings("unchecked")
   void testHandleMessage() {
      this.spanningClientInterceptor.handleMessage(this.message);
      final MetadataMap<String, Object> aHeaders = (MetadataMap<String, Object>) this.message.get(PROTOCOL_HEADERS);
      assertThat(aHeaders).isNotEmpty();
      assertThat(aHeaders).containsKey(LOCALE_HEADER).containsValue(List.of(TEST_LOCALE));
      assertThat(aHeaders).containsKey(CORRELATION_ID_HEADER).containsValue(List.of(TEST_UUID.toString()));
   }
}