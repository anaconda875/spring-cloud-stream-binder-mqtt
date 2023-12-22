package org.springframework.cloud.binder.mqtt;

import org.springframework.cloud.binder.mqtt.inbound.MqttV5MessageDrivenChannelAdapter;
import org.springframework.cloud.binder.mqtt.outbound.MqttV5MessageHandler;
import org.springframework.cloud.binder.mqtt.properties.*;
import org.springframework.cloud.stream.binder.*;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

public class MqttBinder
    extends AbstractMessageChannelBinder<
        ExtendedConsumerProperties<MqttSourceProperties>,
        ExtendedProducerProperties<MqttSinkProperties>,
        MqttProvisioningProvider>
    implements ExtendedPropertiesBinder<MessageChannel, MqttSourceProperties, MqttSinkProperties> {

  private MqttExtendedBindingProperties extendedBindingProperties;
  private RuntimeMqttExtendedBindingProperties runtimeExtendedBindingProperties;
  private MqttBinderConfigurationProperties binderConfigurationProperties;

  public MqttBinder(
      MqttProvisioningProvider provisioningProvider,
      MqttExtendedBindingProperties extendedBindingProperties,
      RuntimeMqttExtendedBindingProperties runtimeExtendedBindingProperties,
      MqttBinderConfigurationProperties binderConfigurationProperties) {
    super(BinderHeaders.STANDARD_HEADERS, provisioningProvider);
    this.extendedBindingProperties = extendedBindingProperties;
    this.runtimeExtendedBindingProperties = runtimeExtendedBindingProperties;
    this.binderConfigurationProperties = binderConfigurationProperties;
  }

  @Override
  protected MessageHandler createProducerMessageHandler(
      ProducerDestination destination,
      ExtendedProducerProperties<MqttSinkProperties> producerProperties,
      MessageChannel errorChannel) {

    MqttSinkProperties sinkProperties = producerProperties.getExtension();
    MqttV5MessageHandler handler =
        new MqttV5MessageHandler(
            binderConfigurationProperties, sinkProperties, destination.getName());

    return handler;
  }

  @Override
  protected MessageProducer createConsumerEndpoint(
      ConsumerDestination destination,
      String group,
      ExtendedConsumerProperties<MqttSourceProperties> properties) {

    MqttSourceProperties sourceProperties = properties.getExtension();
    MqttV5MessageDrivenChannelAdapter adapter =
        new MqttV5MessageDrivenChannelAdapter(
            binderConfigurationProperties, sourceProperties.getClientId(), destination.getName());

    return adapter;
  }

  @Override
  public MqttSourceProperties getExtendedConsumerProperties(String channelName) {
    return this.extendedBindingProperties.getExtendedConsumerProperties(channelName);
  }

  @Override
  public MqttSinkProperties getExtendedProducerProperties(String channelName) {
    if (this.extendedBindingProperties.getBindings().containsKey(channelName)) {
      return this.extendedBindingProperties.getExtendedProducerProperties(channelName);
    }
    if (this.runtimeExtendedBindingProperties.getBindings().containsKey(channelName)) {
      return this.runtimeExtendedBindingProperties.getExtendedProducerProperties(channelName);
    }

    return this.extendedBindingProperties.getExtendedProducerProperties(channelName);
  }

  @Override
  public String getDefaultsPrefix() {
    return this.extendedBindingProperties.getDefaultsPrefix();
  }

  @Override
  public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
    return this.extendedBindingProperties.getExtendedPropertiesEntryClass();
  }
}
