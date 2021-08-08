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

package org.birchframework.framework.cli;

import org.birchframework.dto.ErrorCode;
import org.birchframework.framework.exception.BaseException;

/**
 * Declared exception for command-line application errors.
 * @author Keivan Khalichi
 */
public class CommandLineApplicationException extends BaseException {

   public CommandLineApplicationException(final ErrorCode<?> theErrorCode) {
      super(theErrorCode);
   }

   public CommandLineApplicationException(final ErrorCode<?> theErrorCode, final Throwable theCause) {
      super(theErrorCode, theCause);
   }
}