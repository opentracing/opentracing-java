/*
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
package io.opentracing.util;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;

/**
 * Created by bhs on 8/1/17.
 */
public class ThreadLocalScopeManager implements ScopeManager {
    final ThreadLocal<ThreadLocalScope> tlsScope = new ThreadLocal<ThreadLocalScope>();

    public class ThreadLocalScope implements Scope {
        private final Span wrapped;
        private final ThreadLocalScope toRestore;
        private final Scope.Observer scopeObserver;

        ThreadLocalScope(Span wrapped, Scope.Observer scopeObserver) {
            this.wrapped = wrapped;
            this.toRestore = ThreadLocalScopeManager.this.tlsScope.get();
            this.scopeObserver = scopeObserver;
            tlsScope.set(this);
            if (this.scopeObserver != null) {
                this.scopeObserver.onActivate(this);
            }
        }

        @Override
        public void close() {
            if (this.scopeObserver != null) {
                this.scopeObserver.onClose(this);
            }
            tlsScope.set(toRestore);
        }

        @Override
        public Span span() {
            return wrapped;
        }
    }

    @Override
    public Scope activate(Span span) {
        return new ThreadLocalScope(span, null);
    }

    @Override
    public Scope activate(Span span, Scope.Observer scopeObserver) {
        return new ThreadLocalScope(span, scopeObserver);
    }

    @Override
    public Scope active() {
        return tlsScope.get();
    }
}
