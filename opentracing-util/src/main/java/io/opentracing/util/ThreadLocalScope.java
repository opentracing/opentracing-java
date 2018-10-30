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
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;

/**
 * {@link ThreadLocalScope} is a simple {@link Scope} implementation that relies on Java's
 * thread-local storage primitive.
 *
 * @see ScopeManager
 */
public class ThreadLocalScope implements Scope {
    private final ThreadLocalScopeManager scopeManager;
    private final Span wrapped;
    private final boolean finishOnClose;
    private final ThreadLocalScope toRestore;
    private final SpanContext spanContext;

    ThreadLocalScope(ThreadLocalScopeManager scopeManager, Span wrapped) {
        this(scopeManager, wrapped, null, false);
    }

    ThreadLocalScope(ThreadLocalScopeManager scopeManager, SpanContext spanContext) {
        this(scopeManager, null, spanContext, false);
    }

    ThreadLocalScope(ThreadLocalScopeManager scopeManager, Span wrapped, boolean finishOnClose) {
        this(scopeManager, wrapped, null, finishOnClose);
    }

    private ThreadLocalScope(ThreadLocalScopeManager scopeManager, Span wrapped, SpanContext spanContext, boolean finishOnClose) {
        this.scopeManager = scopeManager;
        this.wrapped = wrapped;
        this.finishOnClose = finishOnClose;
        this.toRestore = scopeManager.tlsScope.get();
        this.spanContext = spanContext;
        scopeManager.tlsScope.set(this);
    }

    @Override
    public void close() {
        if (scopeManager.tlsScope.get() != this) {
            // This shouldn't happen if users call methods in the expected order. Bail out.
            return;
        }

        if (finishOnClose) {
            wrapped.finish();
        }

        scopeManager.tlsScope.set(toRestore);
    }

    @Override
    public Span span() {
        return wrapped;
    }

    SpanContext spanContext() {
        return wrapped != null ? wrapped.context() : spanContext;
    }
}
