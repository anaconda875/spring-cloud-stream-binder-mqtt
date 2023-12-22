package org.springframework.cloud.binder.mqtt.properties;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.binder.AbstractExtendedBindingProperties;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

@ConfigurationProperties("application.cloud.stream.mqtt")
public class RuntimeMqttExtendedBindingProperties
    extends AbstractExtendedBindingProperties<
        MqttSourceProperties, MqttSinkProperties, MqttBindingProperties> {

  private static final String DEFAULTS_PREFIX = "application.cloud.stream.mqtt.default";

  @Override
  public String getDefaultsPrefix() {
    return DEFAULTS_PREFIX;
  }

  @Override
  public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
    return MqttBindingProperties.class;
  }

  @Override
  public Map<String, MqttBindingProperties> getBindings() {
    return this.doGetBindings();
  }
}
