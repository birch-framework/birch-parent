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
package org.birchframework.framework.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.SimpleThreadScope;

/**
 * Auto-configuration that registers custom Spring scopes.
 * @author Keivan Khalichi
 */
@Configuration(proxyBeanMethods = false)
@EnableAutoConfiguration
@RefreshScope
public class CustomScopesAutoConfiguration implements BeanFactoryPostProcessor {

   public static final String THREAD_SCOPE = "thread";

   @Override
   public void postProcessBeanFactory(final ConfigurableListableBeanFactory theFactory) throws BeansException {
      theFactory.registerScope(THREAD_SCOPE, new SimpleThreadScope());
   }
}