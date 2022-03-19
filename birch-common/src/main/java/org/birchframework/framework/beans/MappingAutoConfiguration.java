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
package org.birchframework.framework.beans;

import javax.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

/**
 * For all model class POJOs annotated with {@link MappingModel}, registers a mapping using Orika.
 * @see ma.glasnost.orika.MapperFactory
 * @author Keivan Khalichi
 */
@Configuration(proxyBeanMethods = false)
@EnableAutoConfiguration
@SuppressWarnings("SpringFacetCodeInspection")
public class MappingAutoConfiguration {

   @PostConstruct
   protected void init() {
      final var anAnnotatedClasses = Beans.classesAnnotatedWith(MappingModel.class);
      if (!CollectionUtils.isEmpty(anAnnotatedClasses)) {
         anAnnotatedClasses.forEach(clazz -> {
            final var aMappingModel = clazz.getAnnotation(MappingModel.class);
            Beans.registerMapping(clazz, aMappingModel.mapNulls(), aMappingModel.exclude());
         });
      }
   }
}