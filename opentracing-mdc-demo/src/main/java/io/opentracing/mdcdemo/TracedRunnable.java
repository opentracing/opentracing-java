package io.opentracing.mdcdemo;

import io.opentracing.ActiveSpan;
import io.opentracing.ActiveSpanSource;

public class TracedRunnable implements Runnable {
    private Runnable runnable;
    private ActiveSpan.Continuation continuation;

    public TracedRunnable(Runnable runnable, ActiveSpanSource spanSource) {
        this(runnable, spanSource.activeSpan());
    }

    public TracedRunnable(Runnable runnable, ActiveSpan handle) {
        if (runnable == null) throw new NullPointerException("Runnable is <null>.");
        this.runnable = runnable;
        this.continuation = handle.defer();
    }

    @Override
    public void run() {
        // NOTE: There's no way to be sure about the finishOnDeactivate parameter to activate(), so we play it safe.
        try (ActiveSpan handle = this.continuation.activate()) {
            runnable.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
