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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Collection of all the known {@link org.slf4j.MDC} keys used by Birch Framework.
 * @author Keivan Khalichi
 */
public enum ContextMapKeys {
   correlationID;

   public static final String CORRELATION_ID = correlationID.name();

   public static final List<String> keys = Collections.unmodifiableList(Arrays.stream(values()).map(Enum::name).collect(Collectors.toList()));
}