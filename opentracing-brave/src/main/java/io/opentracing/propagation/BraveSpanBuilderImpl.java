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
package io.opentracing.propagation;

import java.util.Optional;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.IdConversion;
import com.github.kristofa.brave.ServerTracer;
import com.github.kristofa.brave.http.BraveHttpHeaders;

import io.opentracing.Tracer;

final class BraveSpanBuilderImpl extends AbstractSpanBuilder {

    private final Brave brave;
    Long traceId = null;
    Long spanId = null;
    Long parentSpanId = null;

    ServerTracer serverTracer = null;

    BraveSpanBuilderImpl(Brave brave) {
        this.brave = brave;
    }

    @Override
    protected BraveSpanImpl createSpan() {

        BraveSpanImpl span = new BraveSpanImpl(
                brave,
                operationName,
                Optional.ofNullable(parent),
                start,
                Optional.ofNullable(serverTracer));

        assert null == traceId || span.spanId.getTraceId() == traceId;
        assert null == spanId || span.spanId.getSpanId() == spanId;
        assert null == parentSpanId || parentSpanId.equals(span.spanId.getParentSpanId());

        return span;
    }

    @Override
    boolean isTraceState(String key, Object value) {
        try {
            BraveHttpHeaders.valueOf(key);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    boolean isBaggage(String key, Object value) {
        return !isTraceState(key, value);
    }

    @Override
    public Tracer.SpanBuilder withStateItem(String key, Object value) {
        assert isTraceState(key, value);
        switch (BraveHttpHeaders.valueOf(key)) {
            case TraceId:
                traceId = value instanceof Number
                        ? ((Number)value).longValue()
                        : IdConversion.convertToLong(value.toString());
                break;
            case SpanId:
                spanId = value instanceof Number
                        ? ((Number)value).longValue()
                        : IdConversion.convertToLong(value.toString());
                break;
            case ParentSpanId:
                parentSpanId = value instanceof Number
                        ? ((Number)value).longValue()
                        : IdConversion.convertToLong(value.toString());
                break;
        }
        return this;
    }

    void withServerTracer(ServerTracer serverTracer) {
        this.serverTracer = serverTracer;
    }

}
