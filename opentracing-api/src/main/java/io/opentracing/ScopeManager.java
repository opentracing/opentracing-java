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
package io.opentracing;

import io.opentracing.Tracer.SpanBuilder;

/**
 * The {@link ScopeManager} interface abstracts both the activation of {@link Span} instances via
 * {@link ScopeManager#activate(Span)} and access to an active {@link Span}
 * via {@link ScopeManager#activeSpan()}.
 *
 * @see Scope
 * @see Tracer#scopeManager()
 */
public interface ScopeManager {

    /**
     * Set the specified {@link Span} as the active instance for the current
     * context (usually a thread).
     *
     * <p>
     * The returned {@link Scope} represents the active state for the span.
     * Once its active period is due, {@link Scope#close()} ought to be called.
     * To ease this operation, {@link Scope} supports try-with-resources.
     * Observe the span will not be automatically finished when {@link Scope#close()}
     * is called.
     *
     * <p>
     * The corresponding {@link Span} can be accessed at any time through {@link #activeSpan()}.
     *
     * <p>
     * Usage:
     * <pre><code>
     *     Span span = tracer.buildSpan("...").start();
     *     try (Scope scope = tracer.scopeManager().activate(span)) {
     *         span.setTag("...", "...");
     *         ...
     *     } catch (Exception e) {
     *         span.log(...);
     *     } finally {
     *         // Optionally finish the Span if the operation it represents
     *         // is logically completed at this point.
     *         span.finish();
     *     }
     * </code></pre>
     *
     * @param span the {@link Span} that should become the {@link #activeSpan()}
     * @return a {@link Scope} instance to control the end of the active period for the {@link Span}. It is a
     * programming error to neglect to call {@link Scope#close()} on the returned instance.
     */
    Scope activate(Span span);

    /**
     * Return the currently active {@link Span}.
     *
     * <p>
     * Because both {@link #active()} and {@link #activeSpan()} reference the current
     * active state, they both will be either null or non-null.
     *
     * @return the {@link Span active span}, or null if none could be found.
     */
    Span activeSpan();
}
