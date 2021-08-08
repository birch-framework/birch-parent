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
package org.birchframework.framework.logging.logback;

import ch.qos.logback.classic.PatternLayout;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Replaces {@link ch.qos.logback.classic.pattern.DateConverter} with {@link DateConverter} within the Logback global converters configuration.
 * @author Keivan Khalichi
 */
@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class PatternLayoutEncoder extends ch.qos.logback.classic.encoder.PatternLayoutEncoder {

   static {
      PatternLayout.defaultConverterMap.replace("d", DateConverter.class.getName());
      PatternLayout.defaultConverterMap.replace("date", DateConverter.class.getName());
   }
}