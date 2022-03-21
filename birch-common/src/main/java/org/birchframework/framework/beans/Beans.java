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
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.birchframework.dto.ErrorCode;
import org.birchframework.framework.spring.SpringContext;
import org.checkerframework.checker.regex.qual.Regex;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
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
      final var aFilterBuilder = new FilterBuilder().excludePattern(DEFAULT_IGNORED_BASE_PACKAGES);
      defaultExcludedBasePackagesConfig = new ConfigurationBuilder().filterInputsBy(aFilterBuilder);
   }

   /**
    * Registers a default mapping to the provided model class from its superclass (class --&gt; superclass).  The superclass must be a class other
    * than {@link Object}.
    * @param theModelClass the model class
    * @param theMapNulls map null values in superclass instance to the subclass instance
    * @param theIgnoredProperties ignored property names while mapping
    * @param <T> type of the subclass (i.e. the model class)
    * @throws RuntimeException if the superclass is {@link Object}
    */
   public static <T> void registerMapping (@Nonnull final Class<T> theModelClass, final boolean theMapNulls, final String... theIgnoredProperties) {
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

   /**
    * Registers a default mapping between a source class and a target class.
    * @param theA the source class
    * @param theB the target class
    * @param theMapNulls map null values in superclass instance to the subclass instance
    * @param theExcludedProperties property names excluded from mapping
    * @param <A> type of the source
    * @param <B> type of the target
    */
   public static <A,B> void registerMapping(@Nonnull final Class<A> theA, @Nonnull final Class<B> theB,
                                            final boolean theMapNulls, final String... theExcludedProperties) {
      final ClassMapBuilder<A, B> aClassMapBuilder = mapperFactory.classMap(theA, theB).mapNulls(theMapNulls).mapNullsInReverse(theMapNulls);
      Stream.of(theExcludedProperties).forEach(aClassMapBuilder::exclude);
      aClassMapBuilder.byDefault().register();
   }

   /**
    * Maps properties from the source object to an instance of the target class that is created.
    * @param theSource the source object
    * @param theTargetClass the target class
    * @param <S> type of the source object
    * @param <T> type of the target class
    * @return instance of target class
    */
   @SuppressWarnings("unchecked")
   public static <S,T> T mapProperties(@Nonnull final S theSource, @Nonnull final Class<T> theTargetClass) {
      final var aBoundMapper = (BoundMapperFacade<S, T>) mapperFactory.getMapperFacade(theSource.getClass(), theTargetClass);
      return aBoundMapper.map(theSource);
   }

   /**
    * Maps properties in reverse, that is from the target object to an instance of the source class that is created.
    * @param theSourceClass the source class
    * @param theTarget the target object
    * @param <S> type of the source object
    * @param <T> type of the target class
    * @return instance of source class
    */
   @SuppressWarnings("unchecked")
   public static <S,T> S mapPropertiesReverse(@Nonnull final Class<S> theSourceClass, @Nonnull final T theTarget) {
      final var aBoundMapper = (BoundMapperFacade<S, T>) mapperFactory.getMapperFacade(theSourceClass, theTarget.getClass());
      return aBoundMapper.mapReverse(theTarget);
   }

   /**
    * Maps properties from a source object to a target object.
    * @param theSource the source object
    * @param theTarget the target object
    * @param <S> type of the source object
    * @param <T> type of the target object
    * @return a reference to the target object is passed as parameter
    */
   @SuppressWarnings("unchecked")
   public static <S,T> T mapProperties(@Nonnull final S theSource, @Nonnull final T theTarget) {
      final var aBoundMapper = (BoundMapperFacade<S, T>) mapperFactory.getMapperFacade(theSource.getClass(), theTarget.getClass());
      return aBoundMapper.map(theSource, theTarget);
   }

   /**
    * Maps properties in reverse, that is from the target object to the source object.
    * @param theSource the source object
    * @param theTarget the target object
    * @param <S> type of the source object
    * @param <T> type of the target object
    * @return a reference to the source object that is passed as parameter
    */
   @SuppressWarnings("unchecked")
   public static <S,T> S mapPropertiesReverse(@Nonnull final S theSource, @Nonnull final T theTarget) {
      final var aBoundMapper = (BoundMapperFacade<S, T>) mapperFactory.getMapperFacade(theSource.getClass(), theTarget.getClass());
      return aBoundMapper.mapReverse(theTarget, theSource);
   }

   /**
    * Maps properties from a source object to a target object, but does not used a pre-registered mapping factory.  This method is
    * <emphasis>quite inefficient</emphasis>, nevertheless provided for cases wherein the excluded properties are only known at runtime (as opposed to at
    * build time).  Also, this version of the overloaded method does not return a reference to the target object, which implies the target must not be null.
    * @param theSource the source object
    * @param theTarget the target object
    * @param <S> type of the source object
    * @param <T> type of the target object
    */
   public static <S,T> void mapProperties(@Nonnull final S theSource, @Nonnull final T theTarget, final boolean theMapNulls,
                                          final String... theExcludedProperties) {
      final var aMapperFactory = new DefaultMapperFactory.Builder().mapNulls(theMapNulls).build();
      final var aClassMap = aMapperFactory.classMap(theSource.getClass(), theTarget.getClass());
      Stream.of(theExcludedProperties).forEach(aClassMap::exclude);
      aClassMap.byDefault().register();
      aMapperFactory.getMapperFacade().map(theSource, theTarget);
   }

   /**
    * Converts a string value to its scalar or enumerated value equivalent based on the specified target type.  If the target type is
    * String, then the specified string value parameter is returned.
    * @param theTargetType the target type
    * @param theStringValue the value in string format
    * @return the wrapped object equivalent of the scalar, a String, or Enum value
    */
   @SuppressWarnings("unchecked")
   public static <T> Object valueOf(@Nonnull final Class<T> theTargetType, @Nonnull final String theStringValue) {
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
         final var aValue = theStringValue.replace("-", "_");
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
      return findImplementation(null, theInterface, DEFAULT_BASE_PACKAGE_DEPTH, true, theExcludeClasses);
   }

   public static <T> Optional<Class<? extends T>> findImplementation(final Class<T> theInterface, final boolean theIsGlobalFlag,
                                                                     final Class<?>... theExcludeClasses) {
      return findImplementation(null, theInterface, DEFAULT_BASE_PACKAGE_DEPTH, theIsGlobalFlag, theExcludeClasses);
   }

   public static <T> Optional<Class<? extends T>> findImplementation(final StackWalker theStackWalker, final Class<T> theInterface,
                                                                     final Class<?>... theExcludeClasses) {
      return findImplementation(theStackWalker, theInterface, DEFAULT_BASE_PACKAGE_DEPTH, false, theExcludeClasses);
   }

   @SuppressWarnings("unchecked")
   public static <T> Optional<Class<? extends T>> findImplementation(@Nullable final StackWalker theStackWalker, @Nonnull final Class<T> theInterface,
                                                                     final int theBasePackageDepth, final boolean theIsGlobalFlag,
                                                                     final Class<?>... theExcludeClasses) {
      final FilterBuilder aFilterBuilder;
      final String aBasePackage;
      if (theIsGlobalFlag) {
         aFilterBuilder = new FilterBuilder().excludePattern(DEFAULT_IGNORED_BASE_PACKAGES);
         aBasePackage = "";
      }
      else {
         final var aCallerClass = findCallerClass(theStackWalker, DEFAULT_IGNORED_BASE_PACKAGES);
         if (aCallerClass.isEmpty()) {
            aFilterBuilder = new FilterBuilder().excludePattern(DEFAULT_IGNORED_BASE_PACKAGES);
            aBasePackage = "";
         }
         else {
            aBasePackage = computeBasePackageName(aCallerClass.get(), theBasePackageDepth);
            aFilterBuilder = new FilterBuilder().includePackage(aBasePackage).excludePattern(DEFAULT_IGNORED_BASE_PACKAGES);
         }
      }
      final var aConfigBuilder = new ConfigurationBuilder().addUrls(ClasspathHelper.forPackage(aBasePackage))
                                                           .filterInputsBy(aFilterBuilder)
                                                           .setScanners(Scanners.SubTypes);
      final var anExcludeClasses = theExcludeClasses == null ? Collections.<Class<?>>emptyList() : Arrays.asList(theExcludeClasses);
      final var anImplementation = (Class<T>) new Reflections(aConfigBuilder).getSubTypesOf(theInterface)
                                                                             .stream()
                                                                             .filter(c -> anExcludeClasses.stream().noneMatch(ec -> ec.isAssignableFrom(c)))
                                                                             .findFirst()
                                                                             .orElse(null);
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
      return aStackWalker.walk(frames -> frames.dropWhile(e -> e.getClassName().matches(anIgnoredBasePackages)).findFirst().map(StackFrame::getDeclaringClass));
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
                                                                 .filterInputsBy(new FilterBuilder().excludePattern(DEFAULT_IGNORED_BASE_PACKAGES));
            final var aReflections = new Reflections(aConfigBuilder);
            return aReflections.getTypesAnnotatedWith(theAnnotationClass);
         }
      }
   }

   public static Set<Class<?>> classesAnnotatedWith(final Class<? extends Annotation> theAnnotationClass, @Nullable String... theBasePackages) {
      final var aReflections = new Reflections((Object[]) theBasePackages);
      return aReflections.getTypesAnnotatedWith(theAnnotationClass);
   }

   public static String computeBasePackageName(@Nonnull final Class<?> theClass) {
      return computeBasePackageName(theClass, DEFAULT_BASE_PACKAGE_DEPTH);
   }

   public static String computeBasePackageName(@Nonnull final Class<?> theClass, final int theDepth) {
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
    * Invokes {@code theFunction} only if {@code theObject} is not null and using {@code theObject} as the paramter to the function;
    * otherwise returns {@code theValueIfNull}
    * @param theObject the object to be tested for nullness and consumed by {@code theFunction}
    * @param theFunction the function
    * @param theValueIfNull the return value of this method if {@code theObject} is null (essentially the default value)
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