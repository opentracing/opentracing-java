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
 * {@link ActiveSpan} inherits all of the OpenTracing functionality in {@link Span} and layers on in-process
 * propagation capabilities.
 *
 * <p>
 * In any given thread there is at most one {@link ActiveSpan "active" span} primarily responsible for the work
 * accomplished by the surrounding application code. That {@link ActiveSpan} may be accessed via the
 * {@link ActiveSpanSource#activeSpan()} method. If the application needs to defer work that should be part of
 * the same Span, the Source provides a {@link ActiveSpan#capture} method that returns a {@link Continuation};
 * this continuation may be used to re-activate and continue the {@link Span} in that other asynchronous executor
 * and/or thread.
 *
 * <p>
 * {@link ActiveSpan}s are created via {@link Tracer.SpanBuilder#startActive()} or, less commonly,
 * {@link ActiveSpanSource#makeActive}. Per the above, they can be {@link ActiveSpan#capture()}ed as
 * {@link ActiveSpan.Continuation}s, then re-{@link Continuation#activate()}d later.
 *
 * <p>
 * Propagated {@link ActiveSpan} instances form a tree within a process, where each node is an {@link ActiveSpan} and
 * each edge is a {@link ActiveSpan.Continuation}. The {@link ActiveSpan.Observer} interface provides visibility into
 * the node/edge creations as well as {@link ActiveSpan} activation/deactivation lifecycles.
 *
 * <p>
 * NOTE: {@link ActiveSpan} extends {@link Closeable} rather than {@code AutoCloseable} in order to preserve support
 * for JDK1.6.
 *
 * @see Tracer.SpanBuilder#startActive()
 * @see Continuation#activate()
 * @see ActiveSpanSource
 * @see Span
 * @see Span
 */
public interface ActiveSpan extends Closeable, Span {
    /**
     * Mark the end of the active period for the current thread and {@link ActiveSpan}. When the last
     * {@link ActiveSpan} is deactivated for a given {@link Span}, it is automatically {@link Span#finish()}ed.
     *
     * <p>
     * NOTE: Calling {@link #deactivate} more than once on a single {@link ActiveSpan} instance leads to undefined
     * behavior.
     *
     * @see Closeable#close() {@link ActiveSpan}s are auto-closeable and may be used in try-with-resources blocks
     */
    void deactivate();

    /**
     * A synonym for {@link #deactivate()} that can be used in try-with-resources blocks.
     */
    @Override
    void close();

    /**
     * "Capture" a new {@link Continuation} associated with this {@link ActiveSpan} and {@link Span}, as well as any
     * 3rd-party execution context of interest. The {@link Continuation} may be used as data in a closure or callback
     * function where the {@link ActiveSpan} may be resumed and reactivated.
     *
     * <p>
     * <em>IMPORTANT:</em> the caller MUST {@link Continuation#activate()} and {@link ActiveSpan#deactivate()} the
     * returned {@link Continuation} or the associated {@link Span} will never automatically {@link Span#finish()}.
     * That is, calling {@link #capture()} increments a refcount that must be decremented somewhere else.
     *
     * <p>
     * The associated {@link Span} will not {@link Span#finish()} while a {@link Continuation} is outstanding; in
     * this way, it provides a reference/pin just like an {@link ActiveSpan} does.
     *
     * @return a new {@link Continuation} to {@link Continuation#activate()} at the appropriate time.
     */
    Continuation capture();

    Span wrapped();

    /**
     * A {@link Continuation} can be used <em>once</em> to activate a Span along with any non-OpenTracing execution
     * context (e.g., MDC), then deactivate when processing activity moves on to another Span. (In practice, this
     * active period typically extends for the length of a deferred async closure invocation.)
     *
     * <p>
     * Most users do not directly interact with {@link Continuation}, {@link Continuation#activate()} or
     * {@link ActiveSpan#deactivate()}, but rather use {@link ActiveSpanSource}-aware Runnables/Callables/Executors.
     * Those higher-level primitives do not <em>need</em> to be defined within the OpenTracing core API, and so
     * they are not.
     *
     * @see ActiveSpanSource#makeActive(Span)
     */
    interface Continuation {
        /**
         * Make the Span (and other execution context) encapsulated by this {@link Continuation} active and
         * return it.
         *
         * <p>
         * NOTE: It is an error to call activate() more than once on a single Continuation instance.
         *
         * @see ActiveSpanSource#makeActive(Span)
         * @return a handle to the newly-activated {@link ActiveSpan}
         */
        ActiveSpan activate();
    }

    /**
     * The {@link ActiveSpan.Observer} interface learns of capture, activate, and deactivate calls for a given
     * {@link ActiveSpan} instance and any of its "descendants" from a capture/activate standpoint.
     */
    interface Observer {
        void onCapture(ActiveSpan captured, Continuation destination);
        void onActivate(Continuation source, ActiveSpan justActivated);
        void onDeactivate(ActiveSpan activeSpan);
    }
}
