package io.opentracing.mdcdemo;

import io.opentracing.ActiveSpan;
import io.opentracing.ActiveSpanSource;

import java.util.concurrent.Callable;

public class TracedCallable<T> implements Callable<T> {
    private ActiveSpan.Continuation continuation;
    private Callable<T> callable;

    public TracedCallable(Callable<T> callable, ActiveSpanSource spanSource) {
        this(callable, spanSource.active());
    }

    public TracedCallable(Callable<T> callable, ActiveSpan handle) {
        if (callable == null) throw new NullPointerException("Callable is <null>.");
        this.callable = callable;
        this.continuation = handle.defer();
    }

    public T call() throws Exception {
        try (ActiveSpan handle = continuation.activate()) {
            return callable.call();
        }
    }
}
