package io.opentracing.mdcdemo;

import io.opentracing.ActiveSpanSource;

import java.util.concurrent.Callable;

public class TracedCallable<T> implements Callable<T> {
    private ActiveSpanSource.Continuation continuation;
    private Callable<T> callable;

    public TracedCallable(Callable<T> callable, ActiveSpanSource activeSpanSource) {
        this(callable, activeSpanSource.active());
    }

    public TracedCallable(Callable<T> callable, ActiveSpanSource.Handle handle) {
        if (callable == null) throw new NullPointerException("Callable is <null>.");
        this.callable = callable;
        this.continuation = handle.defer();
    }

    public T call() throws Exception {
        try (ActiveSpanSource.Handle handle = continuation.activate()) {
            return callable.call();
        }
    }
}
