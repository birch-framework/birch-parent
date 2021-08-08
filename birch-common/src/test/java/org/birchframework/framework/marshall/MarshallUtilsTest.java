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
package org.birchframework.framework.marshall;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;
import org.birchframework.framework.dto.TestDTO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.w3c.dom.Document;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link MarshallUtils}.
 * @author Keivan Khalichi
 */
@SpringBootTest(classes = TestConfiguration.class)
public class MarshallUtilsTest {

   private static final String XML_OUTPUT_FILE = "dto-out.xml";
   private static final String XML_INPUT_FILE = "src/test/resources/dto-in.xml";
   private static final String JSON_OUTPUT_FILE = "dto-out.json";
   private static final String JSON_INPUT_FILE = "src/test/resources/dto-in.json";
   private static final TestDTO TEST_DTO = new TestDTO(42, "Meaning of life and everything.");

   /** Bean */
   @Autowired
   private MarshallUtils marshallUtils;

   /**
    * Remove files created by tests run.
    */
   @AfterAll
   public static void cleanUp() {
      try {
         Files.delete(Paths.get(XML_OUTPUT_FILE));
         Files.delete(Paths.get(JSON_OUTPUT_FILE));
      }
      catch (IOException e) {
         fail(String.format("Error deleting file.  Message: %s", e.getMessage()));
      }
   }

   /**
    * Tests {@link MarshallUtils#fromXML(File, Class)}.
    */
   @Test
   public void fromXMLFile() {
      try {
         this.marshallUtils.fromXML(new File(XML_INPUT_FILE), TestDTO.class).ifPresentOrElse(
                 this::assertDTO,
                 () -> fail("Expected a value, but there are none")
         );
      }
      catch (MarshallingError e) {
         fail(String.format("Error unmarshalling object.  Message: %s", e.getMessage()));
      }
   }

   /**
    * Tests {@link MarshallUtils#fromXML(String, Class)}.
    */
   @Test
   public void fromXMLString() {
      try (final Stream<String> aFileStream = Files.lines(Paths.get(XML_INPUT_FILE))) {
         final StringBuilder aStringBuilder = new StringBuilder();
         aFileStream.forEach(aStringBuilder::append);
         this.marshallUtils.fromXML(aStringBuilder.toString(), TestDTO.class).ifPresentOrElse(
                 this::assertDTO,
                 () -> fail("Expected a value, but there are none")
         );
      }
      catch (FileNotFoundException e) {
         fail(String.format("File not found.  Message: %s.", e.getMessage()));
      }
      catch (IOException e) {
         fail(String.format("IO exception.  Message: %s.", e.getMessage()));
      }
      catch (MarshallingError e) {
         fail(String.format("Error marshalling object.  Message: %s", e.getMessage()));
      }
   }

   /**
    * Tests {@link MarshallUtils#fromXML(byte[], Class)}.
    */
   @Test
   public void fromXMLByteArray() {
      try {
         final byte[] aBytes = Files.readAllBytes(Paths.get(XML_INPUT_FILE));
         this.marshallUtils.fromXML(aBytes, TestDTO.class).ifPresentOrElse(
                 this::assertDTO,
                 () -> fail("Expected a value, but there are none")
                 );
      }
      catch (IOException e) {
         fail(String.format("IO exception.  Message: %s.", e.getMessage()));
      }
      catch (MarshallingError e) {
         fail(String.format("Error marshalling object.  Message: %s", e.getMessage()));
      }
   }

   /**
    * Tests {@link MarshallUtils#fromXML(byte[], Class)}.
    */
   @Test
   public void fromXMLFileNamespaceUnaware() throws IOException {
      try {
         this.marshallUtils.fromXML(Files.newInputStream(Paths.get(XML_INPUT_FILE)), TestDTO.class, false, true)
                           .ifPresentOrElse(this::assertDTO, () -> fail("Expected a value, but there are none"));
      }
      catch (MarshallingError e) {
         fail(String.format("Error unmarshalling object.  Message: %s", e.getMessage()));
      }
   }

   /**
    * Tests {@link MarshallUtils#fromXML(byte[], Class)}.
    */
   @Test
   public void parse() throws IOException {
      try {
         final Document aDocument = this.marshallUtils.parse(
                 Files.newInputStream(Paths.get(XML_INPUT_FILE)), false, true
         );
         assertThat(aDocument).isNotNull();
         assertThat(aDocument.getDocumentElement().getTagName()).isEqualTo("test-dto");
         assertThat(aDocument.getDocumentElement().getFirstChild().getNextSibling().getNodeName()).isEqualTo("description");
      }
      catch (MarshallingError e) {
         fail(String.format("Error unmarshalling object.  Message: %s", e.getMessage()));
      }
   }

   /**
    * Tests {@link MarshallUtils#toXML(Object)}.
    */
   @Test
   public void toXML() {
      try (final BufferedWriter aBufferedWriter = Files.newBufferedWriter(Paths.get(XML_OUTPUT_FILE))) {
         final Optional<String> anXMLOptional = this.marshallUtils.toXML(TEST_DTO);
         if (anXMLOptional.isPresent()) {
            aBufferedWriter.write(anXMLOptional.get());
         }
      }
      catch (FileNotFoundException e) {
         fail(String.format("File not found.  Message: %s.", e.getMessage()));
      }
      catch (IOException e) {
         fail(String.format("IO exception.  Message: %s.", e.getMessage()));
      }
      catch (MarshallingError e) {
         fail(String.format("Error marshalling object.  Message: %s", e.getMessage()));
      }
   }

   /**
    * Tests {@link MarshallUtils#fromJSON(byte[], Class)}.
    */
   @Test
   public void fromJSONByteArray() {
      try {
         final byte[] aBytes = Files.readAllBytes(Paths.get(JSON_INPUT_FILE));
         this.marshallUtils.fromJSON(aBytes, TestDTO.class).ifPresentOrElse(this::assertDTO, () -> fail("Expected a value, but there are none"));
      }
      catch (IOException e) {
         fail(String.format("IO exception.  Message: %s.", e.getMessage()));
      }
      catch (MarshallingError e) {
         fail(String.format("Error marshalling object.  Message: %s", e.getMessage()));
      }
   }

   /**
    * Tests {@link MarshallUtils#fromJSON(InputStream, Class)}.
    */
   @Test
   public void fromJSONInputStream() {
      try (final InputStream anInputStream = Files.newInputStream(Paths.get(JSON_INPUT_FILE))){
         this.marshallUtils.fromJSON(anInputStream, TestDTO.class).ifPresentOrElse(this::assertDTO, () -> fail("Expected a value, but there are none"));
      }
      catch (IOException e) {
         fail(String.format("IO exception.  Message: %s.", e.getMessage()));
      }
      catch (MarshallingError e) {
         fail(String.format("Error marshalling object.  Message: %s", e.getMessage()));
      }
   }

   /**
    * Tests {@link MarshallUtils#toJSON(Object)}.
    */
   @Test
   public void toJSON() {
      try (final BufferedWriter aBufferedWriter = Files.newBufferedWriter(Paths.get(JSON_OUTPUT_FILE))) {
         final Optional<String> anXMLOptional = this.marshallUtils.toJSON(TEST_DTO);
         if (anXMLOptional.isPresent()) {
            aBufferedWriter.write(anXMLOptional.get());
         }
      }
      catch (FileNotFoundException e) {
         fail(String.format("File not found.  Message: %s.", e.getMessage()));
      }
      catch (IOException e) {
         fail(String.format("IO exception.  Message: %s.", e.getMessage()));
      }
      catch (MarshallingError e) {
         fail(String.format("Error marshalling object.  Message: %s", e.getMessage()));
      }
   }

   /**
    * Tests {@link MarshallUtils#serialize(Object)} .
    */
   @Test
   public void serialize() {
      this.marshallUtils.serialize(TEST_DTO).ifPresentOrElse(
         object -> {
            assertThat(object).isNotNull();
            assertThat(object).contains(TEST_DTO.getDescription());
            try (final BufferedWriter aBufferedWriter = Files.newBufferedWriter(Paths.get(JSON_OUTPUT_FILE))) {
               aBufferedWriter.write(object);
            }
            catch (FileNotFoundException e) {
               fail(String.format("File not found.  Message: %s.", e.getMessage()));
            }
            catch (IOException e) {
               fail(String.format("IO exception.  Message: %s.", e.getMessage()));
            }
         },
         () -> fail("Expected a value, but there are none")
      );
   }

   /**
    * Tests {@link MarshallUtils#deserialize(String, Class)} .
    */
   @Test
   public void deserialize() {
      final String aJSONString;
      try {
         aJSONString = new String(Files.readAllBytes(Paths.get(JSON_INPUT_FILE)));
         this.marshallUtils.deserialize(aJSONString, TestDTO.class).ifPresentOrElse(
            this::assertDTO,
            () -> fail("Expected a value, but there are none")
         );
      }
      catch (IOException e) {
         fail(String.format("IO exception.  Message: %s.", e.getMessage()));
      }
   }

   /**
    * Utility method to test assertions on DTO.
    * @param aTestDTO optional of test DTO
    */
   private void assertDTO(final TestDTO aTestDTO) {
      assertThat(aTestDTO.getId()).isEqualTo(-40);
      assertThat(aTestDTO.getDescription()).isNotEmpty();
   }
}