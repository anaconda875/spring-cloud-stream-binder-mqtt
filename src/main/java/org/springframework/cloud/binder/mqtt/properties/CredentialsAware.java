package org.springframework.cloud.binder.mqtt.properties;

public interface CredentialsAware {
  void setUsername(String username);

  void setPassword(String password);
}
