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
package org.birchframework.framework.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * Aspect with pointcut defined to capture all project classes and method for logging before and after method execution.
 * @author Keivan Khalichi
 */
@Aspect
@Slf4j
class LoggingAspect {

   @Before("execution(* com..*Impl.*(..))")
   public void logImplsBefore(final JoinPoint thePoint) {
      this.logBefore(thePoint);
   }

   @After("execution(* com..*Impl.*(..))")
   public void logImplsAfter(final JoinPoint thePoint) {
      this.logAfter(thePoint);
   }

   @AfterReturning(pointcut = "execution(* com..*Impl.*(..))", returning = "theResult")
   public void logImplsAfterReturning(final JoinPoint thePoint, final Object theResult) {
      this.logAfterReturning(thePoint, theResult);
   }

   @AfterThrowing(pointcut = "execution(* com..*Impl.*(..))", throwing = "theException")
   public void logImplsAfterThrowing(final JoinPoint thePoint, final Throwable theException) {
      this.logAfterThrowing(thePoint, theException);
   }

   @Before("execution(* com..*Manager.*(..))")
   public void logManagersBefore(final JoinPoint thePoint) {
      this.logBefore(thePoint);
   }

   @After("execution(* com..*Manager.*(..))")
   public void logManagersAfter(final JoinPoint thePoint) {
      this.logAfter(thePoint);
   }

   @AfterReturning(pointcut = "execution(* com..*Manager.*(..))", returning = "theResult")
   public void logManagersAfterReturning(final JoinPoint thePoint, final Object theResult) {
      this.logAfterReturning(thePoint, theResult);
   }

   @AfterThrowing(pointcut = "execution(* com..*Manager.*(..))", throwing = "theException")
   public void logManagersAfterThrowing(final JoinPoint thePoint, final Throwable theException) {
      this.logAfterThrowing(thePoint, theException);
   }

   @Before("execution(* com..*Repository.*(..))")
   public void logRepositoriesBefore(final JoinPoint thePoint) {
      this.logBefore(thePoint);
   }

   @After("execution(* com..*Repository.*(..))")
   public void logRepositoriesAfter(final JoinPoint thePoint) {
      this.logAfter(thePoint);
   }

   @AfterReturning(pointcut = "execution(* com..*Repository.*(..))", returning = "theResult")
   public void logRepositoryAfterReturning(final JoinPoint thePoint, final Object theResult) {
      this.logAfterReturning(thePoint, theResult);
   }

   @AfterThrowing(pointcut = "execution(* com..*Repository*.*(..))", throwing = "theException")
   public void logRepositoriesAfterThrowing(final JoinPoint thePoint, final Throwable theException) {
      this.logAfterThrowing(thePoint, theException);
   }

   @Before("execution(* com..*Consumer.*(..))")
   public void logConsumersBefore(final JoinPoint thePoint) {
      this.logBefore(thePoint);
   }

   @After("execution(* com..*Consumer.*(..))")
   public void logConsumersAfter(final JoinPoint thePoint) {
      this.logAfter(thePoint);
   }

   @AfterReturning(pointcut = "execution(* com..*Consumer.*(..))", returning = "theResult")
   public void logConsumersAfterReturning(final JoinPoint thePoint, final Object theResult) {
      this.logAfterReturning(thePoint, theResult);
   }

   @AfterThrowing(pointcut = "execution(* com..*Consumer.*(..))", throwing = "theException")
   public void logConsumersAfterThrowing(final JoinPoint thePoint, final Throwable theException) {
      this.logAfterThrowing(thePoint, theException);
   }

   @Before("execution(* com..*Producer.*(..))")
   public void logProducersBefore(final JoinPoint thePoint) {
      this.logBefore(thePoint);
   }

   @After("execution(* com..*Producer.*(..))")
   public void logProducersAfter(final JoinPoint thePoint) {
      this.logAfter(thePoint);
   }

   @AfterReturning(pointcut = "execution(* com..*Producer.*(..))", returning = "theResult")
   public void logProducersAfterReturning(final JoinPoint thePoint, final Object theResult) {
      this.logAfterReturning(thePoint, theResult);
   }

   @AfterThrowing(pointcut = "execution(* com..*Producer.*(..))", throwing = "theException")
   public void logProducersAfterThrowing(final JoinPoint thePoint, final Throwable theException) {
      this.logAfterThrowing(thePoint, theException);
   }

   protected void logBefore(final JoinPoint thePoint) {
      if (log.isDebugEnabled()) {
         log.debug("Before {}", thePoint.getSignature().toShortString());
      }
   }

   protected void logAfter(final JoinPoint thePoint) {
      if (log.isDebugEnabled()) {
         log.debug("After {}", thePoint.getSignature().toShortString());
      }
   }

   protected void logAfterReturning(final JoinPoint thePoint, final Object theResult) {
      if (log.isDebugEnabled()) {
         log.debug("After {}, returning [{}] ", thePoint.getSignature().toShortString(), theResult);
      }
   }

   protected void logAfterThrowing(final JoinPoint thePoint, final Throwable theException) {
      log.error("{} threw {}; message: {}", thePoint.getSignature().toShortString(), theException.getClass().getName(), theException.getMessage());
   }
}