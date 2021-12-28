package org.springframework.cloud.binder.mqtt.properties;

import lombok.Data;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

/**
 * Container object for MQTT specific extended producer and consumer binding properties.
 *
 * @author Bao Ngo
 */
@Data
public class MqttBindingProperties implements BinderSpecificPropertiesProvider {

  private MqttSourceProperties consumer = new MqttSourceProperties();
  private MqttSinkProperties producer = new MqttSinkProperties();
}
