/*
 * Copyright 2016-2018 The OpenTracing Authors
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.noop.NoopScopeManager;
import io.opentracing.propagation.Binary;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.util.ThreadLocalScopeManager;

/**
 * MockTracer makes it easy to test the semantics of OpenTracing instrumentation.
 *
 * By using a MockTracer as an io.opentracing.Tracer implementation for unittests, a developer can assert that Span
 * properties and relationships with other Spans are defined as expected by instrumentation code.
 *
 * The MockTracerTest has simple usage examples.
 */
public class MockTracer implements Tracer {
    private final List<MockSpan> finishedSpans = new ArrayList<>();
    private final Propagator propagator;
    private final ScopeManager scopeManager;

    public MockTracer() {
        this(new ThreadLocalScopeManager(), Propagator.TEXT_MAP);
    }

    public MockTracer(ScopeManager scopeManager) {
        this(scopeManager, Propagator.TEXT_MAP);
    }

    public MockTracer(ScopeManager scopeManager, Propagator propagator) {
        this.scopeManager = scopeManager;
        this.propagator = propagator;
    }

    /**
     * Create a new MockTracer that passes through any calls to inject() and/or extract().
     */
    public MockTracer(Propagator propagator) {
        this(NoopScopeManager.INSTANCE, propagator);
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

        Propagator BINARY = new Propagator() {
            static final int BUFFER_SIZE = 128;

            @Override
            public <C> void inject(MockSpan.MockContext ctx, Format<C> format, C carrier) {
                if (carrier instanceof Binary) {
                    Binary binary = (Binary) carrier;
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    ObjectOutputStream objStream = null;
                    try {
                        objStream = new ObjectOutputStream(stream);
                        objStream.writeLong(ctx.spanId());
                        objStream.writeLong(ctx.traceId());

                        for (Map.Entry<String, String> entry : ctx.baggageItems()) {
                            objStream.writeUTF(entry.getKey());
                            objStream.writeUTF(entry.getValue());
                        }

                        objStream.flush(); // *need* to flush ObjectOutputStream.

                        ByteBuffer buff = ByteBuffer.wrap(stream.toByteArray());
                        while (buff.hasRemaining()) {
                            binary.write(buff);
                        }

                    } catch (IOException e) {
                        throw new RuntimeException("Corrupted state");
                    } finally {
                        if (objStream != null) {
                            try { objStream.close(); } catch (Exception e2) {}
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Unknown carrier");
                }
            }

            @Override
            public <C> MockSpan.MockContext extract(Format<C> format, C carrier) {
                Long traceId = null;
                Long spanId = null;
                Map<String, String> baggage = new HashMap<>();

                if (carrier instanceof Binary) {
                    Binary binary = (Binary) carrier;
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    ObjectInputStream objStream = null;
                    try {
                        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                        for (int readBytes = 0; (readBytes = binary.read(buffer)) > 0; buffer.rewind()) {
                            outputStream.write(buffer.array(), 0, readBytes);
                        }

                        objStream = new ObjectInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
                        spanId = objStream.readLong();
                        traceId = objStream.readLong();

                        while (objStream.available() > 0) {
                            baggage.put(objStream.readUTF(), objStream.readUTF());
                        }
                    } catch (IOException e) {
                    } finally {
                        if (objStream != null) {
                            try { objStream.close(); } catch (Exception e2) {}
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Unknown carrier");
                }

                if (traceId != null && spanId != null) {
                    return new MockSpan.MockContext(traceId, spanId, baggage);
                }

                return null;
            }
        };

        Propagator TEXT_MAP = new Propagator() {
            public static final String SPAN_ID_KEY = "spanid";
            public static final String TRACE_ID_KEY = "traceid";
            public static final String BAGGAGE_KEY_PREFIX = "baggage-";

            @Override
            public <C> void inject(MockSpan.MockContext ctx, Format<C> format, C carrier) {
                if (carrier instanceof TextMap) {
                    TextMap textMap = (TextMap) carrier;
                    for (Map.Entry<String, String> entry : ctx.baggageItems()) {
                        textMap.put(BAGGAGE_KEY_PREFIX + entry.getKey(), entry.getValue());
                    }
                    textMap.put(SPAN_ID_KEY, String.valueOf(ctx.spanId()));
                    textMap.put(TRACE_ID_KEY, String.valueOf(ctx.traceId()));
                } else {
                    throw new IllegalArgumentException("Unknown carrier");
                }
            }

            @Override
            public <C> MockSpan.MockContext extract(Format<C> format, C carrier) {
                Long traceId = null;
                Long spanId = null;
                Map<String, String> baggage = new HashMap<>();

                if (carrier instanceof TextMap) {
                    TextMap textMap = (TextMap) carrier;
                    for (Map.Entry<String, String> entry : textMap) {
                        if (TRACE_ID_KEY.equals(entry.getKey())) {
                            traceId = Long.valueOf(entry.getValue());
                        } else if (SPAN_ID_KEY.equals(entry.getKey())) {
                            spanId = Long.valueOf(entry.getValue());
                        } else if (entry.getKey().startsWith(BAGGAGE_KEY_PREFIX)){
                            String key = entry.getKey().substring((BAGGAGE_KEY_PREFIX.length()));
                            baggage.put(key, entry.getValue());
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Unknown carrier");
                }

                if (traceId != null && spanId != null) {
                    return new MockSpan.MockContext(traceId, spanId, baggage);
                }

                return null;
            }
        };
    }

    @Override
    public ScopeManager scopeManager() {
        return this.scopeManager;
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

    @Override
    public Span activeSpan() {
        Scope scope = this.scopeManager.active();
        return scope == null ? null : scope.span();
    }

    synchronized void appendFinishedSpan(MockSpan mockSpan) {
        this.finishedSpans.add(mockSpan);
        this.onSpanFinished(mockSpan);
    }

    private SpanContext activeSpanContext() {
        Span span = activeSpan();
        if (span == null) {
            return null;
        }

        return span.context();
    }

    public final class SpanBuilder implements Tracer.SpanBuilder {
        private final String operationName;
        private long startMicros;
        private List<MockSpan.Reference> references = new ArrayList<>();
        private boolean ignoringActiveSpan;
        private Map<String, Object> initialTags = new HashMap<>();

        SpanBuilder(String operationName) {
            this.operationName = operationName;
        }

        @Override
        public SpanBuilder asChildOf(SpanContext parent) {
            return addReference(References.CHILD_OF, parent);
        }

        @Override
        public SpanBuilder asChildOf(Span parent) {
            if (parent == null) {
                return this;
            }
            return addReference(References.CHILD_OF, parent.context());
        }

        @Override
        public SpanBuilder ignoreActiveSpan() {
            ignoringActiveSpan = true;
            return this;
        }

        @Override
        public SpanBuilder addReference(String referenceType, SpanContext referencedContext) {
            if (referencedContext != null) {
                this.references.add(new MockSpan.Reference((MockSpan.MockContext) referencedContext, referenceType));
            }
            return this;
        }

        @Override
        public SpanBuilder withTag(String key, String value) {
            this.initialTags.put(key, value);
            return this;
        }

        @Override
        public SpanBuilder withTag(String key, boolean value) {
            this.initialTags.put(key, value);
            return this;
        }

        @Override
        public SpanBuilder withTag(String key, Number value) {
            this.initialTags.put(key, value);
            return this;
        }

        @Override
        public SpanBuilder withStartTimestamp(long microseconds) {
            this.startMicros = microseconds;
            return this;
        }

        @Override
        public Scope startActive(boolean finishOnClose) {
            return MockTracer.this.scopeManager().activate(this.startManual(), finishOnClose);
        }

        @Override
        public MockSpan start() {
            return startManual();
        }

        @Override
        public MockSpan startManual() {
            if (this.startMicros == 0) {
                this.startMicros = MockSpan.nowMicros();
            }
            SpanContext activeSpanContext = activeSpanContext();
            if(references.isEmpty() && !ignoringActiveSpan && activeSpanContext != null) {
                references.add(new MockSpan.Reference((MockSpan.MockContext) activeSpanContext, References.CHILD_OF));
            }
            return new MockSpan(MockTracer.this, operationName, startMicros, initialTags, references);
        }
    }
}
