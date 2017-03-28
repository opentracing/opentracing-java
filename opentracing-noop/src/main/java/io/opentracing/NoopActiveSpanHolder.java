package io.opentracing;

/**
 * A noop (i.e., cheap-as-possible) implementation of a ActiveSpanHolder.
 */
public class NoopActiveSpanHolder implements ActiveSpanHolder {
    public static final Continuation NOOP_CONTINUATION = new Continuation();

    @Override
    public Continuation active() { return NOOP_CONTINUATION; }

    @Override
    public Span activeSpan() { return null; }

    @Override
    public Continuation capture(Span span) {
        return NOOP_CONTINUATION;
    }

    @Override
    public SpanContext activeContext() {
        return null;
    }

    public static class Continuation implements ActiveSpanHolder.Continuation {
        @Override
        public void activate() {}

        @Override
        public Span span() { return null; }

        @Override
        public ActiveSpanHolder.Continuation capture() { return NOOP_CONTINUATION; }

        @Override
        public void deactivate() {}

        @Override
        public void close() { deactivate(); }
    }
}
