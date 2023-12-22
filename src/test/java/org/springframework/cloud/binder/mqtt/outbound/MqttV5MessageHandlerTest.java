package org.springframework.cloud.binder.mqtt.outbound;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import com.hivemq.client.internal.mqtt.MqttRxClientBuilder;
import com.hivemq.client.internal.mqtt.MqttRxClientBuilderBase;
import com.hivemq.client.internal.mqtt.message.auth.MqttSimpleAuthBuilder;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublishBuilder;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientBuilder;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.lifecycle.MqttClientAutoReconnectBuilder;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.binder.mqtt.properties.MqttBinderConfigurationProperties;
import org.springframework.cloud.binder.mqtt.properties.MqttSinkProperties;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

@ExtendWith(MockitoExtension.class)
public class MqttV5MessageHandlerTest {

  private static MqttV5MessageHandler mqttV5MessageHandler;
  private static MqttBinderConfigurationProperties configurationProperties;
  private static String clientId;
  private static String topic;

  @BeforeAll
  public static void setUp() {
    configurationProperties = new MqttBinderConfigurationProperties();
    clientId = "client";
    topic = "topic";
    MqttSinkProperties sinkProperties = new MqttSinkProperties();
    sinkProperties.setClientId(clientId);
    mqttV5MessageHandler = new MqttV5MessageHandler(configurationProperties, sinkProperties, topic);
  }

  @Test
  public void onInit_shouldWork() {
    MockedStatic<MqttClient> mqttClientMockedStatic = Mockito.mockStatic(MqttClient.class);
    Mqtt5BlockingClient mqtt5BlockingClient =
        Mockito.mock(Mqtt5BlockingClient.class, withSettings().defaultAnswer(inv -> null));
    initMockForMqttClient(mqttClientMockedStatic, mqtt5BlockingClient);

    mqttV5MessageHandler.onInit();

    verify(mqtt5BlockingClient).connect();

    mqttClientMockedStatic.close();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void handleMessageInternal_shouldWork() {
    MockedStatic<MqttClient> mqttClientMockedStatic = Mockito.mockStatic(MqttClient.class);
    Mqtt5BlockingClient mqtt5BlockingClient =
        Mockito.mock(Mqtt5BlockingClient.class, withSettings().defaultAnswer(inv -> null));
    initMockForMqttClient(mqttClientMockedStatic, mqtt5BlockingClient);

    MqttPublishBuilder.Send<Mqtt5PublishResult> mqttPublishBuilder =
        Mockito.mock(MqttPublishBuilder.Send.class);

    Mockito.doReturn(mqttPublishBuilder).when(mqtt5BlockingClient).publishWith();
    Mockito.doReturn(mqttPublishBuilder).when(mqttPublishBuilder).topic(topic);
    Mockito.doReturn(mqttPublishBuilder).when(mqttPublishBuilder).qos(MqttQos.AT_LEAST_ONCE);
    Mockito.doReturn(mqttPublishBuilder)
        .when(mqttPublishBuilder)
        .payload(Mockito.any(byte[].class));
    Mockito.doReturn(mqttPublishBuilder).when(mqttPublishBuilder).contentType((String) null);

    mqttV5MessageHandler.onInit();
    mqttV5MessageHandler.handleMessageInternal(buildMessage());

    verify(mqttPublishBuilder).send();

    mqttClientMockedStatic.close();
  }

  private Message<?> buildMessage() {
    return MessageBuilder.withPayload("payload").build();
  }

  @SuppressWarnings("unchecked")
  private void initMockForMqttClient(
      MockedStatic<MqttClient> mqttClientMockedStatic, Mqtt5BlockingClient mqtt5BlockingClient) {
    MqttClientBuilder mqttClientBuilder = Mockito.mock(MqttClientBuilder.class);
    Mqtt5ClientBuilder mqtt5ClientBuilder =
        Mockito.mock(
            Mqtt5ClientBuilder.class, withSettings().defaultAnswer(inv -> mqtt5BlockingClient));
    MqttClientAutoReconnectBuilder.Nested<MqttRxClientBuilderBase.Choose> autoReconnectBuilder =
        Mockito.mock(MqttClientAutoReconnectBuilder.Nested.class);
    MqttSimpleAuthBuilder.Nested<MqttRxClientBuilder> simpleAuthBuilder =
        Mockito.mock(MqttSimpleAuthBuilder.Nested.class);

    Mockito.doReturn(mqttClientBuilder).when(mqttClientBuilder).sslConfig(null);
    Mockito.doReturn(autoReconnectBuilder).when(mqttClientBuilder).automaticReconnect();
    Mockito.doReturn(autoReconnectBuilder)
        .when(autoReconnectBuilder)
        .initialDelay(500, TimeUnit.MILLISECONDS);
    Mockito.doReturn(autoReconnectBuilder).when(autoReconnectBuilder).maxDelay(2, TimeUnit.MINUTES);
    Mockito.doReturn(mqttClientBuilder).when(autoReconnectBuilder).applyAutomaticReconnect();

    Mockito.doReturn(mqttClientBuilder).when(mqttClientBuilder).identifier(clientId);
    Mockito.doReturn(mqttClientBuilder)
        .when(mqttClientBuilder)
        .serverHost(configurationProperties.getServerHost());
    Mockito.doReturn(mqttClientBuilder)
        .when(mqttClientBuilder)
        .serverPort(configurationProperties.getServerPort());
    Mockito.doReturn(mqtt5ClientBuilder).when(mqttClientBuilder).useMqttVersion5();
    Mockito.doReturn(mqtt5ClientBuilder)
        .when(mqtt5ClientBuilder)
        .addConnectedListener(Mockito.any());
    Mockito.doReturn(mqtt5ClientBuilder)
        .when(mqtt5ClientBuilder)
        .addDisconnectedListener(Mockito.any());

    Mockito.doReturn(simpleAuthBuilder).when(mqtt5ClientBuilder).simpleAuth();
    Mockito.doReturn(simpleAuthBuilder)
        .when(simpleAuthBuilder)
        .username(configurationProperties.getUsername());
    Mockito.doReturn(simpleAuthBuilder)
        .when(simpleAuthBuilder)
        .password(configurationProperties.getPassword().getBytes());
    Mockito.doReturn(mqtt5ClientBuilder).when(simpleAuthBuilder).applySimpleAuth();

    mqttClientMockedStatic.when(MqttClient::builder).thenReturn(mqttClientBuilder);
  }
}
