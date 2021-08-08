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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.checkerframework.checker.regex.qual.Regex;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotates a class defining a regular expression to be parsed via the {@link Parser} utility.  Fields of such classes must be annotated with
 * {@link CaptureGroup} in order for instances of these classes have such fields populated by the parser.
 * @see CaptureGroup
 * @see Parser
 * @author Keivan Khalichi
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface RegexBinding {
   @Regex
   String value();
}