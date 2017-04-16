/**
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
package io.opentracing.impl;

import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

abstract class AbstractSpanBuilder implements Tracer.SpanBuilder {

    protected String operationName = null;
    protected List<Reference> references;
    protected Instant start = Instant.now();

    protected Map<String, String> stringTags;
    protected Map<String, Boolean> booleanTags;
    protected Map<String, Number> numberTags;
    protected Map<String, String> baggage;

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
    public AbstractSpanBuilder addReference(String referenceType, SpanContext referredTo) {
        if (references == null) {
            references = new ArrayList<>();
        }
        this.references.add(new Reference(referenceType, referredTo));
        return this;
    }

    @Override
    public AbstractSpanBuilder asChildOf(SpanContext parent) {
        if (io.opentracing.NoopSpanContext.class.isAssignableFrom(parent.getClass())) {
            return NoopSpanBuilder.INSTANCE;
        } else {
            withBaggageFrom(parent);
            return this.addReference(References.CHILD_OF, parent);
        }
    }

    @Override
    public AbstractSpanBuilder asChildOf(Span parent) {
        if (io.opentracing.NoopSpan.class.isAssignableFrom(parent.getClass())) {
            return NoopSpanBuilder.INSTANCE;
        } else {
            withBaggageFrom(parent.context());
            return this.addReference(References.CHILD_OF, parent.context());
        }
    }

    @Override
    public AbstractSpanBuilder withTag(String key, String value) {
        if (stringTags == null) {
            stringTags = new HashMap<>();
        }
        stringTags.put(key, value);
        return this;
    }

    @Override
    public AbstractSpanBuilder withTag(String key, boolean value) {
        if (booleanTags == null) {
            booleanTags = new HashMap<>();
        }
        booleanTags.put(key, value);
        return this;
    }

    @Override
    public AbstractSpanBuilder withTag(String key, Number value) {
        if (numberTags == null) {
            numberTags = new HashMap<>();
        }
        numberTags.put(key, value);
        return this;
    }

    @Override
    public AbstractSpanBuilder withStartTimestamp(long microseconds) {
        long epochSeconds = TimeUnit.MICROSECONDS.toSeconds(microseconds);
        long nanos = TimeUnit.MICROSECONDS.toNanos(microseconds) - TimeUnit.SECONDS.toNanos(epochSeconds);
        this.start = Instant.ofEpochSecond(epochSeconds, nanos);
        return this;
    }

    public AbstractSpanBuilder withBaggageItem(String key, String value) {
        assert !isTraceState(key, value);
        if (baggage == null) {
            baggage = new HashMap<>();
        }
        baggage.put(key, value);
        return this;
    }

    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
        return baggage == null ? Collections.emptySet() : baggage.entrySet();
    }

    @Override
    public Span start() {
        AbstractSpan span = createSpan();
        if (stringTags != null) {
          stringTags.entrySet().forEach((entry) -> span.setTag(entry.getKey(), entry.getValue()));
        }
        if (booleanTags != null) {
          booleanTags.entrySet().forEach((entry) -> span.setTag(entry.getKey(), entry.getValue()));
        }
        if (numberTags != null) {
          numberTags.entrySet().forEach((entry) -> span.setTag(entry.getKey(), entry.getValue()));
        }
        if (baggage != null) {
          baggage.entrySet().forEach((entry) -> span.setBaggageItem(entry.getKey(), entry.getValue()));
        }
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
