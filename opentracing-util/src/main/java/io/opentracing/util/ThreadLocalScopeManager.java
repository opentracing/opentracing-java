/*
 * Copyright 2016-2019 The OpenTracing Authors
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

/**
 * A simple {@link ScopeManager} implementation built on top of Java's thread-local storage primitive.
 * <p>
 * Optionally supports {@link ScopeListener}, to perform additional actions when scope is changed for given thread.
 * Listener methods are always called synchronously on the same thread, right after activation (meaning {@link #active()}
 * already returns new a scope).
 *
 * @see ThreadLocalScope
 */
public class ThreadLocalScopeManager implements ScopeManager {

    final ThreadLocal<ThreadLocalScope> tlsScope = new ThreadLocal<ThreadLocalScope>();
    final ScopeListener listener;

    /**
     * Default constructor for {@link ThreadLocalScopeManager}, without any listener.
     */
    public ThreadLocalScopeManager() {
        this(null);
    }

    /**
     * Constructs {@link ThreadLocalScopeManager} with custom {@link ScopeListener}.
     *
     * @param listener Listener to register. When null, noop listener will be used.
     */
    public ThreadLocalScopeManager(ScopeListener listener) {
        this.listener = listener != null ? listener : NoopScopeListener.INSTANCE;
    }

    @Override
    public Scope activate(Span span, boolean finishOnClose) {
        return new ThreadLocalScope(this, span, finishOnClose);
    }

    @Override
    public Scope active() {
        return tlsScope.get();
    }
}
