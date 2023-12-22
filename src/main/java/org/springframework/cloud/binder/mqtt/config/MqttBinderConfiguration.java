package org.springframework.cloud.binder.mqtt.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.binder.mqtt.MqttBinder;
import org.springframework.cloud.binder.mqtt.MqttProvisioningProvider;
import org.springframework.cloud.binder.mqtt.properties.MqttBinderConfigurationProperties;
import org.springframework.cloud.binder.mqtt.properties.MqttExtendedBindingProperties;
import org.springframework.cloud.binder.mqtt.properties.RuntimeMqttExtendedBindingProperties;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnMissingBean(Binder.class)
@EnableConfigurationProperties({
  MqttExtendedBindingProperties.class,
  RuntimeMqttExtendedBindingProperties.class,
  MqttBinderConfigurationProperties.class
})
public class MqttBinderConfiguration {

  private final MqttExtendedBindingProperties mqttExtendedBindingProperties;
  private final RuntimeMqttExtendedBindingProperties runtimeExtendedBindingProperties;

  public MqttBinderConfiguration(
      MqttExtendedBindingProperties mqttExtendedBindingProperties,
      RuntimeMqttExtendedBindingProperties runtimeExtendedBindingProperties) {
    this.mqttExtendedBindingProperties = mqttExtendedBindingProperties;
    this.runtimeExtendedBindingProperties = runtimeExtendedBindingProperties;
  }

  @Bean
  public MqttProvisioningProvider provisioningProvider() {
    return new MqttProvisioningProvider();
  }

  @Bean
  public MqttBinder mqttBinder(
      MqttProvisioningProvider provisioningProvider,
      MqttBinderConfigurationProperties mqttProperties) {

    return new MqttBinder(
        provisioningProvider,
        mqttExtendedBindingProperties,
        runtimeExtendedBindingProperties,
        mqttProperties);
  }
}
