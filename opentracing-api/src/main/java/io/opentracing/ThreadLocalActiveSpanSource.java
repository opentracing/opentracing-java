package io.opentracing;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A trivial source for the {@linkplain #activeSpan activeSpan} {@link ActiveSpan}.
 *
 * @see ThreadLocalActiveSpan
 * @see Tracer#activeSpan()
 */
public class ThreadLocalActiveSpanSource implements ActiveSpanSource {
    final ThreadLocal<ThreadLocalActiveSpan> tlsSnapshot = new ThreadLocal<ThreadLocalActiveSpan>();

    ThreadLocalActiveSpan.Continuation makeContinuation(Span span, AtomicInteger refCount) {
        return new ThreadLocalActiveSpan.Continuation(this, span, refCount);
    }

    @Override
    public ThreadLocalActiveSpan activeSpan() {
        return tlsSnapshot.get();
    }

    @Override
    public ActiveSpan adopt(Span span) {
        return makeContinuation(span, new AtomicInteger(1)).activate();
    }

    // Only for tests
    void clearThreadLocal() {
        tlsSnapshot.remove();
    }

}
