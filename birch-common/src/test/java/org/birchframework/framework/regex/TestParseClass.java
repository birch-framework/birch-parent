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

package org.birchframework.framework.regex;

import java.io.Serializable;
import lombok.Getter;
import org.checkerframework.checker.regex.qual.Regex;

/**
 * Test parsable object class.
 * @author Keivan Khalichi
 */
@Regex
@RegexBinding("Age:\\s+(\\d+)\\s+\\|\\s+Last Name:\\s+(\\w*)\\s+\\|\\s+First Name:(\\s+)(\\w*)\\s+\\|\\s+Alive:\\s+(y|n|Y|N|t|f|T|F)\\s+\\|\\s+(.*)")
@Getter
@SuppressWarnings({"unused", "InstanceVariableMayNotBeInitialized"})
public class TestParseClass implements Serializable {

   @CaptureGroup(1)
   private int age;

   @CaptureGroup(2)
   private String lastName;

   @CaptureGroup(5)
   private boolean alive;

   @CaptureGroup(4)
   private String firstName;

   @CaptureGroup(6)
   private Type type;

   public enum Type {
      ROCK_STAR,
      CHARACTER
   }
}