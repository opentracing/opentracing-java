package io.opentracing;

/**
 * A noop (i.e., cheap-as-possible) implementation of a Scheduler.
 */
public class NoopScheduler implements Scheduler {
    public static final Continuation NOOP_CONTINUATION = new Continuation();

    @Override
    public Span active() {
        return null;
    }

    @Override
    public Continuation capture(Span span) {
        return NOOP_CONTINUATION;
    }

    @Override
    public SpanContext activeContext() {
        return null;
    }

    @Override
    public Continuation captureActive() { return NOOP_CONTINUATION; }

    public static class Continuation implements Scheduler.Continuation {
        @Override
        public Span activate(boolean finishOnDeactivate) { return null; }

        @Override
        public void deactivate() {}

        @Override
        public void close() throws Exception { deactivate(); }
    }
}
