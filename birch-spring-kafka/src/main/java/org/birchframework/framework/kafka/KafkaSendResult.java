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

import org.springframework.kafka.support.SendResult;

/**
 * @author Keivan Khalichi
 */
@SuppressWarnings({"InstanceVariableMayNotBeInitialized", "unused"})
public class KafkaSendResult<K,V> {

   SendResult<K,V> result;
   boolean         hasError;
   Throwable       exception;

   KafkaSendResult() {
   }

   public KafkaSendResult(final SendResult<K,V> theResult) {
      this.result = theResult;
   }

   public KafkaSendResult(final SendResult<K,V> theResult, final boolean theHasError, final Throwable theException) {
      this(theResult);
      this.hasError  = theHasError;
      this.exception = theException;
   }

   /**
    * Getter for {@link #result}
    * @returns Value of {@link #result}
    */
   public SendResult<K, V> getResult() {
      return result;
   }

   /**
    * Getter for {@link #hasError}
    * @returns Value of {@link #hasError}
    */
   public boolean getHasError() {
      return this.hasError;
   }

   /**
    * Getter for {@link #exception}
    * @returns Value of {@link #exception}
    */
   public Throwable getException() {
      return this.exception;
   }
}