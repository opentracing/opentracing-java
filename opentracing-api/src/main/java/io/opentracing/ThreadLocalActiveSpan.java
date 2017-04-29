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
package io.opentracing;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

 /**
 * {@link ThreadLocalActiveSpan} is a trivial {@link ActiveSpan} implementation that relies on Java's thread-local
 * storage primitive.
 *
 * @see ActiveSpanSource
 * @see Tracer#activeSpan()
 */
public class ThreadLocalActiveSpan implements ActiveSpan {
    private final ThreadLocalActiveSpanSource source;
    private final Span wrapped;
    private final ThreadLocalActiveSpan toRestore;
    private final AtomicInteger refCount;

    ThreadLocalActiveSpan(ThreadLocalActiveSpanSource source, Span wrapped, AtomicInteger refCount) {
        this.source = source;
        this.refCount = refCount;
        this.wrapped = wrapped;
        this.toRestore = source.tlsSnapshot.get();
        source.tlsSnapshot.set(this);
    }

    @Override
    public void deactivate() {
        if (source.tlsSnapshot.get() != this) {
            // This shouldn't happen if users call methods in the expected order. Bail out.
            return;
        }
        source.tlsSnapshot.set(toRestore);

        if (0 == refCount.decrementAndGet()) {
            wrapped.finish();
        }
    }

    @Override
    public Continuation capture() {
        return new ThreadLocalActiveSpan.Continuation();
    }

    @Override
    public SpanContext context() {
        return wrapped.context();
    }

    @Override
    public BaseSpan setTag(String key, String value) {
        return wrapped.setTag(key, value);
    }

    @Override
    public BaseSpan setTag(String key, boolean value) {
        return wrapped.setTag(key, value);
    }

    @Override
    public BaseSpan setTag(String key, Number value) {
        return wrapped.setTag(key, value);
    }

    @Override
    public BaseSpan log(Map<String, ?> fields) {
        return wrapped.log(fields);
    }

    @Override
    public BaseSpan log(long timestampMicroseconds, Map<String, ?> fields) {
        return wrapped.log(timestampMicroseconds, fields);
    }

    @Override
    public BaseSpan log(String event) {
        return wrapped.log(event);
    }

    @Override
    public BaseSpan log(long timestampMicroseconds, String event) {
        return wrapped.log(timestampMicroseconds, event);
    }

    @Override
    public BaseSpan setBaggageItem(String key, String value) {
        return wrapped.setBaggageItem(key, value);
    }

    @Override
    public String getBaggageItem(String key) {
        return wrapped.getBaggageItem(key);
    }

    @Override
    public BaseSpan setOperationName(String operationName) {
        return wrapped.setOperationName(operationName);
    }

    @Override
    public BaseSpan log(String eventName, Object payload) {
        return wrapped.log(eventName, payload);
    }

    @Override
    public BaseSpan log(long timestampMicroseconds, String eventName, Object payload) {
        return wrapped.log(timestampMicroseconds, eventName, payload);
    }

    @Override
    public void close() {
        deactivate();
    }

    private final class Continuation implements ActiveSpan.Continuation {
        Continuation() {
            refCount.incrementAndGet();
        }

        @Override
        public ThreadLocalActiveSpan activate() {
            return new ThreadLocalActiveSpan(source, wrapped, refCount);
        }
    }

 }
