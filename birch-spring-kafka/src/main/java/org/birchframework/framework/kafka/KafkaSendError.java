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
package org.birchframework.framework.kafka;

import org.birchframework.dto.BirchErrorCode;
import org.birchframework.dto.ErrorResponse;
import org.birchframework.framework.exception.BaseException;

/**
 * Kafka send error.
 * @author Keivan Khalichi
 */
public class KafkaSendError extends BaseException {

   public KafkaSendError(final ErrorResponse theErrorResponse, final Throwable theCause) {
      super(theErrorResponse, theCause);
   }

   public KafkaSendError(final ErrorResponse theErrorResponse) {
      super(theErrorResponse);
   }

   public KafkaSendError(final BirchErrorCode theErrorCode) {
      super(theErrorCode);
   }

   public KafkaSendError(final BirchErrorCode theErrorCode, final Throwable theCause) {
      super(theErrorCode, theCause);
   }
}