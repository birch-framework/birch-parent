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
package org.birchframework.dto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.birchframework.framework.beans.Beans;

/**
 * Interface that can only be applied to {@link Enum}s, allowing for custom definition of keys whose values can span across microservices boundaries.<p/>
 * <emphasis>NOTE: extactly 1 class must be extended from this interface in each microservice's classloader</emphasis><p/>
 * @param <E> marker type parameter that ensures this interface is only implemented by an {@link Enum} class
 * @author Keivan Khalichi
 */
@SuppressWarnings({"rawtypes", "unused"})
public interface Spannable<E extends Enum<E>> {
   /** Discovered implementation in the classpath */
   Class<? extends Spannable> impl = Beans.findImplementation(Spannable.class).filter(Enum.class::isAssignableFrom).orElse(null);
   /** String spannable keys */
   List<String>               keys = keys();

   private static List<String> keys() {
      if (impl == null) {
         return Collections.emptyList();
      }
      final var anEnumValues = impl.getEnumConstants();
      return Stream.of(anEnumValues).map(Spannable::name).collect(Collectors.toUnmodifiableList());
   }

   String name();
}