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

package org.springframework.cloud.binder.mqtt.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.binder.mqtt.MqttBinder;
import org.springframework.cloud.binder.mqtt.MqttProvisioningProvider;
import org.springframework.cloud.binder.mqtt.properties.MqttBinderConfigurationProperties;
import org.springframework.cloud.binder.mqtt.properties.MqttExtendedBindingProperties;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MQTT binder configuration class.
 *
 * @author Bao Ngo
 */
@Configuration
@ConditionalOnMissingBean(Binder.class)
@EnableConfigurationProperties({
        MqttExtendedBindingProperties.class,
        MqttBinderConfigurationProperties.class
})
@RequiredArgsConstructor
public class MqttBinderConfiguration {

    private final MqttExtendedBindingProperties mqttExtendedBindingProperties;

    @Bean
    public MqttProvisioningProvider provisioningProvider() {
        return new MqttProvisioningProvider();
    }

    @Bean
    public MqttBinder mqttBinder(
            MqttProvisioningProvider provisioningProvider,
            MqttBinderConfigurationProperties mqttProperties) {

        return new MqttBinder(provisioningProvider, mqttExtendedBindingProperties, mqttProperties);
    }
}
