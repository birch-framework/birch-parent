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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import static org.birchframework.dto.BirchErrorCode.*;

/**
 * (Un)Marshalling utility methods.  Must import {@link org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration}
 * in order to use.
 * @author Keivan Khalichi
 * @see org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
 */
@Component
@Slf4j
@SuppressWarnings({"unused", "unchecked", "AutoBoxing"})
@SuppressFBWarnings("DM_DEFAULT_ENCODING")
public class MarshallUtils {

   /** Synchronized cache of {@link JAXBContext} instances */
   private final Map<Class<?>, JAXBContext> jaxbContextCache = new ConcurrentHashMap<>();

   private final ObjectMapper objectMapper;

   public MarshallUtils(final Jackson2ObjectMapperBuilder theObjectMapperBuilder) {
      this.objectMapper = theObjectMapperBuilder.build();
   }

   @SuppressWarnings("InstanceVariableMayNotBeInitialized")
   private DocumentBuilderFactory documentBuilderFactory;

   /**
    * Unmarshall XML data given class and file.
    * @param theFile the XML data to be unmarshalled
    * @param theClass the target class
    * @param <T> the type of the class
    * @return instance of the unmarshalled object
    * @throws MarshallingError if there are any exceptions
    */
   public <T> Optional<T> fromXML(final File theFile, final Class<T> theClass) throws MarshallingError {
      final Optional<T> aReturnValue;
      if (theFile != null && theFile.exists()) {
         try (InputStream aFileIS = new FileInputStream(theFile)) {
            try {
               aReturnValue = this.fromXML(aFileIS, theClass);
            }
            finally {
               aFileIS.close();
            }
         }
         catch (IOException e) {
            throw new MarshallingError(B10120, e);
         }
      }
      else {
         throw new MarshallingError(B20130);
      }
      return aReturnValue;
   }

   /**
    * Unmarshall XML data given class and data.
    * @param theData the XML data to be unmarshalled
    * @param theClass the target class
    * @param <T> the type of the class
    * @return instance of the unmarshalled object
    * @throws MarshallingError if there are any exceptions
    */
   public <T> Optional<T> fromXML(final String theData, final Class<T> theClass) throws MarshallingError {
      return this.fromXML(theData.getBytes(), theClass);
   }

   /**
    * Unmarshall XML data given class and data.
    * @param theData the XML data to be unmarshalled
    * @param theClass the target class
    * @param <T> the type of the class
    * @return instance of the unmarshalled object
    * @throws MarshallingError if there are any exceptions
    */
   public <T> Optional<T> fromXML(final byte[] theData, final Class<T> theClass) throws MarshallingError {
      final Optional<T> aReturnValue;
      try (InputStream aBytesIS = new ByteArrayInputStream(theData)) {
         aReturnValue = this.fromXML(aBytesIS, theClass);
      }
      catch (IOException e) {
         throw new MarshallingError(B10120, e);
      }
      return aReturnValue;
   }

   /**
    * Unmarshall XML data given class and data.
    * @param theData the XML data to be unmarshalled
    * @param theClass the target class
    * @param <T> the type of the class
    * @return instance of the unmarshalled object
    * @throws MarshallingError if there are any exceptions
    */
   public <T> Optional<T> fromXML(final InputStream theData, final Class<T> theClass) throws MarshallingError {
      final Optional<T> aReturnValue;
      try {
         final Unmarshaller anUnmarshaller = this.unmarshaller(theClass);
         aReturnValue = Optional.of((T) anUnmarshaller.unmarshal(theData));
      }
      catch (Exception e) {
         throw new MarshallingError(B10120, e);
      }
      return aReturnValue;
   }

   /**
    * Unmarshall XML data given class and file; is not namespace-aware and not validating.
    * @param theFile the XML data to be unmarshalled
    * @param theClass the target class
    * @param <T> the type of the class
    * @return instance of the unmarshalled object
    * @throws MarshallingError if there are any exceptions
    */
   public <T> Optional<T> fromXMLNSUnawareNonValidating(final File theFile, final Class<T> theClass) throws MarshallingError {
      final Optional<T> aReturnValue;
      if (theFile != null && theFile.exists()) {
         try (InputStream aFileIS = new FileInputStream(theFile)) {
            try {
               aReturnValue = this.fromXML(aFileIS, theClass, false, false);
            }
            finally {
               aFileIS.close();
            }
         }
         catch (IOException e) {
            throw new MarshallingError(B10120, e);
         }
      }
      else {
         throw new MarshallingError(B20130);
      }
      return aReturnValue;
   }

   /**
    * Unmarshall XML data given class and data; name-space awareness and validating parser are configurable.
    * @param theData the XML data to be unmarshalled
    * @param theClass the target class
    * @param theIsNamespaceAware enable/disable name-space awareness
    * @param theIsValidating enable/disable validating parser
    * @param <T> the type of the class
    * @return instance of the unmarshalled object
    * @throws MarshallingError if there are any exceptions
    */
   public <T> Optional<T> fromXML(final InputStream theData, final Class<T> theClass,
                                  final boolean theIsNamespaceAware, final boolean theIsValidating) throws MarshallingError {

      final Optional<T> aReturnValue;
      final SAXParserFactory aSAXParserFactory = SAXParserFactory.newInstance();

      aSAXParserFactory.setNamespaceAware(theIsNamespaceAware);
      aSAXParserFactory.setValidating(theIsValidating);
      final SAXParser aSAXParser;
      final SAXSource aSource;
      try {
         aSAXParser = aSAXParserFactory.newSAXParser();
         if (log.isDebugEnabled()) {
            log.debug("{}.isNamespaceAware() = {}", aSAXParser.getClass().getName(), aSAXParser.isNamespaceAware());
         }
         final XMLReader anXMLReader = aSAXParser.getXMLReader();
         aSource = new SAXSource(anXMLReader, new InputSource(theData));
      }
      catch (ParserConfigurationException | SAXException e) {
         throw new MarshallingError(B10120, e);
      }

      try {
         final Unmarshaller anUnmarshaller = this.unmarshaller(theClass);
         aReturnValue = Optional.of((T) anUnmarshaller.unmarshal(aSource));
      }
      catch (JAXBException e) {
         throw new MarshallingError(B10120, e);
      }
      return aReturnValue;
   }

   /**
    * Unmarshall an XML document given class and data; name-space awareness and validating parser are configurable.  This is a DOM implementation that
    * uses {@link DocumentBuilder}.
    * @param theDocument the XML data to be unmarshalled
    * @param theClass the target class
    * @param <T> the type of the class
    * @return instance of the unmarshalled object
    * @throws MarshallingError if there are any exceptions
    */
   public <T> Optional<T> fromXMLDocument(final Document theDocument, final Class<T> theClass) throws MarshallingError {
      final Optional<T> aReturnValue;
      try {
         final Unmarshaller anUnmarshaller = this.unmarshaller(theClass);
         aReturnValue = Optional.of((T) anUnmarshaller.unmarshal(theDocument));
      }
      catch (JAXBException e) {
         throw new MarshallingError(B10120, e);
      }
      return aReturnValue;
   }

   /**
    * Parses an XML document given class and data; name-space awareness and validating parser are configurable.  This is a DOM implementation that
    * uses {@link DocumentBuilder}.
    * @param theData the XML data to be unmarshalled
    * @param theIsNamespaceAware enable/disable name-space awareness
    * @param theIsValidating enable/disable validating parser
    * @return instance of a DOM document object
    * @throws MarshallingError if there are any exceptions
    */
   public Document parse(final InputStream theData, final boolean theIsNamespaceAware, final boolean theIsValidating) throws MarshallingError {
      final Document aReturnValue;
      try {
         final DocumentBuilderFactory aDocumentBuilderFactory = this.documentBuilderFactory();
         aDocumentBuilderFactory.setNamespaceAware(theIsNamespaceAware);
         aDocumentBuilderFactory.setValidating(theIsValidating);
         final DocumentBuilder aDocumentBuilder = aDocumentBuilderFactory.newDocumentBuilder();
         aReturnValue = aDocumentBuilder.parse(theData);
      }
      catch (IOException | ParserConfigurationException | SAXException e) {
         throw new MarshallingError(B10120, e);
      }
      return aReturnValue;
   }

   /**
    * Marshall a class to an XML string.
    * @param theObject the object to be marshalled
    * @param <T> the type of the object
    * @return string representation of the marshalled XML
    * @throws MarshallingError if there are any exceptions
    */
   public <T> Optional<String> toXML(final T theObject) throws MarshallingError {
      final Optional<String> aReturnValue;
      try (StringWriter anXMLWriter = new StringWriter()) {
         final JAXBContext aJAXBContext = this.jaxbContextFor(theObject.getClass());
         final Marshaller aMarshaller = aJAXBContext.createMarshaller();
         aMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
         aMarshaller.marshal(theObject, anXMLWriter);
         anXMLWriter.flush();
         aReturnValue = Optional.of(anXMLWriter.toString());
      }
      catch (Exception e) {
         throw new MarshallingError(B20140, e);
      }
      return aReturnValue;
   }

   /**
    * Unmarshall JSON to object
    * @param theData the data input stream
    * @param theClass the target type's class
    * @param <T> the target type
    * @return optional value of the target
    * @throws MarshallingError if there are unmarshalling exceptions
    */
   public <T> Optional<T> fromJSON(final byte[] theData, final Class<T> theClass) throws MarshallingError {
      final Optional<T> aReturnValue;
      try (InputStream aBytesIS = new ByteArrayInputStream(theData)) {
         aReturnValue = this.fromJSON(aBytesIS, theClass);
      }
      catch (IOException e) {
         throw new MarshallingError(B20110, e);
      }
      return aReturnValue;
   }

   /**
    * Unmarshall JSON to object
    * @param theData the data input stream
    * @param theClass the target type's class
    * @param <T> the target type
    * @return optional value of the target
    * @throws MarshallingError if there are unmarshalling exceptions
    */
   public <T> Optional<T> fromJSON(final InputStream theData, final Class<T> theClass) throws MarshallingError {
      final Optional<T> aReturnValue;
      try {
         aReturnValue = Optional.of(this.objectMapper.readValue(theData, theClass));
      }
      catch (Exception e) {
         throw new MarshallingError(B20110, e);
      }
      return aReturnValue;
   }

   /**
    * Marshall a class to JSON string.
    * @param theObject the object to be marshalled
    * @param <T> the type of the object
    * @return string representation of the marshalled JSON
    * @throws MarshallingError if there are any exceptions
    */
   public <T> Optional<String> toJSON(final T theObject) throws MarshallingError {
      final Optional<String> aReturnValue;
      try {
         final var aString = this.objectMapper.writeValueAsString(theObject);
         aReturnValue = Optional.of(aString);
      }
      catch (Exception e) {
         throw new MarshallingError(B20110, e);
      }
      return aReturnValue;
   }

   /**
    * Marshall a class to JSON byte array.
    * @param theObject the object to be marshalled
    * @param <T> the type of the object
    * @return byte array representation of the marshalled JSON
    * @throws MarshallingError if there are any exceptions
    */
   public <T> Optional<byte[]> toJSONBytes(final T theObject) throws MarshallingError {
      final Optional<byte[]> aReturnValue;
      try  {
         final var aBytes = this.objectMapper.writeValueAsBytes(theObject);
         aReturnValue = Optional.of(aBytes);
      }
      catch (Exception e) {
         throw new MarshallingError(B20110, e);
      }
      return aReturnValue;
   }

   /**
    * Convenience method to serialize an object to JSON, but does not throw an exception.
    * @param theObject the object to be serialized
    * @param <T> the type of the object
    * @return the serialized object as a JSON string
    * @see #toJSON(Object)
    */
   public <T> Optional<String> serialize(final T theObject) {
      try {
         return this.toJSON(theObject);
      }
      catch (MarshallingError e) {
         log.error("An error occurred while attempting to serialize {}.  Error message: {}",
                   theObject, e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
         return Optional.empty();
      }
   }

   /**
    * Convenience method to deserialize an object from JSON, but does not throw an exception.
    * @param theJSONString the JSON string
    * @param <T> the type of the object to be deserialized
    * @return the deserialized object
    * @see #toJSON(Object)
    */
   public <T> Optional<T> deserialize(@NotNull final String theJSONString, @NotNull final Class<T> theTargetClass) {
      try {
         return this.fromJSON(theJSONString.getBytes(), theTargetClass);
      }
      catch (MarshallingError e) {
         log.error("An error occurred while attempting to deserialize {}.  Error message: {}",
                   theJSONString, e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
         return Optional.empty();
      }
   }

   /**
    * Convenience method to deserialize an object from JSON, but does not throw an exception.
    * @param theJSONBytes the JSON byte array
    * @param <T> the type of the object to be deserialized
    * @return the deserialized object
    * @see #toJSON(Object)
    */
   public <T> Optional<T> deserialize(@NotNull final byte[] theJSONBytes, @NotNull final Class<T> theTargetClass) {
      try {
         return this.fromJSON(theJSONBytes, theTargetClass);
      }
      catch (MarshallingError e) {
         log.error("An error occurred while attempting to deserialize {}.  Error message: {}",
                   theJSONBytes, e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
         return Optional.empty();
      }
   }

   /**
    * Helper method
    */
   private <T> JAXBContext jaxbContextFor(Class<T> theClass) throws JAXBException {
      final JAXBContext aReturnValue;
      if (this.jaxbContextCache.containsKey(theClass)) {
         aReturnValue = this.jaxbContextCache.get(theClass);
      }
      else {
         aReturnValue = JAXBContext.newInstance(theClass);
         this.jaxbContextCache.put(theClass, aReturnValue);
      }
      return aReturnValue;
   }

   private DocumentBuilderFactory documentBuilderFactory() {
      if (this.documentBuilderFactory == null) {
         this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
      }
      return this.documentBuilderFactory;
   }

   private <T> Unmarshaller unmarshaller(Class<T> theTargetClass) throws MarshallingError {
      final Unmarshaller aReturnValue;
      try {
         final JAXBContext aJAXBContext = this.jaxbContextFor(theTargetClass);
         aReturnValue = aJAXBContext.createUnmarshaller();
      }
      catch (JAXBException e) {
         throw new MarshallingError(B10120, e);
      }
      return aReturnValue;
   }
}