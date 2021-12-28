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
            if(topicFromHeader != null) {
                topic = topicFromHeader;
            }
            publish(topic, body, headers);
        } catch(JsonProcessingException e) {
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
        if(payload instanceof byte[]) {
            body = (byte[]) payload;
        } else if(payload instanceof String) {
            body = ((String) payload).getBytes(StandardCharsets.UTF_8);
        } else {
            body = parsePayload(objectMapper.writeValueAsString(payload));
        }

        return body;
    }
}
