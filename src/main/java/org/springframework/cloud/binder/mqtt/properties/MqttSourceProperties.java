package org.springframework.cloud.binder.mqtt.properties;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * Extended consumer properties for MQTT binder.
 *
 * @author Bao Ngo
 */
@Data
@Validated
public class MqttSourceProperties {

    /**
     * identifies the client
     */
    @NotBlank
    @Size(min = 1, max = 23)
    private String clientId = "stream.client.id.source";
}
