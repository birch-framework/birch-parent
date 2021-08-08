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
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static javax.xml.bind.annotation.XmlAccessType.PROPERTY;
import static org.apache.commons.lang3.builder.ToStringStyle.*;

/**
 * Interface to be implemented by all enumerations that define error codes.
 * @param <E> requires implementations be {@link Enum}s that implement this interface
 * @author Keivan Khalichi
 */
@XmlAccessorType(PROPERTY)
@XmlType(propOrder = {
        "code",
        "component",
        "description"
})
@XmlEnum
public interface ErrorCode<E extends Enum<E> & ErrorCode<E>> extends Serializable {

   int getCode();

   Component getComponent();

   String getDescription();

   /**
    * Create an {@link ErrorResponse} instance from this error code.
    * @return error response object populated with data from this error code
    */
   ErrorResponse<E> errorResponse();

   /**
    * Convenience method that returns this instance as an {@link Enum} object
    * @return this instance casted as an enum
    */
   @SuppressWarnings("unchecked")
   default Enum<E> asEnum() {
      return (Enum<E>) this;
   }

   /**
    * Takes the place of {@code toString}, which cannot be used as it has other use cases within the Birch Framework.
    * @return string representation of implementations
    */
   default String asString() {
      return new ToStringBuilder(this, SHORT_PREFIX_STYLE).append("errorCode", this.getCode())
                                                          .append("component", this.getComponent())
                                                          .append("description", this.getDescription())
                                                          .toString();
   }

   /**
    * Marshal's this enumeration into a JSON string.
    * @return the JSON string of this object
    */
   default String toJSON() {
      return new ToStringBuilder(this, JSON_STYLE).append("errorCode", this.getCode())
                                                  .append("component", this.getComponent())
                                                  .append("description", this.getDescription())
                                                  .build();

   }
}