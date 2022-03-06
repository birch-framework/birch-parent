/*===============================================================
 = Copyright (c) 2022 Birch Framework
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

import com.example.TestSpannable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link org.birchframework.dto.Spannable}.
 * @author Keivan Khalihci
 */
class SpannableTest {

   @Test
   public void testKeys() {
      assertThat(Spannable.keys).containsExactly(TestSpannable.correlationID.name(), TestSpannable.locale.name());
   }
}