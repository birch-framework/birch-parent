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

package org.birchframework.bridge;

import org.birchframework.dto.ErrorCode;
import org.birchframework.framework.exception.BaseRuntimeException;

/**
 * Exception used for source and target message header mapping errors.
 * @author Keivan Khalichi
 */
@SuppressWarnings("unused")
public class MappingException extends BaseRuntimeException {

   public MappingException(final ErrorCode<?> theErrorCode) {
      super(theErrorCode);
   }

   public MappingException(final ErrorCode<?> theErrorCode, final Throwable theCause) {
      super(theErrorCode, theCause);
   }
}
