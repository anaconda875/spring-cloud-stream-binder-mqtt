package org.springframework.cloud.binder.mqtt.properties;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the MQTT binder. The properties in this class are
 * prefixed with <b>spring.cloud.stream.mqtt.binder</b>.
 *
 * @author Bao Ngo
 */
@Slf4j
@Data
@Validated
@ConfigurationProperties(prefix = "spring.cloud.stream.mqtt.binder")
public class MqttBinderConfigurationProperties {

  private String username = "guest";

  private String password = "guest";

  private String serverHost = "localhost";

  private Integer serverPort = 1883;
}
