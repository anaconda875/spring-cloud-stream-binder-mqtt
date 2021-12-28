/*
 * Copyright 2016-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.binder.mqtt.inbound;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopic;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.springframework.cloud.binder.mqtt.properties.MqttBinderConfigurationProperties;
import org.springframework.cloud.binder.mqtt.support.JsonStringMessageConverter;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.ContentTypeResolver;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.SmartMessageConverter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of {@link MessageProducer}.
 *
 * @author Bao Ngo
 */
public class MqttV5MessageDrivenChannelAdapter extends MessageProducerSupport {

    private Mqtt5BlockingClient mqtt5BlockingClient;
    private MqttBinderConfigurationProperties configurationProperties;
    private String clientId;
    private String topic;
    private ExecutorService executorService;
    private ContentTypeResolver contentTypeResolver;
    private volatile boolean running;

    public MqttV5MessageDrivenChannelAdapter(
            MqttBinderConfigurationProperties configurationProperties, String clientId, String topic) {
        this.configurationProperties = configurationProperties;
        this.clientId = clientId;
        this.topic = topic;
        this.contentTypeResolver = new DefaultContentTypeResolver();
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
    protected void doStart() {
        executorService = Executors.newSingleThreadExecutor();
        running = true;

        Runnable runnable =
                () -> {
                    mqtt5BlockingClient.subscribeWith().topicFilter(topic).qos(MqttQos.AT_LEAST_ONCE).send();
                    final Mqtt5BlockingClient.Mqtt5Publishes publishes =
                            mqtt5BlockingClient.publishes(MqttGlobalPublishFilter.SUBSCRIBED);
                    while(running) {
                        try {
                            publishes
                                    .receiveNow()
                                    .ifPresent(
                                            mqtt5Publish -> {
                                                MessageHeaders messageHeaders = extractedHeader(mqtt5Publish);
                                                SmartMessageConverter messageConverter =
                                                        getMessageConverter(messageHeaders);
                                                Message<?> message =
                                                        messageConverter.toMessage(
                                                                mqtt5Publish.getPayloadAsBytes(), messageHeaders);

                                                sendMessage(message);
                                            });
                            Thread.sleep(1, 5);
                        } catch(InterruptedException e) {
                            // NOOP
                        } catch(Exception e) {
                            logger.error(e, "Exception occurred while processing message");
                        }
                    }
                    publishes.close();
                };
        executorService.submit(runnable);
    }

    @Override
    public void sendMessage(Message<?> message) {
        logger.info("Sending message " + message.toString());
        super.sendMessage(message);
    }

    @SuppressWarnings("unused")
    private SmartMessageConverter getMessageConverter(MessageHeaders messageHeaders) {
        // TODO maybe needed in the future
    /*MimeType mimeType = contentTypeResolver.resolve(messageHeaders);
    if (MimeTypeUtils.APPLICATION_JSON.equals(mimeType)) {
      return new JsonStringMessageConverter();
    }
    return new ByteArrayMessageConverter();*/

        return new JsonStringMessageConverter();
    }

    private MessageHeaders extractedHeader(Mqtt5Publish mqtt5Publish) {
        Map<String, Object> header = new HashMap<>();

        header.put("mqtt_receivedQos", mqtt5Publish.getQos().getCode());
        header.put("mqtt_receivedRetained", mqtt5Publish.isRetain());
        header.put("mqtt_receivedTopic", mqtt5Publish.getTopic().toString());

        mqtt5Publish
                .getResponseTopic()
                .map(MqttTopic::toString)
                .ifPresent(rt -> header.put("mqtt_responseTopic", rt));
        mqtt5Publish
                .getContentType()
                .map(Objects::toString)
                .ifPresent(contentType -> header.put("contentType", contentType));
        mqtt5Publish
                .getCorrelationData()
                .map(
                        byteBuffer -> {
                            int length = byteBuffer.remaining();
                            byte[] correlationData = new byte[length];
                            byteBuffer.get(correlationData);

                            return correlationData;
                        })
                .ifPresent(correlationData -> header.put("mqtt_correlationData", correlationData));

        return new MessageHeaders(header);
    }

    @Override
    protected void doStop() {
        running = false;
        executorService.shutdown();
    }
}
