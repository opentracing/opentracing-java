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
package io.opentracing.noop;


import io.opentracing.Span;
import io.opentracing.Tracer;

public class NoopSpanBuilder implements Tracer.SpanBuilder {
    private Tracer.SpanBuilder defaultSpanBuilder = new NoopSpanBuilder();
    private Span defaultSpan = new NoopSpan();

    @Override
    public Tracer.SpanBuilder withOperationName(String operationName) {
        return defaultSpanBuilder;
    }

    @Override
    public Tracer.SpanBuilder withParent(Span parent) {
        return defaultSpanBuilder;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, String value) {
        return defaultSpanBuilder;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, boolean value) {
        return defaultSpanBuilder;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, Number value) {
        return defaultSpanBuilder;
    }

    @Override
    public Tracer.SpanBuilder withStartTimestamp(long microseconds) {
        return defaultSpanBuilder;
    }

    @Override
    public Span start() {
        return defaultSpan;
    }

    @Override
    public Span start(long microseconds) {
        return defaultSpan;
    }
}
