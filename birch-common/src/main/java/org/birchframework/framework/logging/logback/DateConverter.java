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
package org.birchframework.framework.logging.logback;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import static ch.qos.logback.core.CoreConstants.*;

/**
 * An alternative to {@link ch.qos.logback.classic.pattern.DateConverter}, which internally uses {@link ch.qos.logback.core.util.CachingDateFormatter} that
 * can cause thread locks resulting in performance degradation.  This class internally uses {@link DateTimeFormatter} instead.
 * @author Keivan Khalichi
 */
public class DateConverter extends ClassicConverter {

   private DateTimeFormatter dateTimeFormatter = null;
   @Getter
   private TimeZone          timeZone          = null;
   private ZoneId            timeZoneID        = null;

   @Override
   public void start() {
       var aPattern = this.getFirstOption();
       if (StringUtils.isBlank(aPattern)) {
          aPattern = ISO8601_PATTERN;
       }

       if (aPattern.equals(ISO8601_STR)) {
           aPattern = ISO8601_PATTERN;
       }

      this.dateTimeFormatter = DateTimeFormatter.ofPattern(aPattern);

      final var anOptions = this.getOptionList();
      this.timeZone   = anOptions == null || anOptions.size() <= 1 ? TimeZone.getDefault() : TimeZone.getTimeZone(anOptions.get(1));
      this.timeZoneID = this.timeZone.toZoneId();
      super.start();
   }

   @Override
   public String convert(final ILoggingEvent theEvent) {
      final var aTimeStamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(theEvent.getTimeStamp()), this.timeZoneID);
      return this.dateTimeFormatter.format(aTimeStamp);
   }
}