/**
 * Copyright 2016 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.opentracing.impl;

import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.propagation.Extractor;
import io.opentracing.propagation.TextMap;

import java.util.Map;

final class TestTextMapExtractorImpl implements Extractor<TextMap> {

    @Override
    public SpanBuilder extract(TextMap carrier) {
        String marker = null;
        for (Map.Entry<String,String> entry : carrier) {
            if (entry.getKey().equals("test-marker")) {
                marker = entry.getValue();
            }
        }

        return null != marker ? new TestSpanBuilder(marker) : NoopSpanBuilder.INSTANCE;
    }
}
