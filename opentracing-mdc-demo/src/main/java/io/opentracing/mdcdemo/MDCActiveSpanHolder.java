package io.opentracing.mdcdemo;

import io.opentracing.ActiveSpanHolder;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MDCActiveSpanHolder illustrates the core ActiveSpanHolder concepts and capabilities to a first approximation. Not
 * production-quality code.
 */
public class MDCActiveSpanHolder extends ActiveSpanHolder {
    private final ThreadLocal<MDCContinuation> tlsSnapshot = new ThreadLocal<MDCContinuation>();

    class MDCContinuation extends Continuation {
        private final Map<String, String> mdcContext;
        private final Span span;
        private MDCContinuation toRestore = null;

        MDCContinuation(Span span, AtomicInteger refCount) {
            super(refCount);
            this.mdcContext = MDC.getCopyOfContextMap();
            this.span = span;
        }

        @Override
        public void activate() {
            toRestore = tlsSnapshot.get();
            tlsSnapshot.set(this);
            MDC.setContextMap(mdcContext);
        }

        @Override
        public Span span() {
            return span;
        }

        @Override
        protected void doDeactivate() {
            if (tlsSnapshot.get() != this) {
                // This shouldn't happen if users call methods in the expected order. Bail out.
                return;
            }
            tlsSnapshot.set(toRestore);
        }

        @Override
        protected ActiveSpanHolder holder() {
            return MDCActiveSpanHolder.this;
        }

    }

    @Override
    protected Continuation doCapture(Span span, AtomicInteger refCount) {
        return new MDCContinuation(span, refCount);
    }

    @Override
    public Continuation active() {
        return tlsSnapshot.get();
    }

}
