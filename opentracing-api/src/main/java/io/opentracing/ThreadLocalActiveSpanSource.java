package io.opentracing;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadLocalActiveSpanSource is a trivial ActiveSpanSource implementation that relies on Java's thread-local storage primitive.
 *
 * @see ActiveSpanSource
 * @see Tracer#spanSource()
 */
public class ThreadLocalActiveSpanSource implements ActiveSpanSource {
    private final ThreadLocal<ThreadLocalHandle> tlsSnapshot = new ThreadLocal<ThreadLocalHandle>();

    class ThreadLocalHandle implements Handle {
        private final Span span;
        private ThreadLocalHandle toRestore = null;
        private final AtomicInteger refCount;

        ThreadLocalHandle(Span span, AtomicInteger refCount) {
            this.refCount = refCount;
            this.span = span;
            this.toRestore = tlsSnapshot.get();
            tlsSnapshot.set(this);
        }

        @Override
        public Span span() {
            return span;
        }

        @Override
        public void deactivate() {
            if (tlsSnapshot.get() != this) {
                // This shouldn't happen if users call methods in the expected order. Bail out.
                return;
            }
            tlsSnapshot.set(toRestore);

            if (0 == refCount.decrementAndGet()) {
                Span span = this.span();
                if (span != null) {
                    this.span().finish();
                }
            }
        }

        @Override
        public ThreadLocalContinuation defer() {
            refCount.incrementAndGet();
            return ThreadLocalActiveSpanSource.this.makeContinuation(span(), refCount);
        }

        @Override
        public void close() throws IOException {
            deactivate();
        }
    }

    class ThreadLocalContinuation implements Continuation {
        private final Span span;
        private final AtomicInteger refCount;

        ThreadLocalContinuation(Span span, AtomicInteger refCount) {
            this.refCount = refCount;
            this.span = span;
        }

        @Override
        public ThreadLocalHandle activate() {
            return new ThreadLocalHandle(span, refCount);
        }
    }

    ThreadLocalContinuation makeContinuation(Span span, AtomicInteger refCount) {
        return new ThreadLocalContinuation(span, refCount);
    }

    @Override
    public ThreadLocalHandle active() {
        return tlsSnapshot.get();
    }

    @Override
    public Handle adopt(Span span) {
        return makeContinuation(span, new AtomicInteger(1)).activate();
    }

}
