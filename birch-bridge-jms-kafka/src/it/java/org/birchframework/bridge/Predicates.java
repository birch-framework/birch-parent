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

import java.util.function.Predicate;
import lombok.NoArgsConstructor;
import org.apache.camel.Exchange;

import static lombok.AccessLevel.PRIVATE;

/**
 * Test {@link Predicate}s.
 * @author Keivan Khalichi
 */
@NoArgsConstructor(access = PRIVATE)
@SuppressWarnings("AutoUnboxing")
public final class Predicates {

   public static class Odds implements Predicate<Exchange> {
      @Override
      public boolean test(final Exchange theExchange) {
         final int anIndex = (int) theExchange.getIn().getHeader("index");
         return anIndex % 2 == 1;
      }
   }

   public static class Evens implements Predicate<Exchange> {
      @Override
      public boolean test(final Exchange theExchange) {
         final int anIndex = (int) theExchange.getIn().getHeader("index");
         return anIndex % 2 == 0;
      }
   }
}