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

package org.birchframework.framework.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import javax.validation.constraints.NotNull;

import static java.util.Calendar.DAY_OF_MONTH;
import static org.apache.commons.lang3.time.DateUtils.*;

/**
 * Variety of date utilities, including parsing dates.  Depends on {@link org.apache.commons.lang3.time.DateUtils}.
 * @author Keivan Khalichi
 */
@SuppressWarnings({"VariableArgumentMethod", "unused"})
public class DateUtils {

   @SuppressWarnings("PMD.UnnecessaryFullyQualifiedName")
   public static Optional<Date> parseDate(String theDateString) {
      final Date aDate;
      try {
         aDate = org.apache.commons.lang3.time.DateUtils.parseDate(theDateString);
      }
      catch (ParseException ignore) {
         return Optional.empty();
      }
      return Optional.of(aDate);
   }

   @SuppressWarnings("PMD.UnnecessaryFullyQualifiedName")
   public static Optional<Date> parseDate(String theDateString, String... theParsePatterns) {
      final Date aDate;
      try {
         aDate = org.apache.commons.lang3.time.DateUtils.parseDate(theDateString, theParsePatterns);
      }
      catch (ParseException ignore) {
         return Optional.empty();
      }
      return Optional.of(aDate);
   }

   public static Optional<String> formatDate(Date theDate, String theFormat) {
      try {
         final var aDateFormat = new SimpleDateFormat(theFormat);
         return Optional.of(aDateFormat.format(theDate));
      }
      catch (Exception e) {
         return Optional.empty();
      }
   }

   public static Optional<Date> startOfDay(@NotNull Date theDate) {
      if (theDate == null) {
         return Optional.empty();
      }
      return Optional.of(truncate(theDate, DAY_OF_MONTH));
   }

   public static Optional<Date> endOfDay(@NotNull Date theDate) {
      if (theDate == null) {
         return Optional.empty();
      }
      return startOfDay(theDate).map(sod -> addSeconds(addDays(sod, 1), -1));
   }
}