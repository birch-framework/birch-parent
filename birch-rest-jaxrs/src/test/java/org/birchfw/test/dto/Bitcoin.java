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

package org.birchfw.test.dto;

import java.io.Serializable;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import lombok.Data;

import static javax.xml.bind.annotation.XmlAccessType.PROPERTY;

@XmlRootElement
@XmlType(propOrder = {
   "time",
   "disclaimer",
   "chartName",
   "bpi"
})
@XmlAccessorType(PROPERTY)
@Data
public class Bitcoin implements Serializable {
   private Time                  time;
   private String                disclaimer;
   private String                chartName;
   private Map<String, Currency> bpi;
}