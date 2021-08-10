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

import java.lang.StackWalker.StackFrame;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.birchframework.dto.ErrorCode;
import org.birchframework.framework.spring.SpringContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.regex.qual.Regex;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;
import static lombok.AccessLevel.PRIVATE;

/**
 * Bean utilities.
 * @author Keivan Khalichi
 */
@NoArgsConstructor(access = PRIVATE)
@SuppressWarnings({"VariableArgumentMethod", "unused", "WeakerAccess", "AutoBoxing"})
@Slf4j
public class Beans {

   /** Depth of base packages to scan for {@link ErrorCode} implementation */
   public static final int    DEFAULT_BASE_PACKAGE_DEPTH    = 2;
   @Regex(1)
   public static final String DEFAULT_IGNORED_BASE_PACKAGES =
      "(java|javax|org\\.birchframework|org\\.apache|org\\.springframework|lombok|com\\.sun|com\\.google|org\\.junit|com\\.intellij|org\\.jetbrains|org\\.eclipse).*";

   private static final String[]      DEFAULT_IGNORED_BASE_PACKAGES_ARRAY = DEFAULT_IGNORED_BASE_PACKAGES.replaceAll("\\(|\\)|\\\\|\\.\\*", "").split("\\|");
   private static final Configuration defaultExcludedBasePackagesConfig;
   private static final MapperFactory mapperFactory                       = new DefaultMapperFactory.Builder().build();

   static {
      final var aFilterBuilder = new FilterBuilder().exclude(DEFAULT_IGNORED_BASE_PACKAGES);
      defaultExcludedBasePackagesConfig = new ConfigurationBuilder().filterInputsBy(aFilterBuilder);
   }

   /**
    * Copies properties from source to a new instance of the target class, provided the properties match in name and type.  Property names listed within
    * the {@code theIgnoredProperties} parameter are skipped.
    * @param theSource the source bean
    * @param theTargetClass the target bean's class type; instance is returned by method
    * @param theIgnoredProperties the property names to skip
    * @param <S> the type of the source bean
    * @param <T> the type of the target bean
    * @return returns a reference to the newly created target bean
    */
   public static <S,T> T copyProperties(@NonNull final S theSource, @NonNull final Class<T> theTargetClass, final String... theIgnoredProperties) {
      try {
         return copyProperties(theSource, theTargetClass.getDeclaredConstructor().newInstance(), false, theIgnoredProperties);
      }
      catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
         final var aMessage = String.format("Unable to create new instantce of target class %s", theTargetClass.getName());
         throw new RuntimeException(aMessage, e);
      }
   }

   /**
    * Copies properties from source to target, provided they match in name and type.  Property names listed within the {@code theIgnoredProperties} parameter
    * are skipped.
    * @param theSource the source bean
    * @param theTarget the target bean
    * @param theIgnoredProperties the property names to skip
    * @param <S> the type of the source bean
    * @param <T> the type of the target bean
    * @return returns a reference to the target
    * @deprecated Use {@link #mapProperties(Object, Object)} or {@link #mapProperties(Object, Class)} instead
    */
   @Deprecated(forRemoval = true)
   public static <S,T> T copyProperties(@NonNull final S theSource, @NonNull final T theTarget, final String... theIgnoredProperties) {
      return copyProperties(theSource, theTarget, false, theIgnoredProperties);
   }

   /**
    * Copies properties from source to target, provided they match in name and type.  Property names listed within the {@code theIgnoredProperties} parameter
    * are skipped.
    * @param theSource the source bean
    * @param theTarget the target bean
    * @param theSkipNulls if true, skips copying null source values
    * @param theIgnoredProperties the property names to skip
    * @param <S> the type of the source bean
    * @param <T> the type of the target bean
    * @return returns a reference to the target
    * @deprecated Use {@code #mapProperties} instead
    */
   @Deprecated(forRemoval = true)
   public static <S,T> T copyProperties(@NonNull final S theSource, @NonNull final T theTarget, final boolean theSkipNulls, final String... theIgnoredProperties) {
      Objects.requireNonNull(theSource);
      Objects.requireNonNull(theTarget);

      final var aTargetClass = theTarget.getClass();
      final var aTargetDescriptors  = BeanUtils.getPropertyDescriptors(aTargetClass);
      final var anIgnoredList = theIgnoredProperties == null ? Collections.emptyList() : List.of(theIgnoredProperties);

      Stream.of(aTargetDescriptors).filter(td -> !anIgnoredList.contains(td.getName()) && td.getWriteMethod() != null).forEach(targetDesc -> {
         final var aWriteMethod = targetDesc.getWriteMethod();
         final var aSourceDesc = BeanUtils.getPropertyDescriptor(theSource.getClass(), targetDesc.getName());
         if (aSourceDesc != null) {
            final var aReadMethod = aSourceDesc.getReadMethod();
            if (aReadMethod != null && ClassUtils.isAssignable(aReadMethod.getReturnType(), aWriteMethod.getParameterTypes()[0])) {
               try {
                  if (!Modifier.isPublic(aReadMethod.getDeclaringClass().getModifiers())) {
                     aReadMethod.setAccessible(true);
                  }
                  final var aSourceValue = aReadMethod.invoke(theSource);
                  if (theSkipNulls) {
                     if (aSourceValue != null) {
                        if (!Modifier.isPublic(aWriteMethod.getDeclaringClass().getModifiers())) {
                           aWriteMethod.setAccessible(true);
                        }
                        aWriteMethod.invoke(theTarget, aSourceValue);
                     }
                  }
                  else {
                     if (!Modifier.isPublic(aWriteMethod.getDeclaringClass().getModifiers())) {
                        aWriteMethod.setAccessible(true);
                     }
                     aWriteMethod.invoke(theTarget, aSourceValue);
                  }
               }
               catch (Throwable e) {
                  throw new RuntimeException(String.format("Could not copy property '%s' from source to target", targetDesc.getName()), e);
               }
            }
         }
      });
      return theTarget;
   }

   /**
    * Registers a mapping to the provided model class from its superclass (class --&gt; superclass).  The superclass must be a class other than {@link Object}.
    * @param theModelClass the model class
    * @param theMapNulls map null values in superclass instance to the subclass instance
    * @param theIgnoredProperties ignored property names while mapping
    * @param <T> type of the subclass (i.e. the model class)
    * @throws RuntimeException when the superclass is {@link Object}
    */
   public static <T> void registerMapping (@NonNull final Class<T> theModelClass, final boolean theMapNulls, final String... theIgnoredProperties) {
      final var aSuperclass = theModelClass.getSuperclass();
      if (aSuperclass == Object.class) {
         throw new RuntimeException(String.format(
            "%s applied to a class for which the superclass is %s; superclass must be a DTO",
            MappingModel.class.getName(),
            Object.class.getName()
         ));
      }
      registerMapping(aSuperclass, theModelClass, theMapNulls, theIgnoredProperties);
   }

   public static <A,B> void registerMapping(@NonNull final Class<A> theA, @NonNull final Class<B> theB,
                                            final boolean theMapNulls, final String... theIgnoredProperties) {
      final ClassMapBuilder<A, B> aClassMapBuilder = mapperFactory.classMap(theA, theB).mapNulls(theMapNulls).mapNullsInReverse(theMapNulls).byDefault();
      if (theIgnoredProperties.length > 0) {
         Stream.of(theIgnoredProperties).forEach(aClassMapBuilder::exclude);
      }
      aClassMapBuilder.register();
   }

   @SuppressWarnings("unchecked")
   public static <S,T> T mapProperties(@NonNull final S theSource, @NonNull final Class<T> theTargetClass) {
      try {
         final var aBoundMapper = (BoundMapperFacade<S, T>) mapperFactory.getMapperFacade(theSource.getClass(), theTargetClass);
         return aBoundMapper.map(theSource, theTargetClass.getConstructor().newInstance());
      }
      catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
         final var aMessage = String.format("Unable to create new instantce of target class %s", theTargetClass.getName());
         throw new RuntimeException(aMessage, e);
      }
   }

   @SuppressWarnings("unchecked")
   public static <S,T> T mapProperties(@NonNull final S theSource, @NonNull final T theTarget) {
      final var aBoundMapper = (BoundMapperFacade<S, T>) mapperFactory.getMapperFacade(theSource.getClass(), theTarget.getClass());
      return aBoundMapper.map(theSource, theTarget);
   }

   /**
    * Converts a string value to its scalar or enumerated value equivalent based on the specified target type.  If the target type is
    * String, then the specified string value parameter is returned.
    * @param theTargetType the target type
    * @param theStringValue the value in string format
    * @return the wrapped object equivalent of the scalar, a String, or Enum value
    */
   @SuppressWarnings("unchecked")
   public static <T> Object valueOf(@NonNull final Class<T> theTargetType, @NonNull final String theStringValue) {
      if (String.class.isAssignableFrom(theTargetType)) {
         return theStringValue;
      }
      else if (int.class.isAssignableFrom(theTargetType) || Integer.class.isAssignableFrom(theTargetType)) {
         return Integer.valueOf(theStringValue);
      }
      else if (boolean.class.isAssignableFrom(theTargetType) || Boolean.class.isAssignableFrom(theTargetType)) {
         return BooleanUtils.toBoolean(theStringValue);
      }
      else if (long.class.isAssignableFrom(theTargetType) || Long.class.isAssignableFrom(theTargetType)) {
         return Long.valueOf(theStringValue);
      }
      else if (double.class.isAssignableFrom(theTargetType) || Double.class.isAssignableFrom(theTargetType)) {
         return Double.valueOf(theStringValue);
      }
      else if (char.class.isAssignableFrom(theTargetType) || Character.class.isAssignableFrom(theTargetType)) {
         return theStringValue.charAt(0);
      }
      else if (Enum.class.isAssignableFrom(theTargetType)) {
         final var aValue = theStringValue.replaceAll("-", "_");
         return Stream.of(((Class<Enum<?>>) theTargetType).getEnumConstants()).filter(value -> value.name().equalsIgnoreCase(aValue)).findFirst().orElse(null);
      }
      else if (float.class.isAssignableFrom(theTargetType) || Float.class.isAssignableFrom(theTargetType)) {
         return Float.valueOf(theStringValue);
      }
      else if (byte.class.isAssignableFrom(theTargetType) || Byte.class.isAssignableFrom(theTargetType)) {
         return Byte.valueOf(theStringValue);
      }
      else if (short.class.isAssignableFrom(theTargetType) || Short.class.isAssignableFrom(theTargetType)) {
         return Short.valueOf(theStringValue);
      }
      return null;
   }

   public static <T> Optional<Class<? extends T>> findImplementation(final Class<T> theInterface, final Class<?>... theExcludeClasses) {
      return findImplementation(null, theInterface, DEFAULT_BASE_PACKAGE_DEPTH, theExcludeClasses);
   }

   public static <T> Optional<Class<? extends T>> findImplementation(final StackWalker theStackWalker, final Class<T> theInterface,
                                                                     final Class<?>... theExcludeClasses) {
      return findImplementation(theStackWalker, theInterface, DEFAULT_BASE_PACKAGE_DEPTH, theExcludeClasses);
   }

   @SuppressWarnings("unchecked")
   public static <T> Optional<Class<? extends T>> findImplementation(@Nullable final StackWalker theStackWalker, @NonNull final Class<T> theInterface,
                                                                     final int theBasePackageDepth, final Class<?>... theExcludeClasses) {
      final var aCallerClass = findCallerClass(theStackWalker, DEFAULT_IGNORED_BASE_PACKAGES);
      final Class<T> anImplementation;
      if (aCallerClass.isEmpty()) {
         anImplementation = null;
      }
      else {
         final var aBasePackage = computeBasePackageName(aCallerClass.get(), theBasePackageDepth);
         if (StringUtils.isBlank(aBasePackage)) {
            anImplementation = null;
         }
         else {
            final var anExcludeClasses = theExcludeClasses == null ? Collections.<Class<?>>emptyList() : Arrays.asList(theExcludeClasses);
            final var aFilterBuilder = new FilterBuilder().includePackage(aBasePackage).exclude(DEFAULT_IGNORED_BASE_PACKAGES);
            final var aConfigBuilder = new ConfigurationBuilder().addUrls(ClasspathHelper.forPackage(aBasePackage))
                                                                 .filterInputsBy(aFilterBuilder)
                                                                 .setScanners(new SubTypesScanner());
            anImplementation = (Class<T>) new Reflections(aConfigBuilder).getSubTypesOf(theInterface)
                                                                         .stream()
                                                                         .filter(c -> anExcludeClasses.stream().noneMatch(ec -> ec.isAssignableFrom(c)))
                                                                         .findFirst()
                                                                         .orElse(null);
         }
      }
      return Optional.ofNullable(anImplementation);
   }

   public static Optional<Class<?>> findCallerClass() {
      return findCallerClass(null, null);
   }

   public static Optional<Class<?>> findCallerClass(final String theIgnoredBasePackages) {
      return findCallerClass(null, theIgnoredBasePackages);
   }

   public static Optional<Class<?>> findCallerClass(@Nullable final StackWalker theStackWalker, @Regex final String theIgnoredBasePackages) {
      final var aStackWalker = theStackWalker == null ? StackWalker.getInstance(RETAIN_CLASS_REFERENCE) : theStackWalker;
      final var anIgnoredBasePackages = StringUtils.isBlank(theIgnoredBasePackages) ? DEFAULT_IGNORED_BASE_PACKAGES : theIgnoredBasePackages;
      return aStackWalker.walk(frames -> frames.filter(e -> !e.getClassName().matches(anIgnoredBasePackages)).findFirst().map(StackFrame::getDeclaringClass));
   }

   public static Set<Class<?>> classesAnnotatedWith(final Class<? extends Annotation> theAnnotationClass) {
      final var aCallerClass = findCallerClass();
      if (aCallerClass.isEmpty()) {
         return Collections.emptySet();
      }
      else {
         final var aBasePackage = computeBasePackageName(aCallerClass.get(), DEFAULT_BASE_PACKAGE_DEPTH);
         if (StringUtils.isBlank(aBasePackage)) {
            return Collections.emptySet();
         }
         else {
            final var aConfigBuilder = new ConfigurationBuilder().addUrls(ClasspathHelper.forPackage(aBasePackage))
                                                                 .filterInputsBy(new FilterBuilder().exclude(DEFAULT_IGNORED_BASE_PACKAGES));
            final var aReflections = new Reflections(aConfigBuilder);
            return aReflections.getTypesAnnotatedWith(theAnnotationClass);
         }
      }
   }

   public static Set<Class<?>> classesAnnotatedWith(final Class<? extends Annotation> theAnnotationClass, final String... theBasePackages) {
      final var aReflections = new Reflections((Object[]) theBasePackages);
      return aReflections.getTypesAnnotatedWith(theAnnotationClass);
   }

   public static String computeBasePackageName(@NonNull final Class<?> theClass) {
      return computeBasePackageName(theClass, DEFAULT_BASE_PACKAGE_DEPTH);
   }

   public static String computeBasePackageName(@NonNull final Class<?> theClass, final int theDepth) {
      if (theDepth <= 0) {
         throw new AssertionError("Depth must be greater than 0");
      }
      final var aCallerClassName = theClass.getName();
      var anIndex = 0;
      for (int i = 0; i < theDepth; i++) {
         anIndex = aCallerClassName.indexOf('.', aCallerClassName.indexOf('.') + anIndex);
      }
      return aCallerClassName.substring(0, anIndex);
   }

   /**
    * Given a class, attempts to find and return a bean of the provided class type, and if not found, will construct return an instance of that class.
    * The type must have a public default constructor for the latter case.
    * @param theClass the class of the type to find bean or create instance
    * @param <T> the target type
    * @return null if the parameter is null; an instance otherwise
    * @throws Exception thrown if the bean is not found and an instance cannot be created
    */
   public static <T> T findBeanOrCreateInstance(final Class<T> theClass) throws Exception {
      return findBeanOrCreateInstance(theClass, new Object[0]);
   }

   public static <T> T findBeanOrCreateInstance(final Class<T> theClass, @Nullable final Object... theConstructorArgs) throws Exception {
      if (theClass == null) {
         return null;
      }
      try {
         var aBean = SpringContext.getBean(theClass);
         if (aBean == null) {
            log.info("SpringContext was not loaded; attempting to create a new instance of {}", theClass.getName());
            return ConstructorUtils.invokeConstructor(theClass, theConstructorArgs);
         }
         return aBean;
      }
      catch (NoSuchBeanDefinitionException e) {
         log.info("No bean of type {} found; attempting to create a new instance", theClass.getName());
         return ConstructorUtils.invokeConstructor(theClass, theConstructorArgs);
      }
   }

   /**
    * Invokes {@code theConsumer} only if {@code theObject} is not null
    * @param theObject the object to be tested and consumed by the {@code theConsumer}
    * @param theConsumer the consumer
    * @param <T> the datatype of the object being tested and possibly consumed
    */
   public static <T> void consumeIfNotNull(final T theObject, final Consumer<T> theConsumer) {
      if (theObject != null) {
         theConsumer.accept(theObject);
      }
   }

   /**
    * Invokes {@code theFunction} only if {@code theObject} is not null
    * @param theObject the object to be tested and consumed by {@code theFunction}
    * @param theFunction the consumer
    * @param <T> the datatype of the object being tested and possibly consumed
    * @param <R> the datatype of the return value resulting from invokation of {@code theFunction}
    * @return the result of {@code theFunction}, null if {@code theObject} is null
    */
   public static <T, R> R invokeIfNotNull(final T theObject, final Function<T, R> theFunction, final R theValueIfNull) {
      if (theObject == null) {
         return theValueIfNull;
      }
      return theFunction.apply(theObject);
   }

   /**
    * Runs {@code theRunnable} only if {@code theCondition} is true
    * @param theCondition the test condition
    * @param theRunnable the runnable
    */
   public static void runIfTrue(final boolean theCondition, Runnable theRunnable) {
      if (theCondition) {
         theRunnable.run();
      }
   }
}