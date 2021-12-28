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

package org.springframework.cloud.binder.mqtt.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

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
