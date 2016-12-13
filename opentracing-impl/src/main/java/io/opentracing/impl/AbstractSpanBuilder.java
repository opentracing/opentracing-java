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

import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

abstract class AbstractSpanBuilder implements Tracer.SpanBuilder {

    protected String operationName = null;
    protected final List<Reference> references = new ArrayList<>();
    protected Instant start = Instant.now();

    private final Map<String, String> stringTags = new HashMap<>();
    private final Map<String, Boolean> booleanTags = new HashMap<>();
    private final Map<String, Number> numberTags = new HashMap<>();
    private final Map<String, String> baggage = new HashMap<>();

    AbstractSpanBuilder(String operationName) {
        this.operationName = operationName;
    }

    /** Create a Span, using the builder fields. */
    protected abstract AbstractSpan createSpan();

    /** Adds an entry of the minimal set of properties required to propagate this span */
    abstract AbstractSpanBuilder withStateItem(String key, Object value);

    /** Returns true if this key+value belongs in a Span's required propagation set, otherwise it is baggage. */
    abstract boolean isTraceState(String key, Object value);

    @Override
    public final AbstractSpanBuilder addReference(String referenceType, SpanContext referredTo) {
        this.references.add(new Reference(referenceType, referredTo));
        return this;
    }

    @Override
    public final AbstractSpanBuilder asChildOf(SpanContext parent) {
        if (io.opentracing.NoopSpanContext.class.isAssignableFrom(parent.getClass())) {
            return NoopSpanBuilder.INSTANCE;
        } else {
            withBaggageFrom(parent);
            return this.addReference(References.CHILD_OF, parent);
        }
    }

    @Override
    public final AbstractSpanBuilder asChildOf(Span parent) {
        if (io.opentracing.NoopSpan.class.isAssignableFrom(parent.getClass())) {
            return NoopSpanBuilder.INSTANCE;
        } else {
            withBaggageFrom(parent.context());
            return this.addReference(References.CHILD_OF, parent.context());
        }
    }

    @Override
    public final AbstractSpanBuilder withTag(String key, String value) {
        stringTags.put(key, value);
        return this;
    }

    @Override
    public final AbstractSpanBuilder withTag(String key, boolean value) {
        booleanTags.put(key, value);
        return this;
    }

    @Override
    public final AbstractSpanBuilder withTag(String key, Number value) {
        numberTags.put(key, value);
        return this;
    }

    @Override
    public final AbstractSpanBuilder withStartTimestamp(long microseconds) {
        long epochSeconds = TimeUnit.MICROSECONDS.toSeconds(microseconds);
        long nanos = TimeUnit.MICROSECONDS.toNanos(microseconds) - TimeUnit.SECONDS.toNanos(epochSeconds);
        this.start = Instant.ofEpochSecond(epochSeconds, nanos);
        return this;
    }

    public final AbstractSpanBuilder withBaggageItem(String key, String value) {
        assert !isTraceState(key, value);
        baggage.put(key, value);
        return this;
    }

    @Override
    public final Iterable<Map.Entry<String, String>> baggageItems() {
        return baggage.entrySet();
    }

    @Override
    public final Span start() {
        AbstractSpan span = createSpan();
        stringTags.entrySet().forEach((entry) -> span.setTag(entry.getKey(), entry.getValue()));
        booleanTags.entrySet().forEach((entry) -> span.setTag(entry.getKey(), entry.getValue()));
        numberTags.entrySet().forEach((entry) -> span.setTag(entry.getKey(), entry.getValue()));
        baggage.entrySet().forEach((entry) -> span.setBaggageItem(entry.getKey(), entry.getValue()));
        return span;
    }

    private void withBaggageFrom(SpanContext from) {
        for (Entry<String, String> baggageItem : from.baggageItems()) {
            this.withBaggageItem(baggageItem.getKey(), baggageItem.getValue());
        }
    }

    public static final class Reference {
        private final String referenceType;
        private final SpanContext referredTo;

        Reference(String type, SpanContext referredTo) {
            this.referenceType = type;
            this.referredTo = referredTo;
        }

        public final Object getReferenceType() { return referenceType; }
        public final SpanContext getReferredTo() { return referredTo; }
    }
}
