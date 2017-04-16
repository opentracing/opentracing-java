package io.opentracing;

/**
 * ThreadLocalScheduler is a trivial Scheduler implementation that relies on Java's thread-local storage primitives.
 *
 * @see Scheduler
 * @see Tracer#scheduler()
 */
public class ThreadLocalScheduler implements Scheduler {
    private final ThreadLocal<Continuation> threadLocalActive = new ThreadLocal<Continuation>();

    @Override
    public Span active() {
        Continuation state = threadLocalActive.get();
        return (state == null) ? null : state.span;
    }

    @Override
    public Continuation capture(Span span) {
        return new Continuation(span);
    }

    @Override
    public SpanContext activeContext() {
        Span active = this.active();
        if (active == null) return null;
        return active.context();
    }

    @Override
    public Continuation captureActive() {
        return capture(active());
    }

    class Continuation implements Scheduler.Continuation {
        private final Span span;
        private boolean finishOnDeactivate;
        private Continuation toRestore = null;

        private Continuation(Span span) { this.span = span; }

        @Override
        public Span activate(boolean finishOnDeactivate) {
            this.finishOnDeactivate = finishOnDeactivate;
            toRestore = threadLocalActive.get();
            threadLocalActive.set(this);
            return span;
        }

        @Override
        public void close() {
            this.deactivate();
        }

        @Override
        public void deactivate() {
            if (span != null && this.finishOnDeactivate) {
                span.finish();
            }

            if (threadLocalActive.get() != this) {
                // This should not happen; bail out.
                return;
            }
            threadLocalActive.set(toRestore);
        }
    }
}
