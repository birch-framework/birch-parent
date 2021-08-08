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

package org.birchframework.framework.bridge;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.birchframework.framework.regex.ParseException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;
import static org.birchframework.dto.BirchErrorCode.B20150;

/**
 * Abstraction of Confluent Connector payloads.  Must be specialized in subclass implementations specific to incoming message payloads.
 * Subclasses will be the container for source message payloads.
 * @author Keivan Khalichi
 */
@JsonInclude(value = NON_EMPTY, content = NON_NULL)
@Setter
@Getter
@SuppressWarnings("unused")
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class Payload<T> implements Serializable {

   private static final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

   @JsonProperty
   private       String                     correlationID;
   @JsonProperty
   private       Integer                    deliveryMode;
   @JsonProperty
   private       Destination                destination;
   @JsonProperty
   private       Destination                replyTo;
   @JsonProperty
   private       Long                       expiration;
   @JsonProperty
   private       Map<?, ?>                  map;
   @JsonProperty
   private       String                     messageID;
   @JsonProperty
   private       String                     messageType;
   @JsonProperty
   private       Integer                    priority;
   @JsonProperty
   private       Boolean                    redelivered;
   @JsonProperty
   private       String                     text;
   @JsonProperty
   private       Long                       timestamp;
   @JsonProperty
   private       String                     type;
   @SuppressFBWarnings("SE_BAD_FIELD")
   @JsonProperty
   private final Map<String, PropertyValue> properties = new HashMap<>();

   @SuppressWarnings("FieldCanBeLocal")
   @JsonIgnore
   private T body;

   /**
    * Default constructor.
    */
   public Payload() {
   }

   /**
    * Constructor that initializes the payload.
    * @param theBody the message body
    */
   public Payload(final T theBody) {
      this.body = theBody;
      if (theBody instanceof String) {
         this.text = (String) theBody;
      }
   }

   /**
    * Returns a property value by name.
    * @param thePropertyName the property name
    * @return the property value
    */
   public PropertyValue getProperty(final String thePropertyName) {
      return this.properties.get(thePropertyName);
   }

   /**
    * Sets a property value given its name and value.  Infers value datatype.
    * @param thePropertyName the property name
    * @param thePropertyValue the property value
    */
   public void setProperty(final String thePropertyName, final PropertyValue thePropertyValue) {
      if (StringUtils.isNotBlank(thePropertyName)) {
         this.properties.put(thePropertyName, thePropertyValue);
      }
   }

   public Set<String> propertyNames() {
      return this.properties.keySet();
   }

   @Override
   public String toString() {
      try {
         return objectMapper.writeValueAsString(this);
      }
      catch (JsonProcessingException e) {
         throw new ParseException(B20150, e);
      }
   }
}