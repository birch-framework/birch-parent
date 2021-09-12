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

package org.birchframework.dto.payload;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.ToString.Include;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.birchframework.dto.payload.PropertyValue.PropertyType.*;

/**
 * Generic property value.
 * @author Keivan Khalichi
 */
@JsonInclude(NON_NULL)
@Getter
@NoArgsConstructor
@SuppressWarnings({"InstanceVariableMayNotBeInitialized", "unused"})
public class PropertyValue implements Serializable {

   public static final String BOOLEAN_TYPE_NAME = "boolean";
   public static final String BYTE_TYPE_NAME    = "byte";
   public static final String SHORT_TYPE_NAME   = "short";
   public static final String INTEGER_TYPE_NAME = "integer";
   public static final String LONG_TYPE_NAME    = "long";
   public static final String DOUBLE_TYPE_NAME  = "double";
   public static final String FLOAT_TYPE_NAME   = "float";
   public static final String STRING_TYPE_NAME  = "string";

   @JsonFormat(shape = STRING)
   @RequiredArgsConstructor
   @ToString(includeFieldNames = false, onlyExplicitlyIncluded = true)
   public enum PropertyType {
      @JsonProperty(BOOLEAN_TYPE_NAME)
      BOOLEAN(BOOLEAN_TYPE_NAME, Boolean.class),
      @JsonProperty(BYTE_TYPE_NAME)
      BYTE(BYTE_TYPE_NAME, Byte.class),
      @JsonProperty(SHORT_TYPE_NAME)
      SHORT(SHORT_TYPE_NAME, Short.class),
      @JsonProperty(INTEGER_TYPE_NAME)
      INTEGER(INTEGER_TYPE_NAME, Integer.class),
      @JsonProperty(LONG_TYPE_NAME)
      LONG(LONG_TYPE_NAME, Long.class),
      @JsonProperty(DOUBLE_TYPE_NAME)
      DOUBLE(DOUBLE_TYPE_NAME, Double.class),
      @JsonProperty(FLOAT_TYPE_NAME)
      FLOAT(FLOAT_TYPE_NAME, Float.class),
      @JsonProperty(STRING_TYPE_NAME)
      STRING(STRING_TYPE_NAME, String.class);

      private static final Map<String, PropertyType> valuesMap = Arrays.stream(values()).collect(Collectors.toMap(
         propertyType -> propertyType.typeString, propertyType -> propertyType
      ));

      @Include
      private final String                        typeString;
      @Getter
      private final Class<? extends Serializable> typeClass;

      @JsonCreator
      public static PropertyType fromString(final String theString) {
         return valuesMap.get(theString);
      }
   }

   @JsonProperty(BOOLEAN_TYPE_NAME)
   private Boolean booleanValue;
   @JsonProperty(BYTE_TYPE_NAME)
   private Byte byteValue;
   @JsonProperty(DOUBLE_TYPE_NAME)
   private Double doubleValue;
   @JsonProperty(FLOAT_TYPE_NAME)
   private Float floatValue;
   @JsonProperty(INTEGER_TYPE_NAME)
   private Integer integerValue;
   @JsonProperty(LONG_TYPE_NAME)
   private Long longValue;
   @JsonProperty(SHORT_TYPE_NAME)
   private Short shortValue;
   @JsonProperty(STRING_TYPE_NAME)
   private String stringValue;
   @JsonProperty
   private PropertyType propertyType;

   /**
    * Constructor used mainly for testing.  Assigns value to the appropriate field by inspecting its type.
    * @param theValue the value to be stored in this object
    * @param <T> the type of the value
    */
   public <T> PropertyValue(@NotNull final T theValue) {
      if (theValue instanceof Boolean) {
         this.booleanValue = (Boolean) theValue;
         this.propertyType = BOOLEAN;
      }
      else if (theValue instanceof Byte) {
         this.byteValue = (Byte) theValue;
         this.propertyType = BYTE;
      }
      else if (theValue instanceof Double) {
         this.doubleValue = (Double) theValue;
         this.propertyType = DOUBLE;
      }
      else if (theValue instanceof Float) {
         this.floatValue = (Float) theValue;
         this.propertyType = FLOAT;
      }
      else if (theValue instanceof Integer) {
         this.integerValue = (Integer) theValue;
         this.propertyType = INTEGER;
      }
      else if (theValue instanceof Long) {
         this.longValue = (Long) theValue;
         this.propertyType = LONG;
      }
      else if (theValue instanceof Short) {
         this.shortValue = (Short) theValue;
         this.propertyType = SHORT;
      }
      else if (theValue instanceof String) {
         this.stringValue = (String) theValue;
         this.propertyType = PropertyType.STRING;
      }
   }

   public PropertyValue(final boolean theValue) {
      this(Boolean.valueOf(theValue));
   }

   public PropertyValue(final byte theValue) {
      this(Byte.valueOf(theValue));
   }

   public PropertyValue(final int theValue) {
      this(Integer.valueOf(theValue));
   }

   public PropertyValue(final double theValue) {
      this(Double.valueOf(theValue));
   }

   public PropertyValue(final float theValue) {
      this(Float.valueOf(theValue));
   }

   public PropertyValue(final long theValue) {
      this(Long.valueOf(theValue));
   }

   public PropertyValue(final short theValue) {
      this(Short.valueOf(theValue));
   }

   /**
    * Returns the value represented by this instance.  Value is determined by {@link #propertyType}.
    * @return the value according to property type
    */
   public Object value() {
      if (this.propertyType != null) {
         switch (this.propertyType) {
            case STRING:
               return this.stringValue;
            case INTEGER:
               return this.integerValue;
            case BOOLEAN:
               return this.booleanValue;
            case DOUBLE:
               return this.doubleValue;
            case BYTE:
               return this.byteValue;
            case FLOAT:
               return this.floatValue;
            case LONG:
               return this.longValue;
            case SHORT:
               return this.shortValue;
            default:
               return null;
         }
      }
      return null;
   }

   /**
    * Returns the string representation of the first of the values that is non-null.
    * @return string representation of the value of this property; null if {@link #value()} returns null
    */
   @Override
   @SuppressFBWarnings("NP_TOSTRING_COULD_RETURN_NULL")
   public String toString() {
      return this.value() == null ? null : this.value().toString();
   }
}