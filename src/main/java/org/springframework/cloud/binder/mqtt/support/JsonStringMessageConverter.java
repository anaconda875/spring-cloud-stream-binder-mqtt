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
