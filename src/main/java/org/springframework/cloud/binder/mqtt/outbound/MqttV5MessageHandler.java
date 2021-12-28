package org.springframework.cloud.binder.mqtt.outbound;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import org.springframework.cloud.binder.mqtt.properties.MqttBinderConfigurationProperties;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of {@link MessageHandler}.
 *
 * @author Bao Ngo
 */
public class MqttV5MessageHandler extends AbstractMessageHandler {

  private Mqtt5BlockingClient mqtt5BlockingClient;
  private MqttBinderConfigurationProperties configurationProperties;
  private ObjectMapper objectMapper;
  private String clientId;
  private String topic;

  public MqttV5MessageHandler(
      MqttBinderConfigurationProperties configurationProperties, String clientId, String topic) {
    this(configurationProperties, new ObjectMapper(), clientId, topic);
  }

  public MqttV5MessageHandler(
      MqttBinderConfigurationProperties configurationProperties,
      ObjectMapper objectMapper,
      String clientId,
      String topic) {
    this.configurationProperties = configurationProperties;
    this.objectMapper = objectMapper;
    this.clientId = clientId;
    this.topic = topic;
  }

  @Override
  protected void onInit() {
    super.onInit();
    mqtt5BlockingClient =
        MqttClient.builder()
            .automaticReconnect()
            .initialDelay(500, TimeUnit.MILLISECONDS)
            .maxDelay(2, TimeUnit.MINUTES)
            .applyAutomaticReconnect()
            .identifier(clientId)
            .serverHost(configurationProperties.getServerHost())
            .serverPort(configurationProperties.getServerPort())
            .useMqttVersion5()
            .simpleAuth()
            .username(configurationProperties.getUsername())
            .password(configurationProperties.getPassword().getBytes())
            .applySimpleAuth()
            .buildBlocking();

    mqtt5BlockingClient.connect();
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
        .contentType(messageHeaders.get("contentType", String.class))
        .send();
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
