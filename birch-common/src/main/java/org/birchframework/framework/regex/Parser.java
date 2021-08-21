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

package org.birchframework.framework.regex;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;
import org.birchframework.framework.beans.Beans;
import org.birchframework.framework.stub.Stub;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.checkerframework.org.plumelib.util.RegexUtil;
import org.springframework.core.annotation.AnnotationUtils;

import static org.birchframework.dto.BirchErrorCode.*;

/**
 * Parser that works with classes annotated with {@link RegexBinding} and fields annotated with {@link CaptureGroup}.  This utility
 * will parse an input against any number of annotated classes, and will create an instance of each of the class types where its defined regular
 * expression matches <bold>one line</bold> of that input string.
 * @see RegexBinding
 * @see CaptureGroup
 * @author Keivan Khalichi
 */
@SuppressWarnings({"AutoBoxing", "AutoUnboxing"})
public class Parser {

   private final Map<Class<?>, ParseClassDescriptor> types;

   /**
    * Constructor that prepares the parser given the class types.
    * @param theTypes
    */
   Parser(final Class<?>[] theTypes) {
      this.types = Arrays.stream(theTypes).collect(Collectors.toMap(
         clazz -> clazz,
         clazz -> {
            final var aFieldDescriptors =
               FieldUtils.getFieldsListWithAnnotation(clazz, CaptureGroup.class)
                         .stream()
                         .peek(f -> f.setAccessible(true))
                         .map(f -> new FieldDescriptor(f, (Integer) Objects.requireNonNull(AnnotationUtils.getValue(f.getAnnotation(CaptureGroup.class)))))
                         .collect(Collectors.toList());
            final var aDuplicatesList =
               aFieldDescriptors.stream()
                                .collect(Collectors.groupingBy(f -> f.captureGroup, Collectors.counting()))
                                .entrySet()
                                .stream()
                                .filter(entry -> entry.getValue() > 1)
                                .map(Entry::getKey)
                                .collect(Collectors.toList());
            if (!aDuplicatesList.isEmpty()) {
               throw new ParseException(B12040);
            }
            final var aRegexAnnotation = AnnotationUtils.findAnnotation(clazz, RegexBinding.class);
            final var aRegex = (String) AnnotationUtils.getValue(aRegexAnnotation);
            final var aRegexException = RegexUtil.regexException(
               aRegex, aFieldDescriptors.stream().map(fd -> fd.captureGroup).max(Comparator.comparingInt(cg -> cg)).orElse(0)
            );
            if (aRegexException != null) {
               throw new ParseException(B12045, aRegexException);
            }
            return new ParseClassDescriptor(aFieldDescriptors, aRegex);
         }
      ));
   }

   /**
    * Factory method to create an instance of this utility.
    * @param theTypes class types annotated with {@link RegexBinding}
    * @return a parser instance
    */
   @SuppressWarnings("VariableArgumentMethod")
   public static Parser of(final Class<?>... theTypes) {
      if (ArrayUtils.isEmpty(theTypes)) {
         throw new ParseException(B12010);
      }
      return new Parser(theTypes);
   }

   /**
    * Parses the input against the class types provided in the {@link #of(Class[])} method.  This method is thread-safe.
    * @param theInput the input to be parsed
    * @return instances of types provided, with one instance per match within the input
    */
   public List<?> parse(final String theInput) {
      return this.parseStream(theInput).collect(Collectors.toList());
   }

   /**
    * Parses the input against the class types provided in the {@link #of(Class[])} method, in a stream.  This method is thread-safe.
    * @param theInput the input to be parsed
    * @return stream of instances of types provided, with one instance per match within the input
    */
   public Stream<?> parseStream(final String theInput) {
      if (StringUtils.isBlank(theInput)) {
         throw new ParseException(B12020);
      }
      final var aMatcherRef = new AtomicReference<Matcher>();
      return theInput.lines()
                     .flatMap(line ->
                        this.types.entrySet()
                            .stream()      // cannot be a parallel stream
                            .filter(entry -> {
                               aMatcherRef.set(entry.getValue().matcher(line));
                               return aMatcherRef.get().matches();
                            })
                            .map(entry -> {
                               final var aClass     = entry.getKey();
                               final var aClassDesc = entry.getValue();
                               return Stub.of(aClass, targetObject -> {
                                  aClassDesc.fields.forEach((captureGroup, fieldDesc) -> {
                                     final var aValue = aMatcherRef.get().group(captureGroup);
                                     if (StringUtils.isNotBlank(aValue)) {
                                        try {
                                           FieldUtils.writeField(fieldDesc.field, targetObject, Beans.valueOf(fieldDesc.field.getType(), aValue));
                                        }
                                        catch (IllegalAccessException e) {
                                           throw new ParseException(B12030, e);
                                        }
                                     }
                                  });
                               });
                            })
                     );
   }

   static class ParseClassDescriptor {

      final Map<Integer, FieldDescriptor> fields;
      final Pattern                       pattern;

      ParseClassDescriptor(final List<FieldDescriptor> theFieldDescriptors, final String theRegex) {
         this.fields = Collections.unmodifiableMap(theFieldDescriptors.stream().collect(Collectors.toMap(fd -> fd.captureGroup, fd -> fd)));
         this.pattern = Pattern.compile(theRegex);
      }

      Matcher matcher(final String theString) {
         return this.pattern.matcher(theString);
      }
   }

   static class FieldDescriptor {
      final Field field;
      final int captureGroup;

      FieldDescriptor(@NotNull final Field theField, @NotNull final int theCaptureGroup) {
         field        = theField;
         captureGroup = theCaptureGroup;
      }
   }
}