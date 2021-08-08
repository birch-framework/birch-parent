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
package org.birchframework.configuration;

import java.io.Serializable;

/**
 * Defines an interface for user-provided enumeration of identity provider classifications.  Defined so that
 * {@link BirchProperties.IdPRealm#getType()} can be defined at a framework level.  Users must implement this interface on an Enum class
 * as follows:
 * <pre>
 * public enum IdentityProviderType implements IdPClassifiable{@code <IdentityProviderType>} {
 *    TYPE_1,
 *    TYPE_2,
 *    ...
 * }
 * </pre>
 * @author Keivan Khalichi
 */
public interface IdPClassifiable<T extends Enum<T> & IdPClassifiable<T>> extends Serializable {

   @SuppressWarnings("unchecked")
   default T asEnum() {
      return (T) this;
   }

   String name();
}