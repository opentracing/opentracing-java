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
package io.opentracing;

import io.opentracing.Tracer.SpanBuilder;

/**
 * The {@link ScopeManager} interface abstracts both the activation of {@link Span} instances via
 * {@link ScopeManager#activate(Span, boolean)} and access to an active {@link Span}/{@link Scope}
 * via {@link ScopeManager#active()}.
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
     * This {@link Scope} instance can be accessed at any time through {@link #active()},
     * in case it is not possible for the user to store it (when used through middleware
     * or start/finish event hooks, for example). The corresponding {@link Span} can be
     * accessed through {@link #activeSpan()} likewise. You can also get ahold of the
     * active {@link Span}'s {@link SpanContext} via {@link #activeSpanContext()}.
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
     * <p>
     * Note: You may only activate spans when you own its life cycle.
     * That means you must make sure that no other thread calls {@link Span#finish()}
     * while the scope is still active.
     * If you can't guarantee that, use {@link #activate(SpanContext)} instead.
     *
     * @param span the {@link Span} that should become the {@link #activeSpan()}
     * @return a {@link Scope} instance to control the end of the active period for the {@link Span}. It is a
     * programming error to neglect to call {@link Scope#close()} on the returned instance.
     */
    Scope activate(Span span);

    /**
     * Similar to {@link #activate(Span)} but used in cases where the thread in which the
     * activation is performed does not have control over the life cycle of the span.
     *
     * <p>
     * One example of that is when performing an activation in the {@link Runnable#run()}
     * method of a traced {@link Runnable} wrapper which is executed by an
     * {@link java.util.concurrent.ExecutorService}.
     *
     * <p>
     * The returned {@link Scope} represents the active state for the span.
     * Once its active period is due, {@link Scope#close()} ought to be called.
     * To ease this operation, {@link Scope} supports try-with-resources.
     * Observe the span will not be automatically finished when {@link Scope#close()}
     * is called.
     *
     * <p>
     * This {@link Scope} instance can be accessed at any time through {@link #active()},
     * in case it is not possible for the user to store it (when used through middleware
     * or start/finish event hooks, for example). The corresponding {@link SpanContext} can be
     * accessed through {@link #activeSpanContext()} likewise.
     * In contrast to {@link #activate(Span)}, {@link #activeSpan()} will return {@code null}.
     * This prevents users of the {@link #activeSpan()} API to accidentally interacting with
     * already {@linkplain Span#finish() finished} spans.
     *
     * Usage:
     * <pre><code>
     *     Span span = tracer.buildSpan("...").start();
     *     try (Scope scope = tracer.scopeManager().activate(span.context())) {
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
     * @param spanContext the {@link SpanContext} that should become the {@link #activeSpanContext()}
     * @see #activate(Span)
     * @return a {@link Scope} instance to control the end of the active period for the {@link Span}. It is a
     * programming error to neglect to call {@link Scope#close()} on the returned instance.
     */
    Scope activate(SpanContext spanContext);

    /**
     * Return the currently active {@link Scope} which can be used to deactivate the currently active
     * {@link Span}.
     *
     * <p>
     * Observe that {@link Scope} is expected to be used only in the same thread where it was
     * created, and thus should not be passed across threads.
     *
     * <p>
     * Because both {@link #active()} and {@link #activeSpan()} reference the current
     * active state, they both will be either null or non-null.
     *
     * @return the {@link Scope active scope}, or null if none could be found.
     */
    Scope active();

    /**
     * Return the currently active {@link Span}.
     *
     * <p>
     * Note that {@link #activeSpan()} can return {@code null} while {@link #active()} is non-null
     * in case of a {@linkplain #activate(SpanContext) span context activation}.
     *
     * @return the {@link Span active span}, or null if none could be found.
     */
    Span activeSpan();

    /**
     * Return the currently active {@link SpanContext}, which was activated by activating either
     * a {@link #activate(Span) Span} or a {@link #activate(SpanContext) SpanContext}.
     *
     * <p>
     * Because both {@link #active()} and {@link #activeSpanContext()} reference the current
     * active state, they both will be either null or non-null.
     *
     * @return the {@link SpanContext active span context}, or null if none could be found.
     */
    SpanContext activeSpanContext();

    /**
     * @deprecated use {@link #activate(Span)} instead.
     * Set the specified {@link Span} as the active instance for the current
     * context (usually a thread).
     *
     * <p>
     * Finishing the {@link Span} upon {@link Scope#close()} is discouraged,
     * as reporting errors becomes impossible:
     * <pre><code>
     *     try (Scope scope = tracer.scopeManager().activate(span, true)) {
     *     } catch (Exception e) {
     *         // Not possible to report errors, as
     *         // the span has been already finished.
     *     }
     * </code></pre>
     *
     * @param span the {@link Span} that should become the {@link #activeSpan()}
     * @param finishSpanOnClose whether span should automatically be finished when {@link Scope#close()} is called
     * @return a {@link Scope} instance to control the end of the active period for the {@link Span}. It is a
     * programming error to neglect to call {@link Scope#close()} on the returned instance.
     */
    @Deprecated
    Scope activate(Span span, boolean finishSpanOnClose);
}
