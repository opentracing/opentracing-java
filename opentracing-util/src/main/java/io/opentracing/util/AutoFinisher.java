package io.opentracing.util;

import io.opentracing.ActiveSpan;
import io.opentracing.ActiveSpanSource;
import io.opentracing.Span;
import io.opentracing.Finishable;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link AutoFinisher} is an {@link ActiveSpan.Observer} that automatically {@link Span#finish()}es the
 * underlying {@link Span} when there are zero remaining {@link ActiveSpan}s or {@link ActiveSpan.Continuation}s
 * referencing that underlying {@link Span}.
 *
 * <p>
 * Use {@link AutoFinisher} like this:
 * <pre><code>
 *     try (ActiveSpan span = tracer.buildSpan("...").startActive(new AutoFinisher())) {
 *         // (Do work, even if deferred via {@link ActiveSpan#capture()})
 *         span.setTag( ... );  // etc, etc
 *     }  // Span finish()es automatically when there are no longer any ActiveSpans or Continuations referring to it
 * </code></pre>
 *
 * <p>
 * Note that {@link AutoFinisher} works by counting the number of extant {@link ActiveSpan} and
 * {@link ActiveSpan.Continuation} references to the underlying {@link Span} provided at
 * {@link ActiveSpanSource#makeActive(Span, ActiveSpan.Observer)}
 * </p>
 */
public class AutoFinisher implements ActiveSpan.Observer {
    private final AtomicInteger refCount;

    public AutoFinisher() {
        refCount = new AtomicInteger(1);
    }

    @Override
    public void afterCapture(ActiveSpan captured, ActiveSpan.Continuation destination) {
        // Always increment the reference count when new Continuations are created (i.e., we assume that all
        // Continuations are eventually activate()d).
        refCount.incrementAndGet();
    }

    @Override
    public void afterActivate(ActiveSpan.Continuation source, ActiveSpan justActivated) {}

    @Override
    public void afterDeactivate(ActiveSpan activeSpan, Finishable finisher) {
        if (0 == refCount.decrementAndGet()) {
            finisher.finish();
        }
    }
}
