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

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

/**
 * A convenient component that allows access to the Spring context from non-managed classes.
 * @author Keivan Khalichi
 */
@Component
public class SpringContext implements ApplicationContextAware {

   private static GenericApplicationContext context = null;

   /**
    * Getter for {@link #context}
    * @returns value of {@link #context}
    */
   public static ApplicationContext get() {
      return context;
   }

   /**
    * Get a bean from the Spring application context
    * @param theBeanClass the bean's Class
    * @param <T> the bean's type
    * @return the found bean
    */
   public static <T> T getBean(final Class<T> theBeanClass) {
      return context == null ? null : context.getBean(theBeanClass);
   }

   /**
    * Get a bean from the Spring application context
    * @param theBeanName the bean's name
    * @param <T> the bean's type
    * @return the found bean
    */
   @SuppressWarnings("unchecked")
   public static <T> T getBean(final String theBeanName) {
      return context == null ? null : (T) context.getBean(theBeanName);
   }

   @SuppressWarnings("VariableArgumentMethod")
   public static <T> void registerBean(final Class<T> theBeanClass, final Supplier<T> theBeanSupplier, final BeanDefinitionCustomizer... theCustomizers) {
      if (context != null) {
         context.registerBean(theBeanClass, theBeanSupplier, theCustomizers);
      }
   }

   /** {@inheritDoc} */
   @Override
   @SuppressWarnings("NonThreadSafeLazyInitialization")
   public synchronized void setApplicationContext(@Nonnull final ApplicationContext theApplicationContext) throws BeansException {
      if (context == null) {
         context = (GenericApplicationContext) theApplicationContext;
      }
   }
}