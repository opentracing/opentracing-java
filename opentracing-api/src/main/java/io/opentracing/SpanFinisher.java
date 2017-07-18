package io.opentracing;

/**
 * {@link SpanFinisher} factors out the overloaded {@link #finish()} method(s).
 *
 * @see ActiveSpan.Observer#onDeactivate(ActiveSpan, SpanFinisher)
 * @see Span
 */
public interface SpanFinisher {
    /**
     * Sets the end timestamp to now and records the span.
     *
     * <p>With the exception of calls to {@link BaseSpan#context}, this should be the last call made to the span
     * instance. Future calls to {@link #finish} are defined as noops, and future calls to methods other than
     * {@link BaseSpan#context} lead to undefined behavior.
     *
     * @see BaseSpan#context()
     */
    void finish();

    /**
     * Sets an explicit end timestamp and records the span.
     *
     * <p>With the exception of calls to Span.context(), this should be the last call made to the span instance, and to
     * do otherwise leads to undefined behavior.
     *
     * @param finishMicros an explicit finish time, in microseconds since the epoch
     *
     * @see BaseSpan#context()
     */
    void finish(long finishMicros);
}
