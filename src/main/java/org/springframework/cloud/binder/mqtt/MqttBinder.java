/*
 * Copyright 2016-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.binder.mqtt;

import org.springframework.cloud.binder.mqtt.inbound.MqttV5MessageDrivenChannelAdapter;
import org.springframework.cloud.binder.mqtt.outbound.MqttV5MessageHandler;
import org.springframework.cloud.binder.mqtt.properties.MqttBinderConfigurationProperties;
import org.springframework.cloud.binder.mqtt.properties.MqttExtendedBindingProperties;
import org.springframework.cloud.binder.mqtt.properties.MqttSinkProperties;
import org.springframework.cloud.binder.mqtt.properties.MqttSourceProperties;
import org.springframework.cloud.stream.binder.*;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

/**
 * A {@link org.springframework.cloud.stream.binder.Binder} that uses MQTT as the
 * underlying middleware.
 *
 * @author Bao Ngo
 */
public class MqttBinder
        extends AbstractMessageChannelBinder<
        ExtendedConsumerProperties<MqttSourceProperties>,
        ExtendedProducerProperties<MqttSinkProperties>,
        MqttProvisioningProvider>
        implements ExtendedPropertiesBinder<MessageChannel, MqttSourceProperties, MqttSinkProperties> {

    private MqttExtendedBindingProperties extendedBindingProperties;
    private MqttBinderConfigurationProperties binderConfigurationProperties;

    public MqttBinder(
            MqttProvisioningProvider provisioningProvider,
            MqttExtendedBindingProperties extendedBindingProperties,
            MqttBinderConfigurationProperties binderConfigurationProperties) {
        super(BinderHeaders.STANDARD_HEADERS, provisioningProvider);
        this.extendedBindingProperties = extendedBindingProperties;
        this.binderConfigurationProperties = binderConfigurationProperties;
    }

    @Override
    protected MessageHandler createProducerMessageHandler(
            ProducerDestination destination,
            ExtendedProducerProperties<MqttSinkProperties> producerProperties,
            MessageChannel errorChannel) {

        MqttSinkProperties sinkProperties = producerProperties.getExtension();
        MqttV5MessageHandler handler =
                new MqttV5MessageHandler(
                        binderConfigurationProperties, sinkProperties.getClientId(), destination.getName());

        return handler;
    }

    @Override
    protected MessageProducer createConsumerEndpoint(
            ConsumerDestination destination,
            String group,
            ExtendedConsumerProperties<MqttSourceProperties> properties) {

        MqttSourceProperties sourceProperties = properties.getExtension();
        MqttV5MessageDrivenChannelAdapter adapter =
                new MqttV5MessageDrivenChannelAdapter(
                        binderConfigurationProperties, sourceProperties.getClientId(), destination.getName());

        return adapter;
    }

    @Override
    public MqttSourceProperties getExtendedConsumerProperties(String channelName) {
        return this.extendedBindingProperties.getExtendedConsumerProperties(channelName);
    }

    @Override
    public MqttSinkProperties getExtendedProducerProperties(String channelName) {
        return this.extendedBindingProperties.getExtendedProducerProperties(channelName);
    }

    @Override
    public String getDefaultsPrefix() {
        return this.extendedBindingProperties.getDefaultsPrefix();
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return this.extendedBindingProperties.getExtendedPropertiesEntryClass();
    }
}
