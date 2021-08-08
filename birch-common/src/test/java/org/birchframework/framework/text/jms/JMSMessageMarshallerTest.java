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

package org.birchframework.framework.text.jms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link JMSMessageMarshaller}.
 * @author Keivan Khalichi
 */
class JMSMessageMarshallerTest {

   @BeforeEach
   void before() {
   }

   /**
    * Tests {@link JMSMessageMarshaller#deserialize(String)}.
    */
   @Test
   void testParse() throws IOException {
      final var aFileContents = Files.readString(Paths.get("src/test/resources/jms-message.txt"));
      final var aJMSMessage = JMSMessageMarshaller.deserialize(aFileContents);
      assertThat(aJMSMessage).isNotNull();
      assertThat(aJMSMessage.getProperties()).isNotNull();
      assertThat(aJMSMessage.getProperties()).isNotEmpty();
      assertThat(aJMSMessage.getTextMessage()).isNotBlank();
   }

   /**
    * Tests {@link JMSMessageMarshaller#deserialize(String)}.
    */
   @Test
   void testParseBlankPayload() throws IOException {
      try {
         final var aMessage = JMSMessageMarshaller.deserialize("");
         fail("Expected exception to be thrown, but was not");
      }
      catch (Exception e) {
         assertThat(e).isInstanceOf(JMSMessageMarshallerException.class);
      }
   }
}