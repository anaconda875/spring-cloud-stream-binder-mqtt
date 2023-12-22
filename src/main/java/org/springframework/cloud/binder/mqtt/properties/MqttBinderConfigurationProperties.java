package org.springframework.cloud.binder.mqtt.properties;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Data
@Validated
@ConfigurationProperties(prefix = "spring.cloud.stream.mqtt.binder")
public class MqttBinderConfigurationProperties {

  private String username = "guest";

  private String password = "guest";

  private int connectionTimeout = 30;

  private String serverHost = "localhost";

  private Integer serverPort = 1883;

  private String keyPath;

  private String keyType;

  private String keyPassword;
}
