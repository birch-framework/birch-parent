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

import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

import static org.birchframework.dto.BirchErrorCode.*;

/**
 * (De)serializer of JMS messages.  The payload must adhere to a particular format.
 * @author Keivan Khalichi
 */
public class JMSMessageMarshaller {

   private static final Pattern fullMessagePattern = Pattern.compile(".*\\$Properties:(.*)\\$TextBody:(.*)", Pattern.DOTALL);
   private static final Pattern propertyPattern    = Pattern.compile("^(.*)=(.*):(.*)$", Pattern.DOTALL);

   @SuppressWarnings("AutoBoxing")
   public static JMSMessage deserialize(final String theJMSMessageString) {
      if (StringUtils.isBlank(theJMSMessageString)) {
         throw new JMSMessageMarshallerException(B12100);
      }
      final var aFullMessageMatcher = fullMessagePattern.matcher(theJMSMessageString);
      final var aProperties = new Properties();
      final String aTextBody;
      if (aFullMessageMatcher.matches()) {
         final var aPropertiesString = aFullMessageMatcher.group(1).strip();
         aTextBody = aFullMessageMatcher.group(2).strip();
         aPropertiesString.lines().map(String::strip).forEach(line -> {
            final var aPropertyMatcher = propertyPattern.matcher(line);
            if (aPropertyMatcher.matches()) {
               final var aPropertyKey = aPropertyMatcher.group(1).strip();
               final var aPropertyType = aPropertyMatcher.group(2).strip();
               final var aPropertyValue = aPropertyMatcher.group(3).strip();
               final Object aValue;
               switch (aPropertyType) {
                  case "String":
                     aValue = aPropertyValue;
                     break;
                  case "Boolean":
                     aValue = Boolean.valueOf(aPropertyValue);
                     break;
                  case "Long":
                     aValue = Long.valueOf(aPropertyValue);
                     break;
                  case "Integer":
                     aValue = Integer.valueOf(aPropertyValue);
                     break;
                  case "Short":
                     aValue = Short.valueOf(aPropertyValue);
                     break;
                  case "Character":
                     aValue = aPropertyValue.charAt(0);
                     break;
                  default:
                     aValue = null;
               }
               if (aValue != null) {
                  aProperties.put(aPropertyKey, aValue);
               }
            }
         });
      }
      else {
         throw new JMSMessageMarshallerException(B12110);
      }
      return new JMSMessage(aProperties, aTextBody);
   }
}