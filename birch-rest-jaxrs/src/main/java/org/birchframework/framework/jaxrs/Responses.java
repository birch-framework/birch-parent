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
package org.birchframework.framework.jaxrs;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import org.birchframework.dto.BirchErrorCode;
import org.birchframework.dto.ErrorCode;
import org.birchframework.framework.beans.Beans;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;
import static org.birchframework.dto.BirchErrorCode.*;
import static javax.ws.rs.core.Response.Status.*;

/**
 * REST/HTTP response utilities.  Enables streamlined interaction with {@link Response} objects via functional programming style enabled by Java Lambdas,
 * such as:
 * <p/>
 * <pre>
 * Responses.of(service.findBookByISBN(anISBN)).ifOKOrElse(
 *    Book.class,
 *    book -&gt; log.info("Book: {}", book.toString()),   // book is automatically desrialized as an instance of Book.class
 *    errorCode -&gt; log.error("Error retrieving book with ISBN: {}; error: {}", anISBN, errorCode.asString())
 * );
 * </pre>
 * This class is not thread-safe.  The factory {@link Responses#of(Response)} must be called for each new {@link Response} object.
 * @author Keivan Khalichi
 */
@Slf4j
@SuppressWarnings({"unchecked", "unused"})
public class Responses {

   public static final int HTTP_OK                    = OK.getStatusCode();
   public static final int HTTP_CREATED               = CREATED.getStatusCode();
   public static final int HTTP_BAD_REQUEST           = BAD_REQUEST.getStatusCode();
   public static final int HTTP_NOT_MODIFIED          = NOT_MODIFIED.getStatusCode();
   public static final int HTTP_NOT_FOUND             = NOT_FOUND.getStatusCode();
   public static final int HTTP_NO_CONTENT            = NO_CONTENT.getStatusCode();
   public static final int HTTP_INTERNAL_SERVER_ERROR = INTERNAL_SERVER_ERROR.getStatusCode();
   public static final int HTTP_NOT_IMPLEMENTED       = NOT_IMPLEMENTED.getStatusCode();

   private static final ObjectMapper objectMapper          = Jackson2ObjectMapperBuilder.json().build();
   private static final ObjectMapper errorCodeObjectMapper = Jackson2ObjectMapperBuilder.json().featuresToEnable(ALLOW_UNQUOTED_FIELD_NAMES).build();

   /** Reference to the {@link ErrorCode} implementation class.  Its value is looked up and initialized the very first time this class is loaded. */
   private static final Class<? extends Enum<?>> errorCodeClass;

   static {
      errorCodeClass = (Class<? extends Enum<?>>) Beans.findImplementation(ErrorCode.class, false, BirchErrorCode.class)
                                                       .filter(Enum.class::isAssignableFrom)
                                                       .orElse(BirchErrorCode.class);
      if (errorCodeClass == BirchErrorCode.class) {
         log.warn("Unable to find suitable implementation of {}; there must be exactly one enum implementation of this interface beside {}",
                  ErrorCode.class.getName(), BirchErrorCode.class.getName());
      }
   }

   /** The wrapped JAX-RS {@link Response} */
   private final Response response;

   /**
    * Private constructor.  Given the last caller, find the first implementation of the ErrorCode interface from the caller's base package
    * in order to use it in deserializing error codes.
    * @param theResponse the object to be wrapped
    */
   private Responses(final Response theResponse) {
      this.response = theResponse;
   }

   /**
    * Factory method.  Uses calling method to construct an instance of this class
    * @param theResponse the object to be wrapped
    * @return an instance of this class
    */
   public static Responses of(@Nonnull final Response theResponse) {
      return new Responses(theResponse);
   }

   /**
       * Getter for {@link #response}.  Provided for subclasses to use.
    * @returns Value of {@link #response}
    */
   protected Response getResponse() {
      return response;
   }

   /**
    * Returns {@link Response#getStatusInfo()} == {@link Status#OK}.
    * This method is provided for functional programming (i.e. Responses::ok)
    * @param theResponse the target object
    * @param <T> any type that extends {@link Response}
    * @return true if response was {@link Status#OK}
    */
   public static <T extends Response> boolean ok(final T theResponse) {
      return theResponse != null && theResponse.getStatus() == HTTP_OK;
   }

   /**
    * Returns {@link Response#getStatusInfo()} == {@link Status#CREATED}.
    * This method is provided for functional programming (i.e. Responses::created)
    * @param theResponse the target object
    * @param <T> any type that extends {@link Response}
    * @return true if response was {@link Status#CREATED}
    */
   public static <T extends Response> boolean created(final T theResponse) {
      return theResponse != null && theResponse.getStatus() == HTTP_CREATED;
   }

   /**
    * Returns {@link Response#getStatusInfo()} == {@link Status#NOT_FOUND}.
    * This method is provided for functional programming (i.e. Responses::notFound)
    * @param theResponse the target object
    * @param <T> any type that extends {@link Response}
    * @return true if response was {@link Status#NOT_FOUND}
    */
   public static <T extends Response> boolean notFound(final T theResponse) {
      return theResponse != null && theResponse.getStatus() == HTTP_NOT_FOUND;
   }

   /**
    * Returns {@link Response#getStatusInfo()} == {@link Status#NOT_MODIFIED}.
    * This method is provided for functional programming (i.e. Responses::notModified)
    * @param theResponse the target object
    * @param <T> any type that extends {@link Response}
    * @return true if response was {@link Status#NOT_MODIFIED}
    */
   public static <T extends Response> boolean notModified(final T theResponse) {
      return theResponse != null && theResponse.getStatus() == HTTP_NOT_MODIFIED;
   }

   /**
    * Returns {@link Response#getStatusInfo()} == {@link Status#INTERNAL_SERVER_ERROR}.
    * This method is provided for functional programming (i.e. Responses::internalServerError)
    * @param theResponse the target object
    * @param <T> any type that extends {@link Response}
    * @return true if response was {@link Status#INTERNAL_SERVER_ERROR}
    */
   public static <T extends Response> boolean internalServerError(final T theResponse) {
      return theResponse != null && theResponse.getStatus() == HTTP_INTERNAL_SERVER_ERROR;
   }

   /**
    * Returns {@link Response#getStatusInfo()} == {@link Status#BAD_REQUEST}.
    * @return true if response was {@link Status#BAD_REQUEST}
    */
   public boolean badRequest() {
      return this.response != null && this.response.getStatus() == HTTP_BAD_REQUEST;
   }

   /**
    * Returns {@link Response#getStatusInfo()} == {@link Status#OK}.
    * @return true if response was {@link Status#OK}
    */
   public boolean ok() {
      return this.response != null && this.response.getStatus() == HTTP_OK;
   }

   /**
    * Returns {@link Response#getStatusInfo()} == {@link Status#CREATED}.
    * @return true if response was {@link Status#CREATED}
    */
   public boolean created() {
      return this.response != null && this.response.getStatus() == HTTP_CREATED;
   }

   /**
    * Returns {@link Response#getStatusInfo()} == {@link Status#NOT_FOUND}.
    * @return true if response was {@link Status#NOT_FOUND}
    */
   public boolean notFound() {
      return this.response != null && this.response.getStatus() == HTTP_NOT_FOUND;
   }

   /**
    * Returns {@link Response#getStatusInfo()} == {@link Status#NOT_MODIFIED}.
    * @return true if response was {@link Status#NOT_MODIFIED}
    */
   public boolean notModified() {
      return this.response != null && this.response.getStatus() == HTTP_NOT_MODIFIED;
   }

   /**
    * Returns {@link Response#getStatusInfo()} == {@link Status#INTERNAL_SERVER_ERROR}.
    * @return true if response was {@link Status#INTERNAL_SERVER_ERROR}
    */
   public boolean internalServerError() {
      return this.response != null && this.response.getStatus() == HTTP_INTERNAL_SERVER_ERROR;
   }

   /**
    * Functional method that can be implemented with a lambda.  Will only execute {@code theAction} parameter if the wrapped response was {@link Status#OK}.
    * Unwraps the {@link Response#readEntity(Class)}, unmarshalls, and provides it to the specified type for the executing function.
    * @param theClass the the target entity type
    * @param theAction the function to be executed when the condition is met
    * @param <T> the expected type of the response entity
    * @return a reference to this object for the purposes of chaining
    */
   public <T extends Serializable> Responses ifOK(final Class<T> theClass, final Consumer<T> theAction) {
      if (this.ok()) {
         this.entity(theClass).ifPresent(theAction);
      }
      return this;
   }

   /**
    * Functional method that can be implemented with a lambda.  Will only execute {@code theAction} parameter if the wrapped response was {@link Status#OK}.
    * Unwraps the {@link Response#readEntity(GenericType)}, unmarshalls, and provides it to the specified type for the executing function.
    * @param theType the the target entity type
    * @param theAction the function to be executed when the condition is met
    * @param <T> the expected type of the response entity
    * @return a reference to this object for the purposes of chaining
    */
   public <T extends Serializable> Responses ifOK(final GenericType<T> theType, final Consumer<T> theAction) {
      if (this.ok()) {
         this.entity(theType).ifPresent(theAction);
      }
      return this;
   }

   /**
    * Functional method that can be implemented with a lambda.  Will only execute {@code theAction} parameter if the wrapped response was {@link Status#OK}.
    * Unwraps the entity via {@link ObjectMapper#readValue(String, TypeReference)}, unmarshalls, and provides it to the specified type for the
    * executing function.
    * @param theType the the target entity type
    * @param theAction the function to be executed when the condition is met
    * @param <T> the expected type of the response entity
    * @return a reference to this object for the purposes of chaining
    */
   public <T extends Serializable> Responses ifOK(final TypeReference<T> theType, final Consumer<T> theAction) {
      if (this.ok()) {
         this.entity(theType).ifPresent(theAction);
      }
      return this;
   }

   /**
    * Functional method that can be implemented with 2 lambdas.  Will only execute {@code theAction} parameter if the wrapped response was {@link Status#OK}.
    * Unwraps the {@link Response#readEntity(Class)}, unmarshalls, and provides it to the specified class type for the executing function.
    * Otherwise will execute the {@code theElseAction} parameter with the {@link ErrorCode} that is expected within the {@link #response}
    * @param theClass the the target entity type
    * @param theAction the function to be executed when the condition is met
    * @param theElseAction the function to be executed if response is <emphasis>not</emphasis> {@link #ok()}
    * @param <T> the expected type of the response entity
    */
   public <T extends Serializable> void ifOKOrElse(final Class<T> theClass, final Consumer<T> theAction, final Consumer<ErrorCode<?>> theElseAction) {
      if (this.ok()) {
         this.entity(theClass).ifPresent(theAction);
      }
      else {
         this.errorCode().ifPresent(theElseAction);
      }
   }

   /**
    * Functional method that can be implemented with 2 lambdas.  Will only execute {@code theAction} parameter if the wrapped response was {@link Status#OK}.
    * Unwraps the {@link Response#readEntity(GenericType)}, unmarshalls, and provides it to the specified class type for the executing function.
    * Otherwise will execute the {@code theElseAction} parameter with the {@link ErrorCode} that is expected within the {@link #response}
    * @param theType the the target entity type
    * @param theAction the function to be executed when the condition is met
    * @param theElseAction the function to be executed if response is <emphasis>not</emphasis> {@link #ok()}
    * @param <T> the expected type of the response entity
    */
   public <T extends Serializable> void ifOKOrElse(final GenericType<T> theType, final Consumer<T> theAction, final Consumer<ErrorCode<?>> theElseAction) {
      if (this.ok()) {
         this.entity(theType).ifPresent(theAction);
      }
      else {
         this.errorCode().ifPresent(theElseAction);
      }
   }

   /**
    * Functional method that can be implemented with 2 lambdas.  Will only execute {@code theAction} parameter if the wrapped response was {@link Status#OK}.
    * Unwraps the entity via {@link ObjectMapper#readValue(String, TypeReference)}, unmarshalls, and provides it to the specified type for the
    * executing function.
    * @param theType the the target entity type
    * @param theAction the function to be executed when the condition is met
    * @param theElseAction the function to be executed if response is <emphasis>not</emphasis> {@link #ok()}
    * @param <T> the expected type of the response entity
    */
   public <T extends Serializable> void ifOKOrElse(final TypeReference<T> theType, final Consumer<T> theAction, final Consumer<ErrorCode<?>> theElseAction) {
      if (this.ok()) {
         this.entity(theType).ifPresent(theAction);
      }
      else {
         this.errorCode().ifPresent(theElseAction);
      }
   }

   /**
    * Functional method that can be implemented with 2 lambdas.  Will only execute {@code theAction} parameter if the wrapped response was {@link Status#OK}.
    * Unwraps the {@link Response#readEntity(Class)}, unmarshalls, and provides it to the specified class type for the executing function.
    * Otherwise will execute the {@code theElseAction} parameter.
    * @param theClass the the target entity type
    * @param theAction the function to be executed when the condition is met
    * @param theElseAction the runnable to be executed if response is <emphasis>not</emphasis> {@link #ok()}
    * @param <T> the expected type of the response entity
    */
   public <T extends Serializable> void ifOKOrElse(final Class<T> theClass, final Consumer<T> theAction, final Runnable theElseAction) {
      if (this.ok()) {
         this.entity(theClass).ifPresent(theAction);
      }
      else {
         theElseAction.run();
      }
   }

   /**
    * Functional method that can be implemented with 2 lambdas.  Will only execute {@code theAction} parameter if the wrapped response was {@link Status#OK}.
    * Unwraps the {@link Response#readEntity(GenericType)}, unmarshalls, and provides it to the specified class type for the executing function.
    * Otherwise will execute the {@code theElseAction} parameter.
    * @param theType the the target entity type
    * @param theAction the function to be executed when the condition is met
    * @param theElseAction the runnable to be executed if response is <emphasis>not</emphasis> {@link #ok()}
    * @param <T> the expected type of the response entity
    */
   public <T extends Serializable> void ifOKOrElse(final GenericType<T> theType, final Consumer<T> theAction, final Runnable theElseAction) {
      if (this.ok()) {
         this.entity(theType).ifPresent(theAction);
      }
      else {
         theElseAction.run();
      }
   }

   /**
    * Functional method that can be implemented with 2 lambdas.  Will only execute {@code theAction} parameter if the wrapped response was {@link Status#OK}.
    * Unwraps the entity via {@link ObjectMapper#readValue(String, TypeReference)}, unmarshalls, and provides it to the specified type for the
    * executing function.
    * Otherwise will execute the {@code theElseAction} parameter.
    * @param theType the the target entity type
    * @param theAction the function to be executed when the condition is met
    * @param theElseAction the runnable to be executed if response is <emphasis>not</emphasis> {@link #ok()}
    * @param <T> the expected type of the response entity
    */
   public <T extends Serializable> void ifOKOrElse(final TypeReference<T> theType, final Consumer<T> theAction, final Runnable theElseAction) {
      if (this.ok()) {
         this.entity(theType).ifPresent(theAction);
      }
      else {
         theElseAction.run();
      }
   }

   /**
    * Functional method that can be implemented with a lambda.  Will only execute {@code theAction} parameter if the wrapped response was {@link Status#CREATED}.
    * Unwraps the {@link Response#readEntity(Class)}, unmarshalls, and provides it to the specified type for the executing function.
    * @param theClass the the target entity type
    * @param theAction the function to be executed when the condition is met
    * @param <T> the expected type of the response entity
    * @return a reference to this object for the purposes of chaining
    */
   public <T extends Serializable> Responses ifCreated(final Class<T> theClass, final Consumer<T> theAction) {
      if (this.created()) {
         this.entity(theClass).ifPresent(theAction);
      }
      return this;
   }

   /**
    * Functional method that can be implemented with a lambda.  Will only execute {@code theAction} parameter if the wrapped response was {@link Status#CREATED}.
    * Unwraps the {@link Response#readEntity(GenericType)}, unmarshalls, and provides it to the specified type for the executing function.
    * @param theType the the target entity type
    * @param theAction the function to be executed when the condition is met
    * @param <T> the expected type of the response entity
    * @return a reference to this object for the purposes of chaining
    */
   public <T extends Serializable> Responses ifCreated(final GenericType<T> theType, final Consumer<T> theAction) {
      if (this.created()) {
         this.entity(theType).ifPresent(theAction);
      }
      return this;
   }

   /**
    * Functional method that can be implemented with a lambda.  Will only execute {@code theAction} parameter if the wrapped response was {@link Status#CREATED}.
    * Unwraps the entity via {@link ObjectMapper#readValue(String, TypeReference)}, unmarshalls, and provides it to the specified type for the
    * executing function.
    * @param theType the the target entity type
    * @param theAction the function to be executed when the condition is met
    * @param <T> the expected type of the response entity
    * @return a reference to this object for the purposes of chaining
    */
   public <T extends Serializable> Responses ifCreated(final TypeReference<T> theType, final Consumer<T> theAction) {
      if (this.created()) {
         this.entity(theType).ifPresent(theAction);
      }
      return this;
   }


   /**
    * Functional method that can be implemented with 2 lambdas.  Will only execute {@code theAction} parameter if the wrapped response was
    * {@link Status#CREATED}.
    * Unwraps the {@link Response#readEntity(Class)}, unmarshalls, and provides it to the specified class type for the executing function.
    * Otherwise will execute the {@code theElseAction} parameter with the {@link ErrorCode} that is expected within the {@link #response}
    * @param theClass the the target entity type
    * @param theAction the function to be executed when the condition is met
    * @param theElseAction the function to be executed if response is <emphasis>not</emphasis> {@link #created()}
    * @param <T> the expected type of the response entity
    */
   public <T extends Serializable> void ifCreatedOrElse(final Class<T> theClass, final Consumer<T> theAction, final Consumer<ErrorCode<?>> theElseAction) {
      if (this.created()) {
         this.entity(theClass).ifPresent(theAction);
      }
      else {
         this.errorCode().ifPresent(theElseAction);
      }
   }

   /**
    * Functional method that can be implemented with 2 lambdas.  Will only execute {@code theAction} parameter if the wrapped response was
    * {@link Status#CREATED}.
    * Unwraps the {@link Response#readEntity(GenericType)}, unmarshalls, and provides it to the specified class type for the executing function.
    * Otherwise will execute the {@code theElseAction} parameter with the {@link ErrorCode} that is expected within the {@link #response}
    * @param theType the the target entity type
    * @param theAction the function to be executed when the condition is met
    * @param theElseAction the function to be executed if response is <emphasis>not</emphasis> {@link #created()}
    * @param <T> the expected type of the response entity
    */
   public <T extends Serializable> void ifCreatedOrElse(final GenericType<T> theType, final Consumer<T> theAction, final Consumer<ErrorCode<?>> theElseAction) {
      if (this.created()) {
         this.entity(theType).ifPresent(theAction);
      }
      else {
         this.errorCode().ifPresent(theElseAction);
      }
   }

   /**
    * Functional method that can be implemented with 2 lambdas.  Will only execute {@code theAction} parameter if the wrapped response was
    * {@link Status#CREATED}.  Unwraps the entity via {@link ObjectMapper#readValue(String, TypeReference)}, unmarshalls, and provides it to the specified
    * type for the executing function.
    * @param theType the the target entity type
    * @param theAction the function to be executed when the condition is met
    * @param theElseAction the function to be executed if response is <emphasis>not</emphasis> {@link #created()}
    * @param <T> the expected type of the response entity
    */
   public <T extends Serializable> void ifCreatedOrElse(final TypeReference<T> theType, final Consumer<T> theAction,
                                                        final Consumer<ErrorCode<?>> theElseAction) {
      if (this.created()) {
         this.entity(theType).ifPresent(theAction);
      }
      else {
         this.errorCode().ifPresent(theElseAction);
      }
   }

   /**
    * Functional method that can be implemented with 2 lambdas.  Will only execute {@code theAction} parameter if the wrapped response was
    * {@link Status#CREATED}.
    * Unwraps the {@link Response#readEntity(Class)}, unmarshalls, and provides it to the specified class type for the executing function.
    * Otherwise will execute the {@code theElseAction} parameter.
    * @param theClass the the target entity type
    * @param theAction the function to be executed when the condition is met
    * @param theElseAction the runnable to be executed if response is <emphasis>not</emphasis> {@link #created()}
    * @param <T> the expected type of the response entity
    */
   public <T extends Serializable> void ifCreatedOrElse(final Class<T> theClass, final Consumer<T> theAction, final Runnable theElseAction) {
      if (this.created()) {
         this.entity(theClass).ifPresent(theAction);
      }
      else {
         theElseAction.run();
      }
   }

   /**
    * Functional method that can be implemented with 2 lambdas.  Will only execute {@code theAction} parameter if the wrapped response was
    * {@link Status#CREATED}. Unwraps the {@link Response#readEntity(GenericType)}, unmarshalls, and provides it to the specified class type for the
    * executing function. Otherwise will execute the {@code theElseAction} parameter.
    * @param theType the the target entity type
    * @param theAction the function to be executed when the condition is met
    * @param theElseAction the runnable to be executed if response is <emphasis>not</emphasis> {@link #created()}
    * @param <T> the expected type of the response entity
    */
   public <T extends Serializable> void ifCreatedOrElse(final GenericType<T> theType, final Consumer<T> theAction, final Runnable theElseAction) {
      if (this.created()) {
         this.entity(theType).ifPresent(theAction);
      }
      else {
         theElseAction.run();
      }
   }

   /**
    * Functional method that can be implemented with 2 lambdas.  Will only execute {@code theAction} parameter {@code theAction} parameter if the wrapped response was
    * {@link Status#CREATED}.  Unwraps the entity via {@link ObjectMapper#readValue(String, TypeReference)}, unmarshalls, and provides it to the specified
    * type for the executing function. Otherwise will execute the {@code theElseAction} parameter.
    * @param theType the the target entity type
    * @param theAction the function to be executed when the condition is met
    * @param theElseAction the runnable to be executed if response is <emphasis>not</emphasis> {@link #created()}
    * @param <T> the expected type of the response entity
    */
   public <T extends Serializable> void ifCreatedOrElse(final TypeReference<T> theType, final Consumer<T> theAction, final Runnable theElseAction) {
      if (this.created()) {
         this.entity(theType).ifPresent(theAction);
      }
      else {
         theElseAction.run();
      }
   }

   /**
    * Functional method that can be implemented with a lambda.  Will only execute {@code theAction} parameter if the wrapped response was {@link Status#NOT_FOUND}.
    * Unmarshalls the entity, if one is present, into an {@link Enum} that implements {@link ErrorCode}, and provides it to the provided function.
    * @param theAction the function to be executed when the condition is met
    * @return a reference to this object for the purposes of chaining
    */
   public Responses ifNotFound(final Consumer<ErrorCode<?>> theAction) {
      if (this.notFound() && this.response.hasEntity()) {
         this.errorCode().ifPresent(theAction);
      }
      return this;
   }

   /**
    * Functional method that can be implemented with a lambda.  Will only execute {@code theAction} parameter if the wrapped response was {@link Status#NOT_MODIFIED}.
    * Unmarshalls the entity, if one is present, into an {@link Enum} that implements {@link ErrorCode}, and provides it to the provided function.
    * @param theAction the function to be executed when the condition is met
    * @return a reference to this object for the purposes of chaining
    */
   public Responses ifNotModified(final Consumer<ErrorCode<?>> theAction) {
      if (this.notModified() && this.response.hasEntity()) {
         this.errorCode().ifPresent(theAction);
      }
      return this;
   }

   /**
    * Functional method that can be implemented with a lambda.  Will only execute {@code theAction} parameter if the wrapped response was {@link Status#INTERNAL_SERVER_ERROR}.
    * Unmarshalls the entity, if one is present, into an {@link Enum} that implements {@link ErrorCode}, and provides it to the provided function.
    * @param theAction the function to be executed when the condition is met
    * @return a reference to this object for the purposes of chaining
    */
   public Responses ifInternalServerError(final Consumer<ErrorCode<?>> theAction) {
      if (this.internalServerError() && this.response.hasEntity()) {
         this.errorCode().ifPresent(theAction);
      }
      return this;
   }

   /**
    * Functional method that can be implemented with a lambda.  Will only execute {@code theAction} parameter if the wrapped response was {@link Status#BAD_REQUEST}.
    * Unmarshalls the entity, if one is present, into an {@link Enum} that implements {@link ErrorCode}, and provides it to the provided function.
    * @param theAction the function to be executed when the condition is met
    * @return a reference to this object for the purposes of chaining
    */
   public Responses ifBadRequest(final Consumer<ErrorCode<?>> theAction) {
      if (this.badRequest() && this.response.hasEntity()) {
         this.errorCode().ifPresent(theAction);
      }
      return this;
   }

   /**
    * Functional method that can be implemented with a lambda.  Will only execute {@code theAction} parameter if the wrapped response has an entity, regardless of
    * {@link Response#getStatus()}.
    * @param theClass the type of the entity
    * @param theAction the function to be executed only if the wrapped response {@link Response#hasEntity()}
    * @param <T> the expected type of the response entity
    * @return a reference to this object for the purposes of chaining
    */
   public <T extends Serializable> Responses execute(final Class<T> theClass, final Consumer<T> theAction) {
      if (this.response != null && this.response.hasEntity()) {
         this.entity(theClass).ifPresent(theAction);
      }
      return this;
   }

   /**
    * Functional method that can be implemented with a lambda.  Will only execute {@code theAction} parameter if the wrapped response has an entity, regardless of
    * {@link Response#getStatus()}.
    * @param theType the type of the entity
    * @param theAction the function to be executed only if the wrapped response {@link Response#hasEntity()}
    * @param <T> the expected type of the response entity
    * @return a reference to this object for the purposes of chaining
    */
   public <T extends Serializable> Responses execute(final GenericType<T> theType, final Consumer<T> theAction) {
      if (this.response != null && this.response.hasEntity()) {
         this.entity(theType).ifPresent(theAction);
      }
      return this;
   }

   /**
    * Functional method that can be implemented with a lambda.  Will only execute {@code theAction} parameter  if the wrapped response has an entity, regardless of
    * {@link Response#getStatus()}.
    * @param theType the type of the entity
    * @param theAction the function to be executed only if the wrapped response {@link Response#hasEntity()}
    * @param <T> the expected type of the response entity
    * @return a reference to this object for the purposes of chaining
    */
   public <T extends Serializable> Responses execute(final TypeReference<T> theType, final Consumer<T> theAction) {
      if (this.response != null && this.response.hasEntity()) {
         this.entity(theType).ifPresent(theAction);
      }
      return this;
   }

   /**
    * Used to transform the entity associated with the wrapped response into another object using a lambda expression.
    * If response has no entity or was not {@link Status#OK}, then calls mapper with a null.
    * @param theClass the entity type
    * @param theMapper the function to be executed
    * @param <O> the type of the return object
    * @return the transformed object instance
    */
   public <T,O> O map(final Class<T> theClass, @Nonnull final Function<T, O> theMapper) {
      if (this.response.hasEntity() && this.ok()) {
         return theMapper.apply(this.entity(theClass).orElse(null));
      }
      else {
         return theMapper.apply(null);
      }
   }

   /**
    * Used to transform the entity associated with the wrapped response into another object using a lambda expression.
    * If response has no entity or was not {@link Status#OK}, then calls mapper with a null.
    * @param theType the entity type
    * @param theMapper the function to be executed
    * @param <O> the type of the return object
    * @return the transformed object instance
    */
   public <T,O> O map(final GenericType<T> theType, @Nonnull final Function<T, O> theMapper) {
      if (this.response.hasEntity() && this.ok()) {
         return theMapper.apply(this.entity(theType).orElse(null));
      }
      else {
         return theMapper.apply(null);
      }
   }

   /**
    * Used to transform the entity associated with the wrapped response into another object using a lambda expression.
    * If response has no entity or was not {@link Status#OK}, then calls mapper with a null.
    * @param theType the entity type
    * @param theMapper the function to be executed
    * @param <O> the type of the return object
    * @return the transformed object instance
    */
   public <T,O> O map(final TypeReference<T> theType, @Nonnull final Function<T, O> theMapper) {
      if (this.response.hasEntity() && this.ok()) {
         return theMapper.apply(this.entity(theType).orElse(null));
      }
      else {
         return theMapper.apply(null);
      }
   }

   private <T> Optional<T> entity(final Class<T> theClass) {
      if (this.response.hasEntity()) {
         if (this.response.getEntity() instanceof InputStream) {
            return Optional.of(this.response.readEntity(theClass));
         }
         return Optional.of((T) this.response.getEntity());
      }
      return Optional.empty();
   }

   private <T> Optional<T> entity(final GenericType<T> theType) {
      if (this.response.hasEntity()) {
         if (this.response.getEntity() instanceof InputStream) {
            return Optional.of(this.response.readEntity(theType));
         }
         return Optional.of((T) this.response.getEntity());
      }
      return Optional.empty();
   }

   private <T> Optional<T> entity(final TypeReference<T> theType) {
      if (this.response.hasEntity()) {
         if (this.response.getEntity() instanceof InputStream) {
            final var aJSON   = this.response.readEntity(String.class);
            try {
               return Optional.of(objectMapper.readValue(aJSON, theType));
            }
            catch (IOException e) {
               return Optional.empty();
            }
         }
         else {
            return Optional.of((T) this.response.getEntity());
         }
      }
      return Optional.empty();
   }

   @SuppressWarnings("AutoBoxing")
   private Optional<ErrorCode<?>> errorCode() {
      if (this.response.hasEntity()) {
         if (this.response.getEntity() instanceof ErrorCode) {
            return Optional.of((ErrorCode<?>) this.response.getEntity());
         }
         final var anErrorCodeString = this.response.readEntity(String.class);
         if (StringUtils.isEmpty(anErrorCodeString)) {
            return Optional.of(B21040);
         }
         else {
            try {
               final var anUnquotedString = anErrorCodeString.trim().replace("\"", "");
               final var anErrorCodeValue = (ErrorCode<?>) MethodUtils.invokeStaticMethod(errorCodeClass, "valueOf", anUnquotedString);
               if (anErrorCodeValue == null) {
                  throw new ResponseError(B21030);
               }
               return Optional.of(anErrorCodeValue);
            }
            catch (InvocationTargetException ite) {
               try {
                  final var aJAXRSResponse = errorCodeObjectMapper.readValue(anErrorCodeString, JAXRSErrorResponse.class);
                  log.error("Unexpected JAX-RS error; HTTP status: {}; message: {}", aJAXRSResponse.getStatusCode(), aJAXRSResponse.getMessage());
                  return Optional.of(B21010);
               }
               catch (IOException ioe) {
                  log.error("Unable to unmarshall JAX-RS error; Error message: {}", Throwables.getRootCause(ioe).getMessage());
                  return Optional.of(B21020);
               }
            }
            catch (NoSuchMethodException | IllegalAccessException e) {
               log.error("Unable to unmarshall error code; Error message: {}", Throwables.getRootCause(e).getMessage());
               return Optional.of(B21010);
            }
         }
      }
      return Optional.of(B21030);
   }

   /**
    * Returns the static field.  Used for unit testing only.
    * @return the initialized {@link ErrorCode} implementation
    */
   static Class<? extends Enum<?>> getErrorCodeClass() {
      return errorCodeClass;
   }
}