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

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.springframework.util.ErrorHandler;

/**
 * Default error handler for JMS listeners.
 * @author Keivan Khalichi
 */
public class DefaultJMSListenerContainerErrorHandler implements ErrorHandler {

   private final Logger log;

   public DefaultJMSListenerContainerErrorHandler(final Logger theLogger) {
      this.log = theLogger;
   }

   @Override
   public void handleError(final Throwable theError) {
      this.log.error("Exception {}; root cause: {}", theError.getClass().getSimpleName(), Throwables.getRootCause(theError).getMessage());
   }
}