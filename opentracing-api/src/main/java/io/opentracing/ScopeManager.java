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
 * The {@link ScopeManager} interface abstracts both the activation of {@link Span} instances (via
 * {@link ScopeManager#activate(Span)}) and access to an active {@link Span}/{@link Scope}
 * (via {@link ScopeManager#active()}).
 *
 * @see Scope
 * @see Tracer#setScopeManager(ScopeManager)
 */
public interface ScopeManager {
    /**
     * Make a {@link Span} instance active.
     *
     * @param span the {@link Span} that should become the {@link #active()}
     * @return a {@link Scope} instance to control the end of the active period for the {@link Span}. It is a
     * programming error to neglect to call {@link Scope#close()} on the returned instance.
     */
    Scope activate(Span span);
    Scope activate(Span span, Scope.Observer scopeObserver);

    /**
     * @return the currently active {@link Scope} which can be used to access the currently active
     * {@link Scope#span()}
     */
    Scope active();
}
