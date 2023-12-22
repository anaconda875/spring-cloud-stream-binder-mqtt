package org.springframework.cloud.binder.mqtt.properties;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class MqttSourceProperties {

  @NotBlank
  @Size(min = 1, max = 23)
  private String clientId = "stream.client.id.source";
}
