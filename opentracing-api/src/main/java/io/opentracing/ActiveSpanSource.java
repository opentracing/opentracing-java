package io.opentracing;

/**
 * {@link ActiveSpanSource} allows an existing (possibly thread-local-aware) execution context provider to act as a
 * source for an actively-scheduled OpenTracing Span.
 *
 * @see ActiveSpan
 */
public interface ActiveSpanSource {

    /**
     * @return the activeSpan {@link ActiveSpan}, or null if none could be found. This does not affect the reference count for
     * the {@link ActiveSpan}.
     */
    ActiveSpan activeSpan();

    /**
     * Wrap and "adopt" a @{link Span} by encapsulating it – and any activeSpan state (e.g., MDC state) in the execution
     * context – in a new @{link Handle}.
     *
     * @param span the Span just started
     * @return a @{link Handle} that encapsulates the given Span and any other Source-specific context (e.g.,
     * MDC data)
     */
    ActiveSpan adopt(Span span);
}
