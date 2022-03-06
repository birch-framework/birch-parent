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
package org.birchfw.test.dto;

import org.birchframework.dto.Component;
import org.birchframework.dto.ErrorCode;
import org.birchframework.dto.ErrorResponse;

/**
 * Test implementation of {@link ErrorCode}.
 * @author Keivan Khalichi
 */
public enum TestErrorCode implements ErrorCode<TestErrorCode> {
   T10000;

   @Override
   public int getCode() {
      return 0;
   }

   @Override
   public Component getComponent() {
      return null;
   }

   @Override
   public String getDescription() {
      return null;
   }

   @Override
   public ErrorResponse<TestErrorCode> errorResponse() {
      return null;
   }
}