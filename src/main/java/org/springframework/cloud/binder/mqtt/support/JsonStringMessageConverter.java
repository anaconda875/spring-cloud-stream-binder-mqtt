package org.springframework.cloud.binder.mqtt.support;

import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.messaging.converter.SmartMessageConverter;
import org.springframework.util.MimeTypeUtils;

/**
 * An implementation of {@link SmartMessageConverter}.
 *
 * @author Bao Ngo
 */
public class JsonStringMessageConverter extends AbstractMessageConverter {

  public JsonStringMessageConverter() {
    super(MimeTypeUtils.APPLICATION_JSON);
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return byte[].class == clazz;
  }

  @Override
  protected Object convertToInternal(
      Object payload, MessageHeaders headers, Object conversionHint) {
    return new String((byte[]) payload);
  }
}
