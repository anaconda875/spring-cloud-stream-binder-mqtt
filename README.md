# MQTT Spring Cloud Stream Binder client library for Java

The project provides **Spring Cloud Stream Binder for MQTT** which allows you to build message-driven
microservice using **Spring Cloud Stream**

## Usage


### Include the package
Add dependency
```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-stream-binder-mqtt</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Configuration Options

The binder provides the following configuration options in `application.properties`.

#### MQTT Binder Properties ####

**_spring.cloud.stream.mqtt.binder.serverHost_**

Location of the mqtt broker. Default `localhost`

**_spring.cloud.stream.mqtt.binder.serverPort_**

Port of the mqtt broker. Default `1883`

**_spring.cloud.stream.mqtt.binder.username_**

The username to use when connecting to the broker. Default `guest`

**_spring.cloud.stream.mqtt.binder.password_**

The password to use when connecting to the broker. Default `guest`

#### Mqtt Consumer Properties ####

The following properties are available for MQTT consumers only and must be prefixed with `spring.cloud.stream.mqtt.bindings.<channelName>.consumer.`

**_clientId_**

Identifies the client. Default: `stream.client.id.source`

#### Mqtt Producer Properties ####

The following properties are available for MQTT consumers only and must be prefixed with `spring.cloud.stream.mqtt.bindings.<channelName>.producer.`

**_clientId_**

Identifies the client. Default: `stream.client.id.sink`
