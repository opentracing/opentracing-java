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
package io.opentracing.usecases;

import io.opentracing.ContinuableScope;
import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by bhs on 8/5/17.
 */
public class AutoFinishScopeManager implements ScopeManager {
    final ThreadLocal<AutoFinishScope> tlsScope = new ThreadLocal<AutoFinishScope>();

    @Override
    public AutoFinishScope activate(Span span) {
        return new AutoFinishScope(new AtomicInteger(1), span);
    }

    @Override
    public AutoFinishScope activate(Span span, Scope.Observer observer) {
        return new AutoFinishScope(new AtomicInteger(1), span);
    }

    @Override
    public AutoFinishScope active() {
        return tlsScope.get();
    }

    @Override
    public boolean canDefer() {
      return true;
    }

    public class AutoFinishScope implements ContinuableScope {
        final AtomicInteger refCount;
        private final Span wrapped;
        private final AutoFinishScope toRestore;

        AutoFinishScope(AtomicInteger refCount, Span wrapped) {
            this.refCount = refCount;
            this.wrapped = wrapped;
            this.toRestore = AutoFinishScopeManager.this.tlsScope.get();
            tlsScope.set(this);
        }

        public class Continuation implements ContinuableScope.Continuation {
            public Continuation() {
                refCount.incrementAndGet();
            }

            public AutoFinishScope activate() {
                return new AutoFinishScope(refCount, wrapped);
            }
        }

        @Override
        public Continuation defer() {
            return new Continuation();
        }

        @Override
        public void close() {
            if (refCount.decrementAndGet() == 0) {
                wrapped.finish();
            }
            tlsScope.set(toRestore);
        }

        @Override
        public Span span() {
            return wrapped;
        }
    }
}
