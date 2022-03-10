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

import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

/**
 * Aspect to ensure {@link SpanHeadersContainerBean#unload()} is called at the end of all JAX-RS service methods.
 * @author Keivan Khalichi
 */
@Aspect
@RequiredArgsConstructor
@Slf4j
class ServiceAspect {

   private final SpanHeadersContainerBean spanHeadersContainerBean;

   @PostConstruct
   void init() {
      log.info("This @Aspect has been configured");
   }

   @After(value = "within(@org.springframework.stereotype.Service *) && execution(javax.ws.rs.core.Response *.*(..))")
   public void afterServiceMethodExecution() {
      this.spanHeadersContainerBean.unload();
   }
}
