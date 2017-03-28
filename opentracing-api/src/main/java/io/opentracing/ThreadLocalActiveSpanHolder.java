package io.opentracing;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadLocalActiveSpanHolder is a trivial ActiveSpanHolder implementation that relies on Java's thread-local storage primitives.
 *
 * @see ActiveSpanHolder
 * @see Tracer#holder()
 */
public class ThreadLocalActiveSpanHolder extends ActiveSpanHolder {
    private final ThreadLocal<Continuation> threadLocalActive = new ThreadLocal<Continuation>();

    @Override
    public Continuation active() {
        return threadLocalActive.get();
    }

    @Override
    protected ActiveSpanHolder.Continuation doCapture(Span span, AtomicInteger refCount) {
        return new ThreadLocalActiveSpanHolder.Continuation(span, refCount);
    }

    class Continuation extends ActiveSpanHolder.Continuation {
        private final Span span;
        private Continuation toRestore = null;

        private Continuation(Span span, AtomicInteger refCount) {
            super(refCount);
            this.span = span;
        }

        @Override
        public void activate() {
            toRestore = threadLocalActive.get();
            threadLocalActive.set(this);
        }

        @Override
        public Span span() {
            return span;
        }

        @Override
        protected void doDeactivate() {
            if (threadLocalActive.get() != this) {
                // This should not happen; bail out.
                return;
            }
            threadLocalActive.set(toRestore);
        }

        @Override
        protected ActiveSpanHolder holder() {
            return ThreadLocalActiveSpanHolder.this;
        }
    }
}
