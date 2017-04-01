package io.opentracing.mdcdemo;

import io.opentracing.ActiveSpanSource;
import io.opentracing.Span;
import io.opentracing.impl.AbstractActiveSpanSource;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MDCActiveSpanSource illustrates the core ActiveSpanSource concepts and capabilities to a first approximation. Not
 * production-quality code.
 */
public class MDCActiveSpanSource extends AbstractActiveSpanSource {
    private final ThreadLocal<MDCHandle> tlsSnapshot = new ThreadLocal<MDCHandle>();

    class MDCHandle extends AbstractHandle {
        private final Span span;
        private MDCHandle toRestore = null;

        MDCHandle(Span span, Map<String, String> mdcContext, AtomicInteger refCount) {
            super(refCount);
            this.span = span;
            this.toRestore = tlsSnapshot.get();
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
        protected ActiveSpanSource spanSource() {
            return MDCActiveSpanSource.this;
        }

    }
    class MDCContinuation extends AbstractContinuation {
        private final Map<String, String> mdcContext;
        private final Span span;

        MDCContinuation(Span span, AtomicInteger refCount) {
            super(refCount);
            this.mdcContext = MDC.getCopyOfContextMap();
            this.span = span;
        }

        @Override
        public MDCHandle activate() {
            return new MDCHandle(span, mdcContext, refCount);
        }
    }

    @Override
    protected MDCContinuation makeContinuation(Span span, AtomicInteger refCount) {
        return new MDCContinuation(span, refCount);
    }

    @Override
    public MDCHandle active() {
        return tlsSnapshot.get();
    }

}
