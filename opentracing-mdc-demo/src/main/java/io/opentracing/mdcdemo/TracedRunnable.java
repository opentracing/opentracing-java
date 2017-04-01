package io.opentracing.mdcdemo;

import io.opentracing.ActiveSpanSource;

import java.io.IOException;


public class TracedRunnable implements Runnable {
    private Runnable runnable;
    private ActiveSpanSource.Continuation continuation;

    public TracedRunnable(Runnable runnable, ActiveSpanSource spanSource) {
        this(runnable, spanSource.active());
    }

    public TracedRunnable(Runnable runnable, ActiveSpanSource.Handle handle) {
        if (runnable == null) throw new NullPointerException("Runnable is <null>.");
        this.runnable = runnable;
        this.continuation = handle.defer();
    }

    @Override
    public void run() {
        // NOTE: There's no way to be sure about the finishOnDeactivate parameter to activate(), so we play it safe.
        try (ActiveSpanSource.Handle handle = this.continuation.activate()) {
            runnable.run();
        } catch (IOException e) {
            // Do nothing?
        }
    }
}
