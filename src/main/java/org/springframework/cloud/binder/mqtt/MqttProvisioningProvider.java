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

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.binder.mqtt.properties.MqttSinkProperties;
import org.springframework.cloud.binder.mqtt.properties.MqttSourceProperties;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;

/**
 * MQTT implementation for {@link ProvisioningProvider}.
 *
 * @author Bao Ngo
 */
public class MqttProvisioningProvider
        implements ProvisioningProvider<
        ExtendedConsumerProperties<MqttSourceProperties>,
        ExtendedProducerProperties<MqttSinkProperties>> {

    @Override
    public ProducerDestination provisionProducerDestination(
            String name, ExtendedProducerProperties<MqttSinkProperties> properties)
            throws ProvisioningException {
        return new MqttTopicDestination(name);
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(
            String name, String group, ExtendedConsumerProperties<MqttSourceProperties> properties)
            throws ProvisioningException {
        return new MqttTopicDestination(name);
    }

    @RequiredArgsConstructor
    private class MqttTopicDestination implements ProducerDestination, ConsumerDestination {

        private final String destination;

        @Override
        public String getName() {
            return this.destination.trim();
        }

        @Override
        public String getNameForPartition(int partition) {
            throw new UnsupportedOperationException("Partitioning is not implemented for mqtt");
        }
    }
}
