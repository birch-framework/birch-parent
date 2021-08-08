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

package com.example;

import org.birchframework.dto.BirchErrorCode;
import org.birchframework.dto.ErrorCode;
import org.birchframework.framework.beans.Beans;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Explicitly tests {@link Beans#findImplementation(Class, Class[])}.
 * @author Keivan Khalichi
 */
public class BeansFindImplementationTest {

   @Test
   public void testFindImplementation() {
      Beans.findImplementation(ErrorCode.class, BirchErrorCode.class).ifPresentOrElse(
         c -> assertThat(TestErrorCode.class.isAssignableFrom(c)).isTrue(),
         () -> fail("Expected the class {}", TestErrorCode.class.getName())
      );
   }
}