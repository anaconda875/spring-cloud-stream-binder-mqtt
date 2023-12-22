package org.springframework.cloud.binder.mqtt.inbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import com.hivemq.client.internal.mqtt.MqttRxClientBuilder;
import com.hivemq.client.internal.mqtt.MqttRxClientBuilderBase;
import com.hivemq.client.internal.mqtt.message.auth.MqttSimpleAuthBuilder;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientBuilder;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopic;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.lifecycle.MqttClientAutoReconnectBuilder;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeBuilder;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.cloud.binder.mqtt.properties.MqttBinderConfigurationProperties;
import org.springframework.messaging.Message;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MqttV5MessageDrivenChannelAdapterTest {

  private static MqttV5MessageDrivenChannelAdapter adapter;
  private static MqttBinderConfigurationProperties configurationProperties;
  private static String clientId;
  private static String topic;

  @BeforeAll
  public static void setUp() {
    configurationProperties = new MqttBinderConfigurationProperties();
    clientId = "client";
    topic = "topic";
    adapter =
        Mockito.spy(
            new MqttV5MessageDrivenChannelAdapter(configurationProperties, clientId, topic));
  }

  @Test
  public void onInit_shouldWork() {
    Mqtt5BlockingClient mqtt5BlockingClient =
        Mockito.mock(Mqtt5BlockingClient.class, withSettings().defaultAnswer(inv -> null));
    MockedStatic<MqttClient> mqttClientMockedStatic = Mockito.mockStatic(MqttClient.class);
    initMockForMqttClient(mqttClientMockedStatic, mqtt5BlockingClient);

    adapter.onInit();

    verify(mqtt5BlockingClient).connect();

    mqttClientMockedStatic.close();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void doStart_shouldWork() throws InterruptedException {
    Mqtt5BlockingClient mqtt5BlockingClient =
        Mockito.mock(Mqtt5BlockingClient.class, withSettings().defaultAnswer(inv -> null));
    MockedStatic<MqttClient> mqttClientMockedStatic = Mockito.mockStatic(MqttClient.class);
    initMockForMqttClient(mqttClientMockedStatic, mqtt5BlockingClient);

    Mqtt5SubscribeBuilder.Send.Start<Mqtt5SubAck> mqtt5SubscribeBuilder =
        Mockito.mock(Mqtt5SubscribeBuilder.Send.Start.class);
    Mqtt5SubscribeBuilder.Send.Start.Complete<?> complete =
        Mockito.mock(Mqtt5SubscribeBuilder.Send.Start.Complete.class);
    Mqtt5SubscribeBuilder.Send.Start.Complete<?> completeBuilder =
        Mockito.mock(Mqtt5SubscribeBuilder.Send.Start.Complete.class);
    Mqtt5BlockingClient.Mqtt5Publishes publishes =
        Mockito.mock(Mqtt5BlockingClient.Mqtt5Publishes.class);
    Mqtt5Publish mqtt5Publish = Mockito.mock(Mqtt5Publish.class);

    Mockito.doReturn(mqtt5SubscribeBuilder).when(mqtt5BlockingClient).subscribeWith();
    Mockito.doReturn(complete).when(mqtt5SubscribeBuilder).topicFilter(topic);
    Mockito.doReturn(completeBuilder).when(complete).qos(MqttQos.AT_LEAST_ONCE);

    Mockito.doReturn(publishes)
        .when(mqtt5BlockingClient)
        .publishes(MqttGlobalPublishFilter.SUBSCRIBED);
    Mockito.doReturn(Optional.of(mqtt5Publish), Optional.empty()).when(publishes).receiveNow();
    Mockito.doReturn(Mockito.mock(MqttQos.class, withSettings().defaultAnswer(inv -> 1)))
        .when(mqtt5Publish)
        .getQos();
    Mockito.doReturn(Mockito.mock(MqttTopic.class, withSettings().defaultAnswer(inv -> "topic")))
        .when(mqtt5Publish)
        .getTopic();
    Mockito.doReturn(Optional.empty()).when(mqtt5Publish).getResponseTopic();
    Mockito.doReturn(Optional.empty()).when(mqtt5Publish).getCorrelationData();
    Mockito.doReturn(
            Optional.of(
                Mockito.mock(MqttUtf8String.class, withSettings().defaultAnswer(inv -> null))))
        .when(mqtt5Publish)
        .getContentType();
    Mockito.doReturn("payload".getBytes()).when(mqtt5Publish).getPayloadAsBytes();
    Mockito.doReturn(false).when(mqtt5Publish).isRetain();
    Mockito.doNothing().when(adapter).sendMessage(Mockito.any(Message.class));
    ArgumentCaptor<Message<String>> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);

    adapter.onInit();
    adapter.doStart();

    Thread.sleep(250);
    adapter.doStop();

    verify(adapter).sendMessage(messageArgumentCaptor.capture());
    assertThat(messageArgumentCaptor.getValue().getPayload()).isEqualTo("payload");

    mqttClientMockedStatic.close();
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
