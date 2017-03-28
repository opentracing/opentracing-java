package io.opentracing.mdcdemo;

import io.opentracing.ActiveSpanHolder;
import io.opentracing.Span;


public class TracedRunnable implements Runnable {
    private Runnable runnable;
    private ActiveSpanHolder.Continuation continuation;

    public TracedRunnable(Runnable runnable, ActiveSpanHolder holder) {
        this(runnable, holder.active());
    }

    public TracedRunnable(Runnable runnable, ActiveSpanHolder.Continuation continuation) {
        if (runnable == null) throw new NullPointerException("Runnable is <null>.");
        this.runnable = runnable;
        this.continuation = continuation.capture();
    }

    @Override
    public void run() {
        // NOTE: There's no way to be sure about the finishOnDeactivate parameter to activate(), so we play it safe.
        this.continuation.activate();
        final Span span = this.continuation.span();
        try {
            runnable.run();
        } finally {
            this.continuation.deactivate();
        }
    }
}
