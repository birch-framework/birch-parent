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
package org.birchframework.framework.cxf;

import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;

import static javax.ws.rs.core.HttpHeaders.CONTENT_LANGUAGE;
import static org.birchframework.framework.cxf.SpanHeadersContainerBean.*;

/**
 * CXF filter that pulls {@link SpanHeadersContainerBean.SpanHeadersContainer} from the request on the service side and makes it available to the thread.
 * @author Keivan Khalichi
 */
@Provider
@Slf4j
public class SpanningContainerRequestFilter implements ContainerRequestFilter {

   private static final Pattern localePattern = Pattern.compile(".*([a-z]{2}[-_][A-Z]{2}).*");

   @Resource
   private ApplicationContext       context;
   @SuppressWarnings("InstanceVariableMayNotBeInitialized")
   private SpanHeadersContainerBean spanHeadersContainer;
   @Value("${spring.mvc.locale:en_US}")
   private String                   defaultLocale;

   @PostConstruct
   void init() {
      log.info("Default locale: {}", this.defaultLocale);
      try {
         this.spanHeadersContainer = this.context.getBean(SpanHeadersContainerBean.class);
      }
      catch (NoSuchBeanDefinitionException e) {
         log.warn("Span headers container bean was not found");
      }
   }

   @Override
   public void filter(final ContainerRequestContext theRequestContext) {
      if (this.spanHeadersContainer != null && !this.spanHeadersContainer.hasData()) {
         final var aHeaders = theRequestContext.getHeaders();

         final String aLocale;
         if (aHeaders.containsKey(LOCALE_HEADER)) {
            final var aLocaleHeaderValues = aHeaders.get(LOCALE_HEADER).stream().filter(StringUtils::isNotBlank).collect(Collectors.joining("|"));
            log.debug("Found {}: {}", LOCALE_HEADER, aLocaleHeaderValues);
            aLocale = this.parseLocale(aLocaleHeaderValues);
         }
         else {
            final var aContentLanguage = theRequestContext.getLanguage();
            log.debug("Did not find {}; attempting using {}: {}", LOCALE_HEADER, CONTENT_LANGUAGE, aContentLanguage);
            final var aRequestLanguage = ObjectUtils.defaultIfNull(aContentLanguage, LocaleContextHolder.getLocale());
            aLocale = this.parseLocale(aRequestLanguage.toString());
         }
         log.debug("Locale context is being set to: {}", aLocale);
         LocaleContextHolder.setLocale(LocaleUtils.toLocale(aLocale));

         final String aCorrelationID = aHeaders.containsKey(CORRELATION_ID_HEADER) ? aHeaders.getFirst(CORRELATION_ID_HEADER) : UUID.randomUUID().toString();
         log.debug("Correlation ID: {}", aCorrelationID);

         this.spanHeadersContainer.setLocale(aLocale);
         this.spanHeadersContainer.setCorrelationID(aCorrelationID);
         MDC.put(CORRELATION_ID_HEADER, aCorrelationID);
      }
   }

   private String parseLocale(final String theHeaderValue) {
      if (StringUtils.equals("null", theHeaderValue)) {
         return this.defaultLocale;
      }
      final var aMatcher = localePattern.matcher(theHeaderValue);
      return aMatcher.matches()
           ? aMatcher.group(1).replace("-", "_")
           : this.defaultLocale;
   }
}