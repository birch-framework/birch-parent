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
package org.birchframework.framework.cxf;

import org.birchframework.configuration.BirchProperties;
import org.birchframework.framework.spring.SpringContext;
import lombok.Getter;
import lombok.Setter;
import org.apache.cxf.annotations.Provider;
import org.apache.cxf.jaxrs.openapi.OpenApiFeature;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import static org.apache.cxf.annotations.Provider.Scope.Server;
import static org.apache.cxf.annotations.Provider.Type.Feature;

/**
 * Custom CXF feature used to initialize OpenAPI.
 * @author Keivan Khalichi
 */
@Provider(value = Feature, scope = Server)
@RefreshScope
public class CustomOpenAPIFeature extends OpenApiFeature {

   @Getter
   @Setter
   private String basePath;

   @SuppressWarnings("AutoBoxing")
   public CustomOpenAPIFeature() {
      super();
      final var aProperties = SpringContext.getBean(BirchProperties.class);
      if (aProperties != null) {
         final var aFeature = aProperties.getOpenapi().getFeature();
         this.setScanKnownConfigLocations(false);
         this.setVersion(aFeature.getVersion());
         this.setTitle(aFeature.getTitle());
         this.setDescription(aFeature.getDescription());
         this.setContactName(aFeature.getContactUrl());
         this.setContactUrl(aFeature.getContactUrl());
         this.setLicense(aFeature.getLicense());
         this.setLicenseUrl(aFeature.getLicenseUrl());
         this.setPrettyPrint(aFeature.isPrettyPrint());
         this.setReadAllResources(aFeature.isReadAllResources());
         this.setSupportSwaggerUi(aFeature.isSupportSwaggerUi());
         this.setBasePath(aFeature.getBasePath());
         this.enabled = true;
      }
   }
}