package io.opentracing.util;

import io.opentracing.ActiveSpan;
import io.opentracing.Span;
import io.opentracing.SpanContext;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link AutoFinisher} is an {@link ActiveSpan} wrapper that automatically {@link Span#finish()}es the underlying
 * {@link Span} when there are zero remaining {@link ActiveSpan}s or {@link ActiveSpan.Continuation}s referencing
 * that underlying {@link Span}.
 *
 * <p>
 * Use {@link AutoFinisher} like this:
 * <pre><code>
 *     try (ActiveSpan span = new AutoFinisher(tracer.buildSpan("...").startActive())) {
 *         // (Do work, even if deferred via {@link ActiveSpan#capture()})
 *         span.setTag( ... );  // etc, etc
 *     }  // Span finish()es automatically when there are no longer any ActiveSpans or Continuations referring to it
 * </code></pre>
 *
 * <p>
 * Note that {@link AutoFinisher} works by counting the number of extant {@link ActiveSpan} or
 * {@link ActiveSpan.Continuation} references to the underlying {@link ActiveSpan} provided at construction time.
 * </p>
 */
public class AutoFinisher implements ActiveSpan {
    private final AtomicInteger refCount;
    private final ActiveSpan delegated;

    public AutoFinisher(ActiveSpan delegated) {
        this.delegated = delegated;
        this.refCount = new AtomicInteger(1);
    }

    AutoFinisher(ActiveSpan delegated, AtomicInteger refCount) {
        this.refCount = refCount;
        this.delegated = delegated;
    }

    @Override
    public SpanContext context() {
        return delegated.context();
    }

    @Override
    public ActiveSpan setTag(String key, String value) {
        delegated.setTag(key, value);
        return this;
    }

    @Override
    public ActiveSpan setTag(String key, boolean value) {
        delegated.setTag(key, value);
        return this;
    }

    @Override
    public ActiveSpan setTag(String key, Number value) {
        delegated.setTag(key, value);
        return this;
    }

    @Override
    public ActiveSpan log(Map<String, ?> fields) {
        delegated.log(fields);
        return this;
    }

    @Override
    public ActiveSpan log(long timestampMicroseconds, Map<String, ?> fields) {
        delegated.log(timestampMicroseconds, fields);
        return this;
    }

    @Override
    public ActiveSpan log(String event) {
        delegated.log(event);
        return this;
    }

    @Override
    public ActiveSpan log(long timestampMicroseconds, String event) {
        delegated.log(timestampMicroseconds, event);
        return this;
    }

    @Override
    public ActiveSpan setBaggageItem(String key, String value) {
        delegated.setBaggageItem(key, value);
        return this;
    }

    @Override
    public String getBaggageItem(String key) {
        return delegated.getBaggageItem(key);
    }

    @Override
    public ActiveSpan setOperationName(String operationName) {
        delegated.setOperationName(operationName);
        return this;
    }

    @Override
    public Span wrapped() {
        return this.delegated.wrapped();
    }

    @Override
    public void deactivate() {
        this.delegated.deactivate();
        if (0 == refCount.decrementAndGet()) {
            this.delegated.wrapped().finish();
        }
    }

    @Override
    public void close() {
        this.deactivate();
    }

    @Override
    public Continuation capture() {
        return new AutoFinisher.Continuation();
    }

    class Continuation implements ActiveSpan.Continuation {
        Continuation() {
            // Always increment the reference count when new Continuations are created (i.e., we assume that all
            // Continuations are eventually activate()d).
            refCount.incrementAndGet();
        }

        @Override
        public ActiveSpan activate() {
            return new AutoFinisher(delegated, refCount);
        }
    }
}
