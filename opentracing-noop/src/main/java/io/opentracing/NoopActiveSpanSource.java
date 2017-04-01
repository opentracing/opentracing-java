package io.opentracing;

import java.io.IOException;

/**
 * A noop (i.e., cheap-as-possible) implementation of a ActiveSpanSource.
 */
public class NoopActiveSpanSource implements ActiveSpanSource {
    public static final Handle NOOP_ACTIVE_SPAN = new NoopHandle();
    public static final Continuation NOOP_CONTINUATION = new NoopContinuation();

    @Override
    public Handle adopt(Span span) {
        return NOOP_ACTIVE_SPAN;
    }

    @Override
    public Handle active() { return NOOP_ACTIVE_SPAN; }

    public static class NoopHandle implements Handle {
        @Override
        public Span span() {
            return null;
        }

        @Override
        public void deactivate() {}

        @Override
        public ActiveSpanSource.Continuation defer() {
            return NOOP_CONTINUATION;
        }

        @Override
        public void close() throws IOException {}
    }
    public static class NoopContinuation implements ActiveSpanSource.Continuation {
        @Override
        public Handle activate() {
            return NOOP_ACTIVE_SPAN;
        }
    }
}
