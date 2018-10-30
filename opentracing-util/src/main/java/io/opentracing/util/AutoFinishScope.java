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

package io.opentracing.util;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;

import java.util.concurrent.atomic.AtomicInteger;

public class AutoFinishScope implements Scope {
    final AutoFinishScopeManager manager;
    final AtomicInteger refCount;
    private final Span wrapped;
    private final AutoFinishScope toRestore;
    private final SpanContext spanContext;

    AutoFinishScope(AutoFinishScopeManager manager, AtomicInteger refCount, Span wrapped) {
        this(manager, refCount, wrapped, null);
    }

    AutoFinishScope(AutoFinishScopeManager manager, SpanContext spanContext) {
        this(manager, new AtomicInteger(), null, spanContext);
    }

    private AutoFinishScope(AutoFinishScopeManager manager, AtomicInteger refCount, Span wrapped, SpanContext spanContext) {
        this.manager = manager;
        this.refCount = refCount;
        this.wrapped = wrapped;
        this.toRestore = manager.tlsScope.get();
        this.spanContext = spanContext;
        manager.tlsScope.set(this);
    }

    public class Continuation {
        public Continuation() {
            refCount.incrementAndGet();
        }

        public AutoFinishScope activate() {
            return new AutoFinishScope(manager, refCount, wrapped);
        }
    }

    public Continuation capture() {
        return new Continuation();
    }

    @Override
    public void close() {
        if (manager.tlsScope.get() != this) {
            return;
        }

        if (refCount.decrementAndGet() == 0 && wrapped != null) {
            wrapped.finish();
        }

        manager.tlsScope.set(toRestore);
    }

    @Override
    public Span span() {
        return wrapped;
    }

    SpanContext spanContext() {
        return wrapped != null ? wrapped.context() : spanContext;
    }
}
