package org.springframework.cloud.binder.mqtt.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JsonStringMessageConverterTest {

  private static JsonStringMessageConverter converter = new JsonStringMessageConverter();

  @Test
  public void supports_shouldReturnTrue_whenClassIsByteArray() {
    assertThat(converter.supports(byte[].class)).isTrue();
  }

  @Test
  public void supports_shouldReturnFalse_whenClassIsNotByteArray() {
    assertThat(converter.supports(Object.class)).isFalse();
  }

  @Test
  public void convertToInternal_shouldWork() {
    String payload = "payload";
    assertThat(converter.convertToInternal(payload.getBytes(), null, null)).isEqualTo(payload);
  }
}
