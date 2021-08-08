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

import java.util.function.Consumer;
import org.apache.camel.Exchange;

/**
 * Test after-receive consumer.
 * @author Keivan Khalichi
 */
public class TestAfterReceive implements Consumer<Exchange> {

   @Override
   public void accept(final Exchange theExchange) {
      if (((int) theExchange.getIn().getHeader("index")) == 8) {
         throw new RuntimeException("Testing error handling");
      }
   }
}