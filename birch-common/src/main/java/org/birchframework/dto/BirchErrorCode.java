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
package org.birchframework.dto;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import org.birchframework.framework.jackson.ErrorCodeDeserializer;
import org.springframework.lang.NonNull;

import static org.birchframework.dto.BirchComponent.*;
import static javax.xml.bind.annotation.XmlAccessType.PROPERTY;

/**
 * Enumerations representing various error codes and descriptions.
 * @author Keivan Khalichi
 */
@XmlAccessorType(PROPERTY)
@XmlType(propOrder = {
        "code",
        "component",
        "description"
})
@XmlEnum
@JsonDeserialize(using = ErrorCodeDeserializer.class)
@Getter
public enum BirchErrorCode implements ErrorCode<BirchErrorCode> {
   // Common
   B10120(COMMON, "Exception stubbing data"),
   B10030(COMMON, "Unable to find implementation"),

   B12000(PARSER, "Parser not configured properly"),
   B12010(PARSER, "Parse types list is empty"),
   B12020(PARSER, "Input is null or blank"),
   B12030(PARSER, "Error setting value of capture group"),
   B12040(PARSER, "Duplicate capture groups defined in the target class"),
   B12045(PARSER, "Invalid regular expression definition and/or number of capture groups"),
   B12100(PARSER, "JMS message to be parsed in null or blank"),
   B12110(PARSER, "Error parsing JMS message"),

   B20100(JAXB, "JSON Unmarshalling error"),
   B20105(JAXB, "JSON Unmarshalling error; file not found"),
   B20110(JAXB, "JSON Marshalling error"),
   B20120(JAXB, "JAXB Unmarshalling error"),
   B20130(JAXB, "JAXB Unmarshalling error; file not found"),
   B20140(JAXB, "JAXB Marshalling error"),
   B20150(JAXB, "Error marshalling Payload"),

   B21000(JAXRS, "Invalid response state"),
   B21010(JAXRS, "Unable to unmarshal error code"),
   B21020(JAXRS, "Unable to unmarshal JAXRS error response"),
   B21030(JAXRS, "No known error code was returned within the response"),
   B21040(JAXRS, "Entity contained no serialized value"),

   B31000(BRIDGE, "Invalid configuration; source type value not supported"),
   B31010(BRIDGE, "Invalid configuration; source is missing"),
   B31020(BRIDGE, "Invalid configuration; no JMS destination (queue or topic) defined"),
   B31021(BRIDGE, "Invalid configuration; both queue and topic are defined"),
   B31023(BRIDGE, "Invalid configuration; no topic listener container factory found"),
   B31025(BRIDGE, "Invalid configuration; no queue listener container factory found"),
   B31027(BRIDGE, "Invalid configuration; no topic connection factory found"),
   B31029(BRIDGE, "Invalid configuration; no queue connection factory found"),
   B31030(BRIDGE, "Invalid configuration; source JMS destination is null or blank"),
   B31031(BRIDGE, "Invalid configuration; source JMS queue and dead letter queue are the same"),
   B31032(BRIDGE, "Invalid configuration; both key-property and key-regex are defined"),
   B31035(BRIDGE, "Invalid configuration; target Kafka topic is null or blank"),
   B31040(BRIDGE, "Invalid configuration; source Kafka topic is null or blank"),
   B31043(BRIDGE, "Invalid configuration; source Kafka topic and dead letter copy are the same"),
   B31045(BRIDGE, "Invalid configuration; target JMS destination is null or blank"),
   B31046(BRIDGE, "Invalid configuration; message type is not supported"),
   B31047(BRIDGE, "Error attempting to register Payload DataFormat service"),
   B31048(BRIDGE, "Error attempting to create Camel route"),
   B31050(BRIDGE, "Unable to retrieve JMS message body"),
   B31060(BRIDGE, "Error attempting to set JMS message property"),
   B31070(BRIDGE, "Key property {} not found in source message header"),
   B31080(BRIDGE, "Key property {} in JMS header is blank"),
   B31085(BRIDGE, "Key property {} in JMS payload is blank"),
   B31090(BRIDGE, "Error attempting to send JMS message from Kafka consumer"),
   B31100(BRIDGE, "Error attempting to send Kafka message from JMS consumer"),
   B31105(BRIDGE, "Error attempting to send Kafka message from JMS consumer future"),
   B31110(BRIDGE, "Unable to resolve destination while configuring the EMS bridge"),
   B31120(BRIDGE, "Error attempting to send JMS message from Kafka consumer, asynchronously"),
   B31130(BRIDGE, "Error attempting to serialize JMS ObjectMessage to JSON"),

   B43000(OAUTH2, "Error occurred during initialization of authenticator manager for OAuth2 resource server"),
   B43010(OAUTH2, "Error occurred during initialization of SSL trust all strategy"),
   B43020(OAUTH2, "User name claim is not provided or its value is null");

   /** Component in which error occurred */
   private final Component component;

   /** Integral value of the error code extracted from enumeration value */
   private final int code;

   /** Error description */
   private final String description;

   /**
    * Construct immutable enum values.
    * @param theComponent component
    * @param theDescription description
    */
   BirchErrorCode(@NonNull final BirchComponent theComponent, @NonNull final String theDescription) {
      this.component = theComponent;
      this.code = Integer.parseInt(this.name().substring(1));
      this.description = theDescription;
   }

   /** {@inheritDoc} */
   @Override
   public ErrorResponse<BirchErrorCode> errorResponse() {
      return new ErrorResponse<>(this);
   }
}