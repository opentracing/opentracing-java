package io.opentracing;

import java.io.Closeable;

/**
 * In any execution context (or any thread, etc), there is at most one "activeSpan" {@link Span} primarily responsible for
 * the work accomplished by the surrounding application code. That activeSpan Span may be accessed via the
 * {@link ActiveSpanSource#activeSpan()} method. If the application needs to defer work that should be part of the same Span, the
 * Source provides a {@link ActiveSpan#defer} method that returns a {@link Continuation}; this continuation may be used
 * to re-activate and continue the {@link Span} in that other asynchronous executor and/or thread.
 *
 * <p>
 * {@link ActiveSpan}s are created via {@link Tracer.SpanBuilder#startActive()} or {@link ActiveSpanSource#adopt}. They can
 * be {@link ActiveSpan#defer()}ed as {@link ActiveSpan.Continuation}s, then re-{@link Continuation#activate()}d later.
 *
 * @see ActiveSpanSource
 */
public interface ActiveSpan extends Closeable, Span {
    /**
     * Mark the end of the activeSpan period for the {@link Span} pinned by this {@link ActiveSpan}. When the last
     * {@link ActiveSpan} is deactivated for a given {@link Span}, it is automatically {@link Span#finish()}ed.
     * <p>
     * <p>
     * NOTE: It is an error to call deactivate() more than once on a single {@link ActiveSpan}.
     *
     * @see Closeable#close() {@link ActiveSpan}s are auto-closeable and may be used in try-with-resources blocks
     */
    void deactivate();

    /**
     * "Fork" a new {@link Continuation} associated with this {@link ActiveSpan} and {@link Span}, as well as any
     * 3rd-party execution context of interest.
     * <p>
     * <p>
     * The associated {@link Span} will not {@link Span#finish()} while a {@link Continuation} is outstanding; in
     * this way, it provides a reference/pin just like an @{ActiveSpan} does.
     *
     * @return a new {@link Continuation} to {@link Continuation#activate()} at the appropriate time.
     */
    Continuation defer();

    /**
     * A {@link Continuation} can be used *once* to activate a Span along with any non-OpenTracing execution context
     * (e.g., MDC), then deactivate when processing activity moves on to another Span. (In practice, this activeSpan period
     * typically extends for the length of a deferred async closure invocation.)
     *
     * <p>
     * Most users do not directly interact with {@link Continuation}, {@link Continuation#activate()} or
     * {@link ActiveSpan#deactivate()}, but rather use {@link ActiveSpanSource}-aware Runnables/Callables/Executors.
     * Those higher-level primitives need not be defined within the OpenTracing core API, and so they are not.
     *
     * <p>
     * NOTE: {@link Continuation} extends {@link Closeable} rather than AutoCloseable in order to keep support
     * for JDK1.6.
     *
     * @see ActiveSpanSource#adopt(Span)
     */
    interface Continuation {
        /**
         * Make the Span (and other execution context) encapsulated by this Continuation activeSpan and return it.
         *
         * <p>
         * NOTE: It is an error to call activate() more than once on a single Continuation instance.
         *
         * @see ActiveSpanSource#adopt(Span)
         * @return a handle to the newly-activated Span
         */
        ActiveSpan activate();
    }

}
