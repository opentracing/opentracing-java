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
package io.opentracing;

import io.opentracing.log.Field;

import java.util.Collections;
import java.util.Map;

final class NoopSpan implements Span {

    static final NoopSpan INSTANCE = new NoopSpan();

    static final SpanContext CONTEXT = new SpanContext() {
        @Override
        public Iterable<Map.Entry<String, String>> baggageItems() {
            return Collections.EMPTY_MAP.entrySet();
        }
    };

    private NoopSpan() {}

    @Override
    public SpanContext context() { return CONTEXT; }

    @Override
    public void finish() {}

    @Override
    public void finish(long finishMicros) {}

    @Override
    public void close() {
        finish();
    }

    @Override
    public Span setTag(String key, String value) {
        return this;
    }

    @Override
    public Span setTag(String key, boolean value) {
        return this;
    }

    @Override
    public Span setTag(String key, Number value) {
        return this;
    }

    @Override
    public Span log(Field... fields) { return this; }

    @Override
    public Span log(long timestampMicroseconds, Field... fields) { return this; }

    @Override
    public Span log(String eventName, Object payload) {
        return this;
    }

    @Override
    public Span log(long timestampMicroseconds, String eventName, Object payload) {
        return this;
    }

    @Override
    public Span setBaggageItem(String key, String value) { return this; }

    @Override
    public String getBaggageItem(String key) { return null; }

    @Override
    public Span setOperationName(String operationName) {
        return this;
    }

}
