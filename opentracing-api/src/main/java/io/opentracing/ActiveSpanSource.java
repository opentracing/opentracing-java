package io.opentracing;

import java.io.Closeable;

/**
 * {@link ActiveSpanSource} allows an existing (possibly thread-local-aware) execution context provider to act as a
 * source for an actively-scheduled OpenTracing Span.
 *
 * <p>
 * In any execution context (or any thread, etc), there is at most one "active" Span primarily responsible for the
 * work accomplished by the surrounding application code. That active Span may be accessed -- via an
 * {@link ActiveSpanSource.Handle} -- with the {@link ActiveSpanSource#active()} method. If the application needs
 * to defer work that should be part of the same Span, the ActiveSpanSource provides a
 * {@link Handle#defer} method that returns a {@link Continuation}; this continuation may be used to re-activate and
 * the Span in that other asynchronous executor and/or thread.
 *
 * <p>
 * There are two important use cases for {@link ActiveSpanSource} and {@link ActiveSpanSource.Continuation}:
 * <ul>
 *
 *     <li>Accessing the active {@link Handle}/{@link Span}: first, call {@link Tracer#spanSource()}, then use
 *     {@link ActiveSpanSource#active()} and {@link Handle#span}.
 *
 *     <li>Propagating the active {@link Handle} to another (async) executor. First, call {@link Handle#defer()} to
 *     defer a reference to the active Span, then pass that to the async method (even via a final local variable
 *     that's used within a closure). Within that closure, code should call {@link Continuation#activate()} to install
 *     the deferred {@link Handle}/{@link Span} for subsequent calls to {@link ActiveSpanSource#active)}. (Helper
 *     libraries can abstract away much of the above behind {@link java.util.concurrent.ExecutorService} wrappers)
 *
 * </ul>
 */
public interface ActiveSpanSource {

    interface Handle extends Closeable {
        /**
         * @return the active {@link Span} pinned by this {@link Handle}.
         */
        Span span();

        /**
         * Mark the end of the active period for the {@link Span} pinned by this {@link Handle}. When the last
         * {@link Handle} is deactivated for a given {@link Span}, it is automatically {@link Span#finish()}ed.
         *
         * <p>
         * NOTE: It is an error to call deactivate() more than once on a single {@link Handle}.
         *
         * @see Closeable#close() {@link Handle}s are auto-closeable and may be used in try-with-resources blocks
         */
        void deactivate();

        /**
         * "Fork" a new {@link Continuation} associated with this {@link Handle} and {@link Span}, as well as any
         * 3rd-party execution context of interest.
         *
         * <p>
         * The associated {@link Span} will not {@link Span#finish()} while a {@link Continuation} is outstanding; in
         * this way, it provides a reference/pin just like an active @{Handle} does.
         *
         * @return a new {@link Continuation} to {@link Continuation#activate()} at the appropriate time.
         */
        Continuation defer();
    }

    /**
     * A {@link Continuation} can be used *once* to activate a Span along with any non-OpenTracing execution context
     * (e.g., MDC), then deactivate when processing activity moves on to another Span. (In practice, this active period
     * typically extends for the length of a deferred async closure invocation.)
     *
     * <p>
     * Most users do not directly interact with {@link Continuation}, {@link Continuation#activate()} or
     * {@link Handle#deactivate()}, but rather use {@link ActiveSpanSource}-aware Runnables/Callables/Executors.
     * Those higher-level primitives need not be defined within the OpenTracing core API, and so they are not.
     *
     * <p>
     * NOTE: {@link Continuation} extends {@link java.io.Closeable} rather than AutoCloseable in order to keep support
     * for JDK1.6.
     *
     * @see ActiveSpanSource#adopt(Span)
     */
    interface Continuation {
        /**
         * Make the Span (and other execution context) encapsulated by this Continuation active and return it.
         *
         * <p>
         * NOTE: It is an error to call activate() more than once on a single Continuation instance.
         *
         * @see ActiveSpanSource#adopt(Span)
         * @return a handle to the newly-activated Span
         */
        Handle activate();
    }

    /**
     * @return the active {@link Handle}, or null if none could be found. This does not affect the reference count for
     * the {@link Handle}.
     */
    Handle active();

    /**
     * Wrap and "adopt" a @{link Span} by encapsulating it – and any active state (e.g., MDC state) in the execution
     * context – in a new @{link Handle}.
     *
     * @param span the Span just started
     * @return a @{link Handle} that encapsulates the given Span and any other ActiveSpanSource-specific context (e.g.,
     * MDC data)
     */
    Handle adopt(Span span);
}
