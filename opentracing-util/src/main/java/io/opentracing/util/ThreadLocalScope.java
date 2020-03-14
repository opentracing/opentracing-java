/*
 * Copyright 2016-2020 The OpenTracing Authors
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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link ThreadLocalScope} is a simple {@link Scope} implementation that relies on Java's
 * thread-local storage primitive.
 *
 * @see ScopeManager
 */
public class ThreadLocalScope implements Scope {
    private static final Logger log = Logger.getLogger(ThreadLocalScope.class.getName());

    private final ThreadLocalScopeManager scopeManager;
    private final Span wrapped;
    private final ThreadLocalScope toRestore;

    ThreadLocalScope(ThreadLocalScopeManager scopeManager, Span wrapped) {
        this.scopeManager = scopeManager;
        this.wrapped = wrapped;
        this.toRestore = scopeManager.tlsScope.get();
        scopeManager.tlsScope.set(this);
    }

    @Override
    public void close() {
        if (scopeManager.tlsScope.get() != this) {
            // This shouldn't happen if users call methods in the expected order. Bail out.
            log.log(Level.SEVERE, "Scope being closed is not the active instance.",
                new Throwable().fillInStackTrace());
            return;
        }

        scopeManager.tlsScope.set(toRestore);
    }

    Span span() {
        return wrapped;
    }
}
