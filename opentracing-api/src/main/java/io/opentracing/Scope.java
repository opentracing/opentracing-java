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
package io.opentracing;

import java.io.Closeable;

/**
 * A {@link Scope} formalizes the activation and deactivation of a {@link Span}, usually from a CPU standpoint.
 *
 * <p>
 * Many times a {@link Span} will be extant (in that {@link Span#finish()} has not been called) despite being in a
 * non-runnable state from a CPU/scheduler standpoint. For instance, a {@link Span} representing the client side of an
 * RPC will be unfinished but blocked on IO while the RPC is still outstanding. A {@link Scope} defines when a given
 * {@link Span} <em>is</em> scheduled and on the critical path.
 */
public interface Scope extends Closeable {
    /**
     * End this {@link Scope}, updating the {@link ScopeManager#active()} in the process.
     */
    @Override
    void close();

    /**
     * @return the {@link Span} that's been scoped by this {@link Scope}
     */
    Span span();

    /**
     * {@link Observer} is a simple API for observing the opening/closing of {@link Scope} instances.
     *
     * @see ScopeManager#activate(Span, Observer)
     * @see Tracer.SpanBuilder#startActive(Observer)
     * @see Span#activate(Observer)
     */
    interface Observer {
        /**
         * A trivial, static {@link Scope.Observer} that finishes the underlying {@link Span} on scope close.
         */
        Observer FINISH_ON_CLOSE = new FinishOnCloseScopeObserverImpl();

        /**
         * Invoked just after the {@link Scope} becomes active.
         */
        void onActivate(Scope scope);

        /**
         * Invoked just before the {@link Scope} closes / is deactivated.
         */
        void onClose(Scope scope);
    }
}

/**
 * @see Scope.Observer#FINISH_ON_CLOSE
 */
class FinishOnCloseScopeObserverImpl implements Scope.Observer {
    @Override
    public void onActivate(Scope scope) {}

    @Override
    public void onClose(Scope scope) {
        scope.span().finish();
    }
}
