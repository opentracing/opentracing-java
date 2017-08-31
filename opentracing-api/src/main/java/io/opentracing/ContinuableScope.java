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

/**
 * A {@link ContinuableScope} formalizes the deferral of state of a {@link Span} for cross-thread scenarios.
 *
 * <p>
 * When doing cross-thread tracing, a {@link Span} state may need to be deferred and later recovered on a
 * different thread. This interface formalizes this advanced scenario, by extending {@link Scope} and
 * adding {@link ContinuableScope#defer()} and exposing {@link Continuation} used to reactivate
 * the {@link Span}.
 */
public interface ContinuableScope extends Scope {
    /**
     * "Capture" a new {@link Continuation} associated with this {@link Scope} and {@link Span}, as well as any
     * 3rd-party execution context of interest. The {@link Continuation} may be used as data in a closure or callback
     * function where the {@link Scope} may be resumed and reactivated.
     */
    Continuation defer();

    /**
     * A {@link Continuation} can be used <em>once</em> to activate a Span along with any non-OpenTracing execution
     * context (e.g., MDC), then deactivate when processing activity moves on to another Span. (In practice, this
     * active period typically extends for the length of a deferred async closure invocation.)
     */
    interface Continuation {
        /**
         * Make the Span (and other execution context) encapsulated by this {@link Continuation} active and
         * return it.
         *
         * <p>
         * NOTE: It is an error to call activate() more than once on a single Continuation instance.
         *
         * @see ScopaManager#activate(Span)
         * @return a handle to the newly-activated {@link Scope}
         */
        Scope activate();
    }
}
