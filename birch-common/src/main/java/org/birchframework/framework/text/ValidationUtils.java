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

package org.birchframework.framework.text;

import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Various RegEx based validations of strings.
 * @author Keivan Khalichi
 */
public class ValidationUtils {

   private static final Pattern URL_PATTERN = Pattern.compile("^((((https?|ftps?|gopher|telnet|nntp)://)|(mailto:|news:))(%[0-9A-Fa-f]{2}|[-()_.!~*';/?:@&=+$,A-Za-z0-9])+)([).!';/?:,][[:blank:]])?$");

   public static boolean validURL(final String theURL) {
      return !StringUtils.isBlank(theURL) && URL_PATTERN.matcher(theURL).matches();
   }
}