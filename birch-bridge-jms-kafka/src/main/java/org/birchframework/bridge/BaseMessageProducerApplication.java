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
package org.birchframework.bridge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import javax.jms.Message;
import javax.jms.QueueConnectionFactory;
import com.google.common.base.Throwables;
import org.birchframework.framework.cli.AbstractCommandLineApplication;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.jms.JmsMessageType;
import org.apache.camel.spring.boot.SpringBootCamelContext;
import org.apache.commons.cli.Option;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Instant.EPOCH;
import static org.apache.camel.component.jms.JmsMessageType.Text;
import static org.apache.camel.component.jms.JmsMessageType.Bytes;

/**
 * Base class for Spring Boot applications that act as message producers to JMS destinations (currently only supports queues).  Reads command line
 * options and sends messages to specified configurations specified through those options.  Running the concrete subclass application without command line
 * arguments will print usage.
 *
 * This class requires implementation of the abstract methods so as so enrich the messages.  See each method's Javadocs for details.
 */
@ComponentScan(basePackages = "org.springframework.boot.autoconfigure.jackson")
@Slf4j
@SuppressWarnings("AutoBoxing")
public class BaseMessageProducerApplication extends AbstractCommandLineApplication {

   protected static final Option TEMPLATE_FILE_OPTION      = requiredWithArgsOption("t", "template-file", "load-testing template file");
   protected static final Option NUMBER_OF_MESSAGES_OPTION = requiredWithArgsOption("n", "number-of-messages", "number of messages to send");
   protected static final Option QUEUE_OPTION              = requiredWithArgsOption("q", "queue", "queue name");
   protected static final Option APPLY_GROUP_ID_OPTION     = noArgsOption("g", "apply-group-id", "apply group id (boolean)");
   protected static final String JMSXGROUP_ID              = "JMSXGroupID";

   protected static final Set<JmsMessageType> supportedTypes = Set.of(Text, Bytes);
   @SuppressWarnings("unused")
   protected static final ThreadLocal<Object> threadLocal    = new ThreadLocal<>();

   private final JmsMessageType   messageType;
   private final ProducerTemplate producerTemplate;

   protected final ApplicationContext springContext;

   /**
    * Initializes this application for text message body type.
    * @param theContext the Camel context
    */
   public BaseMessageProducerApplication(final SpringBootCamelContext theContext) {
      this(Text, theContext);
   }

   /**
    * Initializes this application for the given message body type.  Currently only {@link JmsMessageType#Text} and {@link JmsMessageType#Bytes} are
    * supported.
    * @param theMessageType the message type
    * @param theContext the Camel context
    */
   public BaseMessageProducerApplication(final JmsMessageType theMessageType, final SpringBootCamelContext theContext) {
      if (!supportedTypes.contains(theMessageType)) {
         throw new RuntimeException(String.format("Message type %s is not supported.", theMessageType));
      }
      this.messageType      = theMessageType;
      this.producerTemplate = theContext.createProducerTemplate();
      this.springContext    = theContext.getApplicationContext();
   }

   @Override
   protected List<Option> options() {
      return List.of(TEMPLATE_FILE_OPTION, NUMBER_OF_MESSAGES_OPTION, QUEUE_OPTION, APPLY_GROUP_ID_OPTION);
   }

   /**
    * Override to initialize variables before iterations loop begins.  Can be used to initialize the thread-local variable
    * {@link #threadLocal}.
    * @param theMessage the message the message as read from source
    */
   @SuppressWarnings("unused")
   protected void before(final String theMessage) {
      // No-op; meant to be overriden if necessary to do so
   }

   /**
    * Override to initialize variables for each iteration of the message producer.  Can be used to initialize the thread-local variable
    * {@link #threadLocal}.
    * @param i the iteration
    * @param theMessage the message the message as read from source
    */
   @SuppressWarnings("unused")
   protected void beforeEach(final int i, final String theMessage) {
      // No-op; meant to be overriden if necessary to do so
   }

   /**
    * Override with a {@link Map} that will be used to populate {@link Message} properties.
    * @param i iteration of messages being sent
    * @param theMessage
    * @return map of properties with string keys and Object values
    */
   protected Map<String, Object> messageProperties(final int i, final String theMessage) {
      return null;
   }

   /**
    * Override to enrich or modify the text message.
    * @param theTextMessage the message created from template
    * @return the enriched or modified text message
    * @throws NullPointerException if the returned string is null
    */
   protected String enhanceTextMessage(final String theTextMessage) {
      return theTextMessage;
   }

   /**
    * Override to produce a JMSXGroupID value.  Highly recommended to override this method when using the {@code -g} option.
    * @return the string used to set the JMSXGroupID property
    */
   protected String jmsXGroupID() {
      return null;
   }

   @Override
   protected void run() {
      final var aTemplateFile     = this.optionValue(TEMPLATE_FILE_OPTION);
      final var aNumberOfMessages = Integer.parseInt(this.optionValue(NUMBER_OF_MESSAGES_OPTION));
      final var aQueueName        = this.optionValue(QUEUE_OPTION);
      final var anApplyGroupID    = this.hasOption(APPLY_GROUP_ID_OPTION);

      final Path aFilePath               = Files.exists(Paths.get(aTemplateFile)) ? Paths.get(aTemplateFile) : null;
      final var  aQueueConnectionFactory = Arrays.stream(springContext.getBeanNamesForType(QueueConnectionFactory.class)).findFirst().orElse(null);
      final var  aComponentURI           = String.format("jms:queue:%s?connectionFactory=%s", aQueueName, aQueueConnectionFactory);
      if (aFilePath == null) {
         log.error("File {} not found", aTemplateFile);
      }
      else {
         try {
            final var aMessage = Files.readString(aFilePath);
            final var aStart = Instant.now();
            this.before(aMessage);
            IntStream.range(0, aNumberOfMessages).parallel().forEach(i -> this.sendJMSMessage(i, aComponentURI, aMessage, anApplyGroupID));
            log.info("Sent {} messages in {} milliseconds", aNumberOfMessages,
                     Duration.between(aStart.adjustInto(EPOCH), Instant.now().adjustInto(EPOCH)).toMillis());
         }
         catch (IOException e) {
            log.error("IO exception; error message: {}", Throwables.getRootCause(e).getMessage());
         }
      }
   }

   private void sendJMSMessage(final int theIteration, final String theComponentURI, final String theMessage, final boolean theApplyJMSXGroupID) {
      this.beforeEach(theIteration, theMessage);
      var aProperties = this.messageProperties(theIteration, theMessage);
      if (theApplyJMSXGroupID) {
         if (aProperties == null) {
            aProperties = new HashMap<>(Map.of(JMSXGROUP_ID, this.jmsXGroupID()));
         }
         else {
            aProperties = new HashMap<>(aProperties);
            aProperties.put(JMSXGROUP_ID, this.jmsXGroupID());
         }
      }
      final var aMessageString = this.enhanceTextMessage(theMessage);
      final Object aMessageBody;
      switch (this.messageType) {
         case Text: aMessageBody = aMessageString; break;
         case Bytes: aMessageBody = aMessageString.getBytes(UTF_8); break;
         default: aMessageBody = null;
      }
      this.producerTemplate.sendBodyAndHeaders(theComponentURI, aMessageBody, aProperties);
      log.info("Sending message iteration {} to {}", theIteration, theComponentURI);
   }
}