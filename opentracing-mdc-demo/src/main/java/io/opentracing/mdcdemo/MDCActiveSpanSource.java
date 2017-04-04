package io.opentracing.mdcdemo;

import io.opentracing.Span;
import io.opentracing.impl.AbstractActiveSpan;
import io.opentracing.impl.AbstractActiveSpanSource;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MDCActiveSpanSource illustrates the core Source concepts and capabilities to a first approximation. Not
 * production-quality code.
 */
public class MDCActiveSpanSource extends AbstractActiveSpanSource {
    final ThreadLocal<MDCActiveSpan> tlsSnapshot = new ThreadLocal<MDCActiveSpan>();

    @Override
    protected MDCActiveSpan.MDCContinuation makeContinuation(Span span, AtomicInteger refCount) {
        if (span instanceof AbstractActiveSpan) {
            throw new IllegalArgumentException("Should only adopt the wrapped Span");
        }
        return new MDCActiveSpan.MDCContinuation(this, span, refCount);
    }

    @Override
    public MDCActiveSpan active() {
        return tlsSnapshot.get();
    }

}
