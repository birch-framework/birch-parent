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

package org.birchframework.framework.regex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.java.Log;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link Parser}.
 * @author Keivan Khalichi
 */
@Log
class ParserTest {

   private final Path testFilePath = Paths.get("src/test/resources/parser-test-file.txt");

   /**
    * Tests {@link Parser#parse(String)}.
    */
   @Test
   void testParse() throws IOException {
      final var aParser = Parser.of(TestParseClass.class);
      assertThat(aParser).isNotNull();
      final var anInput = Files.readString(this.testFilePath);
      assertThat(anInput).isNotBlank();
      final var anObjectsList = aParser.parse(anInput);
      assertThat(anObjectsList).size().isEqualTo(4);
      assertThat(anObjectsList.get(2)).isInstanceOf(TestParseClass.class);
      assertThat(((TestParseClass) anObjectsList.get(2)).getAge()).isEqualTo(33);
      assertThat(((TestParseClass) anObjectsList.get(2)).getLastName()).isEqualTo("Holden");
      assertThat(((TestParseClass) anObjectsList.get(1)).getType()).isEqualTo(TestParseClass.Type.ROCK_STAR);
      assertThat(((TestParseClass) anObjectsList.get(3)).getType()).isEqualTo(TestParseClass.Type.CHARACTER);
   }

   /**
    * Tests {@link Parser#parse(java.util.stream.Stream)}.
    */
   @Test
   void testParseLines() throws IOException {
      final var aParser = Parser.of(TestParseClass.class);
      assertThat(aParser).isNotNull();
      try(final var aLines = Files.lines(this.testFilePath)) {
         final var anObjectsStream = aParser.parse(aLines.parallel());
         assertThat(anObjectsStream).isNotNull();
         anObjectsStream.forEach(object -> {
            assertThat(object).isNotNull();
            assertThat(object).isInstanceOf(TestParseClass.class);
            assertThat(((TestParseClass) object).getAge()).isPositive();
            assertThat(((TestParseClass) object).getLastName()).isNotEmpty();
            assertThat(((TestParseClass) object).getType()).isNotNull();
            log.info(ToStringBuilder.reflectionToString(object));
         });
      }
   }
}