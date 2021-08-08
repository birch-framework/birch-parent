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
package org.birchframework.framework.dto;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Keivan Khalichi
 */
@XmlRootElement(name = "test-dto")
@XmlAccessorType(XmlAccessType.FIELD)
public class TestDTO implements Serializable {

   @XmlAttribute
   private int id;

   @XmlElement
   private String description;

   private TestDTO() {
   }

   public TestDTO(final int theId, final String theDescription) {
      this();
      id = theId;
      description = theDescription;
   }

   /**
    * Getter for {@link #id}
    * @returns Value of {@link #id}
    */
   public int getId() {
      return id;
   }

   /**
    * Getter for {@link #description}
    * @returns Value of {@link #description}
    */
   public String getDescription() {
      return description;
   }
}