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

package org.birchframework.bridge.jmstemplate;

import java.time.Duration;
import javax.jms.ConnectionFactory;
import javax.jms.TopicConnectionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.destination.DestinationResolver;

/**
 * Configures JMS templates given different {@link ConnectionFactory} instances.  Mainly used to configure instances of {@link JmsTemplate}s
 * for queues vs topics, based on their respective connection factories.
 * @author Keivan Khalichi
 */
@Configuration
@ConditionalOnClass(JmsTemplate.class)
@ConditionalOnProperty(prefix = "spring.jms", name = "template")
@EnableConfigurationProperties(JmsProperties.class)
@RefreshScope
@SuppressWarnings("SpringFacetCodeInspection")
public class JMSTemplateConfigurer {

   private final PropertyMapper                      mapper = PropertyMapper.get();
   private final JmsProperties                       jmsProperties;
   private final ObjectProvider<MessageConverter>    messageConverter;
   private final ObjectProvider<DestinationResolver> destinationResolver;

   public JMSTemplateConfigurer(final JmsProperties theJmsProperties,
                                final ObjectProvider<MessageConverter> theMessageConverter,
                                final ObjectProvider<DestinationResolver> theDestinationResolver) {
      this.jmsProperties       = theJmsProperties;
      this.messageConverter    = theMessageConverter;
      this.destinationResolver = theDestinationResolver;
   }

   @SuppressWarnings("AutoBoxing")
   public void configureJMSTemplate(final JmsTemplate theJMSTemplate) {
      final var aTemplateProperties = this.jmsProperties.getTemplate();
      theJMSTemplate.setPubSubDomain(this.jmsProperties.isPubSubDomain());
      mapper.from(this.destinationResolver::getIfUnique).whenNonNull().to(theJMSTemplate::setDestinationResolver);
      mapper.from(this.messageConverter::getIfUnique).whenNonNull().to(theJMSTemplate::setMessageConverter);
      mapper.from(aTemplateProperties::getDefaultDestination).whenNonNull().to(theJMSTemplate::setDefaultDestinationName);
      mapper.from(aTemplateProperties::getDeliveryDelay).whenNonNull().as(Duration::toMillis).to(theJMSTemplate::setDeliveryDelay);
      mapper.from(aTemplateProperties::determineQosEnabled).to(theJMSTemplate::setExplicitQosEnabled);
      mapper.from(aTemplateProperties::getDeliveryMode).whenNonNull().as(JmsProperties.DeliveryMode::getValue).to(theJMSTemplate::setDeliveryMode);
      mapper.from(aTemplateProperties::getPriority).whenNonNull().to(theJMSTemplate::setPriority);
      mapper.from(aTemplateProperties::getTimeToLive).whenNonNull().as(Duration::toMillis).to(theJMSTemplate::setTimeToLive);
      mapper.from(aTemplateProperties::getReceiveTimeout).whenNonNull().as(Duration::toMillis).to(theJMSTemplate::setReceiveTimeout);
      mapper.from(() -> theJMSTemplate.getConnectionFactory() instanceof TopicConnectionFactory).to(theJMSTemplate::setPubSubDomain);
   }
}