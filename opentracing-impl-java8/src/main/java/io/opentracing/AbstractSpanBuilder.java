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

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

abstract class AbstractSpanBuilder implements Tracer.SpanBuilder {
    protected String operationName = null;
    protected List<Reference> references = new ArrayList<Reference>();
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

    @Override
    public final Tracer.SpanBuilder addReference(String referenceType, SpanContext referredTo) {
        this.references.add(new Reference(referenceType, referredTo));
        return this;
    }

    @Override
    public final Tracer.SpanBuilder asChildOf(SpanContext parent) {
        return this.addReference(References.CHILD_OF, parent);
    }

    @Override
    public final Tracer.SpanBuilder asChildOf(Span parent) {
        return this.addReference(References.CHILD_OF, parent.context());
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

    public static final class Reference {
        private String referenceType;
        private SpanContext referredTo;

        Reference(String type, SpanContext referredTo) {
            this.referenceType = type;
            this.referredTo = referredTo;
        }

        public final Object getReferenceType() { return referenceType; }
        public final SpanContext getReferredTo() { return referredTo; }
    }
}
