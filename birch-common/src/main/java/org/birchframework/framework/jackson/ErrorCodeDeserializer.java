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

package org.birchframework.framework.jackson;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.birchframework.dto.ErrorCode;

/**
 * Jackson deserializer for {@link ErrorCode} subclasses.  This class needs to be initialized by the implementation of ErrorCode.  Only one
 * implementation can be registered with this class per class loader.
 * @author Keivan Khalichi
 */
public class ErrorCodeDeserializer<E extends Enum<E> & ErrorCode<E>> extends StdDeserializer<E> {

   private Class<E> valueClass;

   protected ErrorCodeDeserializer(final Class<E> theValueClass) {
      super(theValueClass);
      this.valueClass = theValueClass;
   }

   public ErrorCodeDeserializer(final JavaType theValueType) {
      super(theValueType);
      this.valueClass = (theValueType == null) ? null : (Class<E>) theValueType.getRawClass();
   }

   @Override
   public E deserialize(final JsonParser theParser, final DeserializationContext theContext) throws IOException {
      if (valueClass != null) {
         var aValueString = theParser.readValueAs(String.class);
         if (StringUtils.isEmpty(aValueString)) {
            return null;
         }
         else {
            aValueString = aValueString.trim().replace("\"", "");
            try {
               return (E) MethodUtils.invokeStaticMethod(this.valueClass, "valueOf", aValueString);
            }
            catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
               return null;
            }
         }
      }
      else {
         return null;
      }
   }
}