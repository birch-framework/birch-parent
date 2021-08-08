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
package org.birchframework.dto;

import java.io.Serializable;
import java.lang.reflect.Type;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import static javax.xml.bind.annotation.XmlAccessType.FIELD;
import static lombok.AccessLevel.PRIVATE;

/**
 * Data structure used for service and messaging error responses.  Responsible for returning error response when the request is not valid.
 * @author Keivan Khalichi
 */
@XmlRootElement
@XmlAccessorType(FIELD)
@XmlType(propOrder = {
        "code",
        "message"
})
@NoArgsConstructor(access = PRIVATE)
@SuppressWarnings("InstanceVariableMayNotBeInitialized")
public class ErrorResponse<E extends Enum<E> & ErrorCode<E>> implements Serializable, Type {

   /** Error code */
   @Getter
   private E code;

   /** String property for error message */
   @Getter
   private String message;

   /**
    * Constructor that defaults to {@link BirchErrorCode#getDescription()} for the {@link #message}.
    * @param theCode error code
    */
   public ErrorResponse(@NonNull final E theCode) {
      super();
      this.code = theCode;
      this.message = theCode.getDescription();
   }

   /**
    * Constructor with code and message properties
    * @param theCode the error code
    * @param message the customized message; if null defaults to code description
    */
   public ErrorResponse(@NonNull final E theCode, final String message) {
      super();
      this.code = theCode;
      this.message = Strings.isNullOrEmpty(message) ? theCode.getDescription() : message;
   }

   /**
    * getter method for component property
    * @return
    */
   public Component getComponent() {
      return this.code.getComponent();
   }

   /**
    * Constructs a formatted string representation of the error.
    * @return formatted string in the format [component].[code]: [message]
    */
   @Override
   public String toString() {
      return String.format("%s.%s: %s", this.getComponent(), this.getCode(), this.getMessage());
   }
}