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
package org.birchframework.framework.function;

import java.util.function.Predicate;

/**
 * A predicate that always returns true.  This is the default predicate for {@code BirchProperties.BaseProperties#filterPredicate}.
 * @author Keivan Khalichi
 */
public class AlwaysTruePredicate implements Predicate<Object> {

   @Override
   public boolean test(final Object theObject) {
      return true;
   }
}