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
package org.birchframework.framework.exception;

import org.birchframework.dto.ErrorCode;
import org.birchframework.dto.ErrorResponse;

/**
 * Base exception that extends {@link RuntimeException}, thus not required to be declared in method signatures when thrown.
 * @author Keivan Khalichi
 */
@SuppressWarnings("WeakerAccess")
public class BaseRuntimeException extends RuntimeException {

   /** Instance of error response */
   private final ErrorResponse<? extends Enum<?>> errorResponse;

   @SuppressWarnings("CPD-START")
   public BaseRuntimeException(final ErrorResponse<? extends Enum<?>> theErrorResponse, final Throwable theCause) {
      super(theCause);
      this.errorResponse = theErrorResponse;
   }

   public BaseRuntimeException(final ErrorResponse<? extends Enum<?>> theErrorResponse) {
      super();
      this.errorResponse = theErrorResponse;
   }

   public BaseRuntimeException(final ErrorCode<? extends Enum<?>> theErrorCode) {
      this(theErrorCode.errorResponse());
   }

   public BaseRuntimeException(final ErrorCode<? extends Enum<?>> theErrorCode, final Throwable theCause) {
      this(theErrorCode.errorResponse(), theCause);
   }

   @SuppressWarnings("CPD-END")
   public ErrorResponse<? extends Enum<?>> getErrorResponse() {
      return this.errorResponse;
   }

   public ErrorCode<? extends Enum<?>> getErrorCode() {
      return this.errorResponse.getCode();
   }

   @Override
   public String toString() {
      return this.getCause() == null ? super.toString() : this.getCause() == this ? super.toString() : this.getCause().toString();
   }

   @Override
   public String getMessage() {
      return this.errorResponse.getMessage();
   }
}