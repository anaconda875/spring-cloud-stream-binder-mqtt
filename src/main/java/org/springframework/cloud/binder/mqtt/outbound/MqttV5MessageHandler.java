package org.springframework.cloud.binder.mqtt.outbound;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.TrustManagerFactory;
import org.springframework.cloud.binder.mqtt.properties.MqttBinderConfigurationProperties;
import org.springframework.cloud.binder.mqtt.properties.MqttSinkProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

public class MqttV5MessageHandler extends AbstractMessageHandler {

  private Mqtt5BlockingClient mqtt5BlockingClient;
  private MqttBinderConfigurationProperties configurationProperties;
  private ObjectMapper objectMapper;
  private MqttSinkProperties sinkProperties;
  private String topic;

  public MqttV5MessageHandler(
      MqttBinderConfigurationProperties configurationProperties,
      MqttSinkProperties sinkProperties,
      String topic) {
    this(configurationProperties, new ObjectMapper(), sinkProperties, topic);
  }

  public MqttV5MessageHandler(
      MqttBinderConfigurationProperties configurationProperties,
      ObjectMapper objectMapper,
      MqttSinkProperties sinkProperties,
      String topic) {
    this.configurationProperties = configurationProperties;
    this.objectMapper = objectMapper;
    this.sinkProperties = sinkProperties;
    this.topic = topic;
  }

  @Override
  protected void onInit() {
    super.onInit();
    String username = sinkProperties.getUsername();
    String password = sinkProperties.getPassword();
    if (username == null || password == null) {
      username = configurationProperties.getUsername();
      password = configurationProperties.getPassword();
    }

    try {
      MqttClientSslConfig sslConfig = null;
      if (configurationProperties.getKeyPath() != null) {
        TrustManagerFactory trustMgrFact =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore trustStore = KeyStore.getInstance(configurationProperties.getKeyType());
        trustStore.load(
            new ClassPathResource(configurationProperties.getKeyPath()).getInputStream(),
            configurationProperties.getKeyPassword() == null
                ? null
                : configurationProperties.getKeyPassword().toCharArray());
        trustMgrFact.init(trustStore);
        sslConfig =
            MqttClientSslConfig.builder()
                .trustManagerFactory(trustMgrFact)
                .keyManagerFactory(null)
                .build();
      }

      mqtt5BlockingClient =
          MqttClient.builder()
              .sslConfig(sslConfig)
              .automaticReconnect()
              .initialDelay(500, TimeUnit.MILLISECONDS)
              .maxDelay(2, TimeUnit.MINUTES)
              .applyAutomaticReconnect()
              .identifier(sinkProperties.getClientId())
              .serverHost(configurationProperties.getServerHost())
              .serverPort(configurationProperties.getServerPort())
              .useMqttVersion5()
              .addConnectedListener(System.out::println)
              .addDisconnectedListener(c -> c.getCause().printStackTrace())
              .simpleAuth()
              .username(username)
              .password(password.getBytes(StandardCharsets.UTF_8))
              .applySimpleAuth()
              .buildBlocking();

      mqtt5BlockingClient.connect();
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  protected void handleMessageInternal(Message<?> message) {
    Object payload = message.getPayload();
    try {
      byte[] body = parsePayload(payload);
      MessageHeaders headers = message.getHeaders();
      String topic = this.topic;
      String topicFromHeader = headers.get("mqtt_topic", String.class);
      if (topicFromHeader != null) {
        topic = topicFromHeader;
      }
      publish(topic, body, headers);
    } catch (JsonProcessingException e) {
      logger.error(e, "Could not parse payload of type " + payload.getClass() + " to json string");
    }
  }

  private void publish(String topic, byte[] body, MessageHeaders messageHeaders) {
    mqtt5BlockingClient
        .publishWith()
        .topic(topic)
        .qos(MqttQos.AT_LEAST_ONCE)
        .payload(body)
        .contentType(getContentType(messageHeaders))
        .send();
  }

  private String getContentType(MessageHeaders messageHeaders) {
    Object contentType = messageHeaders.get("contentType", Object.class);

    return Optional.ofNullable(contentType)
        .map(
            c -> {
              if (c instanceof String) {
                return (String) c;
              }
              return c.toString();
            })
        .orElse(null);
  }

  private byte[] parsePayload(Object payload) throws JsonProcessingException {
    byte[] body;
    if (payload instanceof byte[]) {
      body = (byte[]) payload;
    } else if (payload instanceof String) {
      body = ((String) payload).getBytes(StandardCharsets.UTF_8);
    } else {
      body = parsePayload(objectMapper.writeValueAsString(payload));
    }

    return body;
  }
}
