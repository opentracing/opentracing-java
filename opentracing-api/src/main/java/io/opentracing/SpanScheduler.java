package io.opentracing;

/**
 * Scheduler allows an existing (possibly thread-local-aware) execution context provider to also manage an
 * actively-scheduled OpenTracing Span.
 * <p>
 * In any execution context (or any thread, etc), there is at most one "active" Span primarily responsible for the
 * work accomplished by the surrounding application code. That active Span may be accessed via the
 * {@link Scheduler#active()} method. If the application needs to defer work that should be part of the same
 * Span, the Scheduler provides a {@link Scheduler#captureActive()} method that returns a
 * {@link Continuation}; this activation context may be used to re-activate and deactivate the
 * captured Span in that other asynchronous executor and/or thread.
 */
public interface Scheduler {

    /**
     * A Continuation can be used *once* to activate a Span and other execution context, then deactivate once the
     * active period has concluded. (In practice, this active period typically extends for the length of a deferred
     * async closure invocation.)
     * <p>
     * Most users do not directly interact with Continuation, activate(), or deactivate(), but rather use
     * Scheduler-aware Runnables/Callables/Executors. Those higher-level primitives need not be defined within the
     * OpenTracing core API.
     *
     * @see Scheduler#capture(Span)
     */
    interface Continuation extends AutoCloseable {

        /**
         * Make the Span (and other execution context) encapsulated by this Continuation active and return it.
         * <p>
         * NOTE: It is an error to call activate() more than once on a single Continuation instance.
         *
         * @param finishOnDeactivate true if.f. the span should be finish()ed when the Continuation is deactivated
         *
         * @see Scheduler#capture(Span)
         * @return the newly-activated Span
         */
        Span activate(boolean finishOnDeactivate);

        /**
         * Mark the end of this active period for the Span previously returned by activate().
         * <p>
         * NOTE: It is an error to call deactivate() more than once on a single Continuation instance.
         *
         * @see AutoCloseable#close()
         */
        void deactivate();

    }

    /**
     * @return the currently active Span for this Scheduler, or null if there is no such Span
     */
    Span active();

    /**
     * Explicitly capture the newly-started Span along with any active state (e.g., MDC state) from the current
     * execution context.
     *
     * @param span the Span just started
     * @return a Continuation that represents the active Span and any other Scheduler-specific context, even if the
     *     active Span is null.
     */
    Continuation capture(Span span);

    // (Convenience methods follow)

    /**
     * A shorthand for <pre>{@code
     *     Span active = this.active();
     *     if (active == null) return null;
     *     return active.context();
     * }</pre>@code{}
     * @return the currently active SpanContext, or null if there is no active Span
     */
    SpanContext activeContext();

    /**
     * A shorthand for <pre>{@code
     *     this.capture(this.active())
     * }</pre>@code{}
     *
     * @return a Continuation wrapped around this.active()
     */
    Continuation captureActive();
}
