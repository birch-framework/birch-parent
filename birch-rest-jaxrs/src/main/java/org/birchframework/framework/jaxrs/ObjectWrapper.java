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
package org.birchframework.framework.jaxrs;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

/**
 * Utility class to wrap any object for the purpose JAXB binding when transporting over REST
 * @author Keivan Khalichi
 */
@XmlRootElement
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@SuppressWarnings("unused")
public class ObjectWrapper<T extends Serializable> implements Serializable {

   @XmlElement(name = "value")
   private T value;
}