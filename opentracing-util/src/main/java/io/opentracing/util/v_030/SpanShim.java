/*
 * Copyright 2016-2017 The OpenTracing Authors
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
package io.opentracing.util.v_030;

import io.opentracing.SpanContext;
import io.opentracing.v_030.Span;

import java.util.Map;

class SpanShim implements Span, SpanWrapper {
    io.opentracing.Span span;

    public SpanShim(io.opentracing.Span span) {
        this.span = span;
    }

    @Override
    public io.opentracing.Span span() {
        return span;
    }

    @Override
    public void finish() {
        span.finish();
    }

    @Override
    public void finish(long finishMicros) {
        span.finish(finishMicros);
    }

    @Override
    public SpanContext context() {
        return span.context();
    }

    @Override
    public SpanShim setTag(String key, String value) {
        span.setTag(key, value);
        return this;
    }

    @Override
    public SpanShim setTag(String key, boolean value) {
        span.setTag(key, value);
        return this;
    }

    @Override
    public SpanShim setTag(String key, Number value) {
        span.setTag(key, value);
        return this;
    }

    @Override
    public final SpanShim log(Map<String, ?> fields) {
        span.log(fields);
        return this;
    }

    @Override
    public final SpanShim log(long timestampMicros, Map<String, ?> fields) {
        span.log(timestampMicros, fields);
        return this;
    }

    @Override
    public SpanShim log(String event) {
        span.log(event);
        return this;
    }

    @Override
    public SpanShim log(long timestampMicroseconds, String event) {
        span.log(timestampMicroseconds, event);
        return this;
    }

    @Override
    public SpanShim setBaggageItem(String key, String value) {
        span.setBaggageItem(key, value);
        return this;
    }

    @Override
    public String getBaggageItem(String key) {
        return span.getBaggageItem(key);
    }

    @Override
    public SpanShim setOperationName(String operationName) {
        span.setOperationName(operationName);
        return this;
    }

    @Override
    public String toString() {
        return span.toString();
    }
}
