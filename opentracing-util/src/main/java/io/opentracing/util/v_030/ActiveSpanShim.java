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

import io.opentracing.Scope;
import io.opentracing.SpanContext;
import io.opentracing.v_030.ActiveSpan;

import java.util.Map;

class ActiveSpanShim implements ActiveSpan, SpanWrapper {
    Scope scope;

    public ActiveSpanShim(Scope scope) {
        this.scope = scope;
    }

    protected Scope scope() {
        return scope;
    }

    @Override
    public io.opentracing.Span span() {
        return scope.span();
    }

    @Override
    public void deactivate() {
        scope.close();
    }

    @Override
    public void close() {
        deactivate();
    }

    @Override
    public Continuation capture() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpanContext context() {
        return scope.span().context();
    }

    @Override
    public ActiveSpanShim setTag(String key, String value) {
        scope.span().setTag(key, value);
        return this;
    }

    @Override
    public ActiveSpanShim setTag(String key, boolean value) {
        scope.span().setTag(key, value);
        return this;
    }

    @Override
    public ActiveSpanShim setTag(String key, Number value) {
        scope.span().setTag(key, value);
        return this;
    }

    @Override
    public final ActiveSpanShim log(Map<String, ?> fields) {
        scope.span().log(fields);
        return this;
    }

    @Override
    public final ActiveSpanShim log(long timestampMicros, Map<String, ?> fields) {
        scope.span().log(timestampMicros, fields);
        return this;
    }

    @Override
    public ActiveSpanShim log(String event) {
        scope.span().log(event);
        return this;
    }

    @Override
    public ActiveSpanShim log(long timestampMicroseconds, String event) {
        scope.span().log(timestampMicroseconds, event);
        return this;
    }

    @Override
    public ActiveSpanShim setBaggageItem(String key, String value) {
        scope.span().setBaggageItem(key, value);
        return this;
    }

    @Override
    public String getBaggageItem(String key) {
        return scope.span().getBaggageItem(key);
    }

    @Override
    public ActiveSpanShim setOperationName(String operationName) {
        scope.span().setOperationName(operationName);
        return this;
    }

    @Override
    public String toString() {
        return scope.span().toString();
    }
}
