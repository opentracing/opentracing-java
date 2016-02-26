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

import io.opentracing.Span;
import io.opentracing.Tracer;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

abstract class AbstractSpanBuilder implements Tracer.SpanBuilder {

    AbstractSpanBuilder() {}

    protected String operationName = null;
    protected Span parent = null;
    protected Instant start = Instant.now();
    private final Map<String, String> stringTags = new HashMap<>();
    private final Map<String, Boolean> booleanTags = new HashMap<>();
    private final Map<String, Number> numberTags = new HashMap<>();
    private final Map<String, String> baggage = new HashMap<>();

    /** Create a Span, using the builder fields. */
    protected abstract AbstractSpan createSpan();

    /** Returns true if this key+value belongs in a Span's traceState. */
    abstract boolean isTraceState(String key, Object value);

    /** Returns true if this key+value belongs in a Span's baggage. */
    abstract boolean isBaggage(String key, Object value);

    abstract Tracer.SpanBuilder withStateItem(String key, Object value);

    public Tracer.SpanBuilder withBaggageItem(String key, String value) {
        assert isBaggage(key, value);
        baggage.put(key, value);
        return this;
    }

    @Override
    public final Tracer.SpanBuilder withOperationName(String operationName) {
        this.operationName = operationName;
        return this;
    }

    @Override
    public final Tracer.SpanBuilder withParent(Span parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public final Tracer.SpanBuilder withTag(String key, String value) {
        stringTags.put(key, value);
        return this;
    }

    @Override
    public final Tracer.SpanBuilder withTag(String key, boolean value) {
        booleanTags.put(key, value);
        return this;
    }

    @Override
    public final Tracer.SpanBuilder withTag(String key, Number value) {
        numberTags.put(key, value);
        return this;
    }

    @Override
    public final Tracer.SpanBuilder withStartTimestamp(long microseconds) {
        long epochSeconds = TimeUnit.MICROSECONDS.toSeconds(microseconds);
        long nanos = TimeUnit.MICROSECONDS.toNanos(microseconds) - TimeUnit.SECONDS.toNanos(epochSeconds);
        this.start = Instant.ofEpochSecond(epochSeconds, nanos);
        return this;
    }

    @Override
    public final Span start() {
        AbstractSpan span = createSpan();
        stringTags.entrySet().stream().forEach((entry) -> { span.setTag(entry.getKey(), entry.getValue()); });
        booleanTags.entrySet().stream().forEach((entry) -> { span.setTag(entry.getKey(), entry.getValue()); });
        numberTags.entrySet().stream().forEach((entry) -> { span.setTag(entry.getKey(), entry.getValue()); });
        baggage.entrySet().stream().forEach((entry) -> { span.setBaggageItem(entry.getKey(), entry.getValue()); });
        return span;
    }

}
