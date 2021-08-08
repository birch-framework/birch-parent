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
package org.birchframework.framework.stub;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.reflect.ConstructorUtils;

import static org.birchframework.dto.BirchErrorCode.B10120;

/**
 * Stub utilities.  Provides methods to stub instances of classes.
 * @author Keivan Khalichi
 */
public class Stub {

   private Stub() {
   }

   /**
    * Functions as a mapper.  The target class <emphasis>is not</emphasis> instantiated.  Provides flexibility by allowing the caller to provide
    * instantiation logic within the provided function, especially useful (and required) when the target class does not have a no-args constructor.
    * @param theTarget the target type
    * @param theMapper mapper to be used in a lambda expression
    * @param <T> the type of the stub
    * @return stubbed instance of {@literal Class<T>}
    */
   public static <T> T map(@NotNull final Class<T> theTarget, @NotNull final Function<Class<T>, T> theMapper) {
      return theMapper.apply(theTarget);
   }

   /**
    * Functions as a consumer.  The target class <emphasis>is</emphasis> instantiated via reflections, utilizing its no-args constructor.
    * @param theTarget the target type
    * @param theAction the action to be performed within a lambda expression
    * @param <T> the type of the stub
    * @return stubbed instance of {@literal Class<T>}
    */
   public static <T> T of(@NotNull final Class<T> theTarget, @NotNull final Consumer<T> theAction) {
      try {
         final var aReturnValue = (T) ConstructorUtils.invokeConstructor(theTarget);
         theAction.accept(aReturnValue);
         return aReturnValue;
      }
      catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
         throw new StubbingError(B10120, e);
      }
   }
}