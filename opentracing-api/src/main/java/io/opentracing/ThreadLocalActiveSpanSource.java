package io.opentracing;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A trivial source for the {@linkplain #active active} {@link ActiveSpan}.
 *
 * @see ThreadLocalActiveSpan
 * @see Tracer#spanSource()
 */
public class ThreadLocalActiveSpanSource implements ActiveSpanSource {
    final ThreadLocal<ThreadLocalActiveSpan> tlsSnapshot = new ThreadLocal<ThreadLocalActiveSpan>();

    ThreadLocalActiveSpan.Continuation makeContinuation(Span span, AtomicInteger refCount) {
        return new ThreadLocalActiveSpan.Continuation(this, span, refCount);
    }

    @Override
    public ThreadLocalActiveSpan active() {
        return tlsSnapshot.get();
    }

    @Override
    public ActiveSpan adopt(Span span) {
        return makeContinuation(span, new AtomicInteger(1)).activate();
    }

}
