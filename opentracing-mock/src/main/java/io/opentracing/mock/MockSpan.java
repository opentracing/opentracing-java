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
package io.opentracing.mock;

import io.opentracing.Span;
import io.opentracing.SpanContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MockSpans are created via MockTracer.buildSpan(...), but they are also returned via calls to
 * MockTracer.finishedSpans(). They provide accessors to all Span state.
 *
 * @see MockTracer#finishedSpans()
 */
public final class MockSpan implements Span {
    // A simple-as-possible (consecutive for repeatability) id generator.
    private static AtomicLong nextId = new AtomicLong(0);

    private final MockTracer mockTracer;
    private MockContext context;
    private final long parentId; // 0 if there's no parent.
    private final long startMicros;
    private long finishMicros;
    private final Map<String, Object> tags;
    private final List<LogEntry> logEntries = new ArrayList<>();
    private final String operationName;

    public String operationName() {
        return this.operationName;
    }
    /**
     * TODO: Support multiple parents in this API.
     *
     * @return the spanId of the Span's parent context, or 0 if no such parent exists.
     *
     * @see MockContext#spanId()
     */
    public long parentId() {
        return parentId;
    }
    public long startMicros() {
        return startMicros;
    }
    /**
     * @return the finish time of the Span; only valid after a call to finish().
     */
    public long finishMicros() {
        assert finishMicros > 0 : "must call finish() before finishMicros()";
        return finishMicros;
    }

    /**
     * @return a copy of all tags set on this Span.
     */
    public Map<String, Object> tags() {
        return new HashMap<>(this.tags);
    }
    /**
     * @return a copy of all log entries added to this Span.
     */
    public List<LogEntry> logEntries() {
        return new ArrayList<>(this.logEntries);
    }

    @Override
    public synchronized MockContext context() {
        return this.context;
    }

    @Override
    public void finish() {
        this.finish(System.nanoTime() / 1000);
    }

    @Override
    public synchronized void finish(long finishMicros) {
        this.finishMicros = finishMicros;
        this.mockTracer.appendFinishedSpan(this);
    }

    @Override
    public void close() {
        this.finish();
    }

    @Override
    public synchronized Span setTag(String key, String value) {
        this.tags.put(key, value);
        return this;
    }

    @Override
    public synchronized Span setTag(String key, boolean value) {
        this.tags.put(key, value);
        return this;
    }

    @Override
    public synchronized Span setTag(String key, Number value) {
        this.tags.put(key, value);
        return this;
    }

    @Override
    public Span log(String eventName, Object payload) {
        return this.log(System.nanoTime() / 1000, eventName, payload);
    }

    @Override
    public synchronized Span log(long timestampMicroseconds, String eventName, Object payload) {
        this.logEntries.add(new LogEntry(timestampMicroseconds, eventName, payload));
        return this;
    }

    @Override
    public synchronized Span setBaggageItem(String key, String value) {
        this.context = this.context.withBaggageItem(key, value);
        return this;
    }

    @Override
    public synchronized String getBaggageItem(String key) {
        return this.context.getBaggageItem(key);
    }

    /**
     * MockContext implements a Dapper-like opentracing.SpanContext with a trace- and span-id.
     *
     * Note that parent ids are part of the MockSpan, not the MockContext (since they do not need to propagate
     * between processes).
     */
    public static class MockContext implements SpanContext {
        private final long traceId;
        private final Map<String, String> baggage;
        private final long spanId;

        /**
         * A package-protected constructor to create a new MockContext. This should only be called by MockSpan and/or
         * MockTracer.
         *
         * @param baggage the MockContext takes ownership of the baggage parameter
         *
         * @see MockContext#withBaggageItem(String, String)
         */
        MockContext(long traceId, long spanId, Map<String, String> baggage) {
            this.baggage = baggage;
            this.traceId = traceId;
            this.spanId = spanId;
        }

        public String getBaggageItem(String key) { return this.baggage.get(key); }
        public long traceId() { return traceId; }
        public long spanId() { return spanId; }

        /**
         * Create and return a new (immutable) MockContext with the added baggage item.
         */
        public MockContext withBaggageItem(String key, String val) {
            Map<String, String> newBaggage = new HashMap<>(this.baggage);
            newBaggage.put(key, val);
            return new MockContext(this.traceId, this.spanId, newBaggage);
        }

        @Override
        public Iterable<Map.Entry<String, String>> baggageItems() {
            return baggage.entrySet();
        }
    }

    public static class LogEntry {
        private final long timestampMicros;
        private final String eventName;
        private final Object payload;

        public LogEntry(long timestampMicros, String eventName, Object payload) {
            this.timestampMicros = timestampMicros;
            this.eventName = eventName;
            this.payload = payload;
        }

        public long timestampMicros() {
            return timestampMicros;
        }

        public String eventName() {
            return eventName;
        }

        public Object payload() {
            return payload;
        }
    }

    MockSpan(MockTracer tracer, String operationName, long startMicros, Map<String, Object> initialTags, MockContext parent) {
        this.mockTracer = tracer;
        this.operationName = operationName;
        this.startMicros = startMicros;
        if (initialTags == null) {
            this.tags = new HashMap<>();
        } else {
            this.tags = new HashMap<>(initialTags);
        }
        if (parent == null) {
            // We're a root Span.
            this.context = new MockContext(nextId(), nextId(), new HashMap<String, String>());
            this.parentId = 0;
        } else {
            // We're a child Span.
            this.context = new MockContext(parent.traceId, nextId(), parent.baggage);
            this.parentId = parent.spanId;
        }
    }

    static long nextId() {
        return nextId.addAndGet(1);
    }
}
