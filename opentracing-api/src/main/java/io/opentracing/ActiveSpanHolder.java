package io.opentracing;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link ActiveSpanHolder} allows an existing (possibly thread-local-aware) execution context provider to also manage an
 * actively-scheduled OpenTracing Span.
 *
 * <p>
 * In any execution context (or any thread, etc), there is at most one "active" Span primarily responsible for the
 * work accomplished by the surrounding application code. That active Span may be accessed via the
 * {@link ActiveSpanHolder#active()} method. If the application needs to defer work that should be part of the same
 * Span, the ActiveSpanHolder provides a {@link ActiveSpanHolder#capture(Span)} method that returns a
 * {@link Continuation}; this continuation / activation context may be used to re-activate and deactivate the
 * captured Span in that other asynchronous executor and/or thread.
 *
 * <p>
 * There are two important use cases for {@link ActiveSpanHolder} and {@link ActiveSpanHolder.Continuation}:
 * <ul>
 *
 *     <li>Accessing the active {@link ActiveSpanHolder.Continuation}/{@link Span}/{@link SpanContext}: first, call
 *     {@link Tracer#activeSpanHolder()}, then use {@link ActiveSpanHolder#active()} / {@link ActiveSpanHolder#activeSpan()} /
 *     {@link ActiveSpanHolder#activeContext()}
 *
 *     <li>Propagating the active {@link ActiveSpanHolder.Continuation}/{@link Span} to another (async) executor. First,
 *     call {@link Continuation#capture()} to capture a reference to the active Span, then pass that to the async
 *     method (even via a final local variable that's used within a closure). Within that closure, code should call
 *     {@link Continuation#activate()} to install the captured {@link ActiveSpanHolder.Continuation}/{@link Span} for
 *     subsequent calls to {@linkplain ActiveSpanHolder#active active*()}. (Helper libraries can abstract away much
 *     of the above behind {@link java.util.concurrent.ExecutorService} wrappers)
 *
 * </ul>
 */
public abstract class ActiveSpanHolder {

    /**
     * A {@link Continuation} can be used *once* to activate a Span and some non-OpenTracing execution context (e.g.,
     * MDC), then deactivate when processing activity moves on to another Span. (In practice, this active period
     * typically extends for the length of a deferred async closure invocation.)
     *
     * <p>
     * Most users do not directly interact with {@link Continuation}, {@link Continuation#activate()} or
     * {@link Continuation#deactivate()}, but rather use {@link ActiveSpanHolder}-aware Runnables/Callables/Executors.
     * Those higher-level primitives need not be defined within the OpenTracing core API, and so they are not.
     *
     * <p>
     * NOTE: {@link Continuation} extends {@link java.io.Closeable} rather than AutoCloseable in order to keep support
     * for JDK1.6.
     *
     * @see ActiveSpanHolder#capture(Span)
     */
    public static abstract class Continuation implements Closeable {
        private final AtomicInteger refCount;

        protected Continuation(AtomicInteger refCount) {
            this.refCount = refCount;
        }

        /**
         * Make the Span (and other execution context) encapsulated by this Continuation active and return it.
         *
         * <p>
         * NOTE: It is an error to call activate() more than once on a single Continuation instance.
         *
         * @see ActiveSpanHolder#capture(Span)
         * @return the newly-activated Span
         */
        public abstract void activate();

        /**
         * @return the {@link Span} associated with this {@link Continuation}, or null if there is no such {@link Span}.
         */
        public abstract Span span();

        /**
         * Mark the end of this active period for the Span previously returned by activate().
         *
         * <p>
         * NOTE: It is an error to call deactivate() more than once on a single Continuation instance.
         *
         * @see Closeable#close()
         */
        public final void deactivate() {
            doDeactivate();
            decRef();
        }

        /**
         * Per the {@link java.io.Closeable} API.
         */
        @Override
        public final void close() {
            this.deactivate();
        }

        /**
         * Capture and take a reference to the {@link Span} associated with this {@link Continuation} and any 3rd-party
         * execution context of interest.
         *
         * @return a new {@link Continuation} to {@link Continuation#activate()} at the appropriate time.
         */
        public final Continuation capture() {
            refCount.incrementAndGet();
            return holder().doCapture(span(), refCount);
        }

        /**
         * Implementations must clean up any state (including thread-locals, etc) associated with the previosly active
         * {@link Span}.
         */
        protected abstract void doDeactivate();

        /**
         * Return the {@link ActiveSpanHolder} associated wih this {@link Continuation}.
         */
        protected abstract ActiveSpanHolder holder();

        /**
         * Decrement the {@link Continuation}'s reference count, calling {@link Span#finish()} if no more references
         * remain.
         */
        final void decRef() {
            if (0 == refCount.decrementAndGet()) {
                Span span = this.span();
                if (span != null) {
                    this.span().finish();
                }
            }
        }
    }

    /**
     * @return the active {@link Continuation}, or null if none could be found.
     */
    public abstract Continuation active();

    /**
     * @return the currently active Span for this ActiveSpanHolder, or null if there is no such Span
     */
    public final Span activeSpan() {
        Continuation continuation = active();
        if (continuation == null) {
            return null;
        }
        return continuation.span();
    }

    /**
     * Explicitly capture the newly-started Span along with any active state (e.g., MDC state) from the current
     * execution context.
     *
     * @param span the Span just started
     * @return a Continuation that represents the active Span and any other ActiveSpanHolder-specific context, even if the
     *     active Span is null.
     */
    public final Continuation capture(Span span) {
        Continuation rval = doCapture(span, new AtomicInteger(1));
        return rval;
    }

    /**
     * @return the currently active SpanContext, or null if there is no active Span
     */
    public final SpanContext activeContext() {
        Span span = activeSpan();
        if (span == null) {
            return null;
        }
        return span.context();
    }

    protected abstract Continuation doCapture(Span span, AtomicInteger refCount);
}
