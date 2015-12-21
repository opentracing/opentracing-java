package io.opentracing;

/**
 * An active, not-yet-completed Span instance. RawSpan represents a completed Span.
 *
 * @see RawSpan
 */
public interface Span {
    /**
     * @return the associated TraceContext
     */
    TraceContext getTraceContext();

    /**
     * Creates and starts a child span.
     *
     * @param operationName the operation name for the child span
     * @return the child span instance
     */
    Span startChild(String operationName);

    /**
     * Sets an arbitrary key:value attribute for this Span instance.
     *
     * @param key
     * @param value XXX probably shouldn't be Object
     * @return the Span instance (for chaining)
     */
    Span setTag(String key, Object value);

    /**
     * `message` is a format string and can refer to fields in the payload by path, like so:
     *
     *   "first transaction is worth ${transactions[0].amount} ${transactions[0].currency}"
     *
     * , and the payload might look something like
     *
     *   {
     *       transactions: [
     *           {amount: 10, currency: "USD"},
     *           {amount: 11, currency: "USD"},
     *       ],
     *   }
     *
     * @param message
     * @param payload
     */
    void info(String message, Object... payload);

    /**
     * Like info, but for errors :)
     */
    void error(String message, Object... payload);

    /**
     * Complete the span instance.
     *
     * finish() must be the last method call made to the span interface.
     */
    void finish();
}
