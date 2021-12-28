package org.springframework.cloud.binder.mqtt.properties;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * Extended producer properties for MQTT binder.
 *
 * @author Bao Ngo
 */
@Data
@Validated
public class MqttSinkProperties {

  /**
   * identifies the client
   */
  @NotBlank
  @Size(min = 1, max = 23)
  private String clientId = "stream.client.id.sink";
}
