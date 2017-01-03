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
package io.opentracing.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;

/**
 * MockTracer makes it easy to test the semantics of OpenTracing instrumentation.
 *
 * By using a MockTracer as an io.opentracing.Tracer implementation for unittests, a developer can assert that Span
 * properties and relationships with other Spans are defined as expected by instrumentation code.
 *
 * The MockTracerTest has simple usage examples.
 */
public class MockTracer implements Tracer {
    private List<MockSpan> finishedSpans = new ArrayList<>();
    private final Propagator propagator;

    public MockTracer() {
        this(Propagator.PRINTER);
    }

    /**
     * Create a new MockTracer that passes through any calls to inject() and/or extract().
     */
    public MockTracer(Propagator propagator) {
        this.propagator = propagator;
    }

    /**
     * Clear the finishedSpans() queue.
     *
     * Note that this does *not* have any effect on Spans created by MockTracer that have not finish()ed yet; those
     * will still be enqueued in finishedSpans() when they finish().
     */
    public synchronized void reset() {
        this.finishedSpans.clear();
    }

    /**
     * @return a copy of all finish()ed MockSpans started by this MockTracer (since construction or the last call to
     * MockTracer.reset()).
     *
     * @see MockTracer#reset()
     */
    public synchronized List<MockSpan> finishedSpans() {
        return new ArrayList<>(this.finishedSpans);
    }

    /**
     * Noop method called on {@link Span#finish()}.
     */
    protected void onSpanFinished(MockSpan mockSpan) {
    }

    /**
     * Propagator allows the developer to intercept and verify any calls to inject() and/or extract().
     *
     * By default, MockTracer uses Propagator.PRINTER which simply logs such calls to System.out.
     *
     * @see MockTracer#MockTracer(Propagator)
     */
    public interface Propagator {
        <C> void inject(MockSpan.MockContext ctx, Format<C> format, C carrier);
        <C> MockSpan.MockContext extract(Format<C> format, C carrier);

        Propagator PRINTER = new Propagator() {
            @Override
            public <C> void inject(MockSpan.MockContext ctx, Format<C> format, C carrier) {
                System.out.println("inject(" + ctx + ", " + format + ", " + carrier + ")");
            }

            @Override
            public <C> MockSpan.MockContext extract(Format<C> format, C carrier) {
                System.out.println("extract(" + format + ", " + carrier + ")");
                return null;
            }
        };
    }

    @Override
    public SpanBuilder buildSpan(String operationName) {
        return new SpanBuilder(operationName);
    }

    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
        this.propagator.inject((MockSpan.MockContext)spanContext, format, carrier);
    }

    @Override
    public <C> SpanContext extract(Format<C> format, C carrier) {
        return this.propagator.extract(format, carrier);
    }

    synchronized void appendFinishedSpan(MockSpan mockSpan) {
        this.finishedSpans.add(mockSpan);
        this.onSpanFinished(mockSpan);
    }

    final class SpanBuilder implements Tracer.SpanBuilder {
        private final String operationName;
        private long startMicros;
        private MockSpan.MockContext firstParent;
        private Map<String, Object> initialTags = new HashMap<>();

        SpanBuilder(String operationName) {
            this.operationName = operationName;
        }
        @Override
        public Tracer.SpanBuilder asChildOf(SpanContext parent) {
            return addReference(References.CHILD_OF, parent);
        }

        @Override
        public Tracer.SpanBuilder asChildOf(Span parent) {
            return addReference(References.CHILD_OF, parent.context());
        }

        @Override
        public Tracer.SpanBuilder addReference(String referenceType, SpanContext referencedContext) {
            if (firstParent == null && (
                    referenceType.equals(References.CHILD_OF) || referenceType.equals(References.FOLLOWS_FROM))) {
                this.firstParent = (MockSpan.MockContext)referencedContext;
            }
            return this;
        }

        @Override
        public Tracer.SpanBuilder withTag(String key, String value) {
            this.initialTags.put(key, value);
            return this;
        }

        @Override
        public Tracer.SpanBuilder withTag(String key, boolean value) {
            this.initialTags.put(key, value);
            return this;
        }

        @Override
        public Tracer.SpanBuilder withTag(String key, Number value) {
            this.initialTags.put(key, value);
            return this;
        }

        @Override
        public Tracer.SpanBuilder withStartTimestamp(long microseconds) {
            this.startMicros = microseconds;
            return this;
        }

        @Override
        public Span start() {
            if (this.startMicros == 0) {
                this.startMicros = MockSpan.nowMicros();
            }
            return new MockSpan(MockTracer.this, this.operationName, this.startMicros, initialTags, this.firstParent);
        }

        @Override
        public Iterable<Map.Entry<String, String>> baggageItems() {
            if (firstParent == null) {
                return Collections.EMPTY_MAP.entrySet();
            } else {
                return firstParent.baggageItems();
            }
        }
    }
}
