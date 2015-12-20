package io.opentracing;

public interface OpenTracer extends TraceContextSource {
    /*
     * Create, start, and return a new Span with the given `operationName`, all without specifying a parent Span that can be used to incorporate the newly-returned Span into an existing trace.
     *
     * @param operationName the operation name for the returned Span
     */
    Span startTrace(String operationName);

    /**
     * Like `StartTrace`, but the return `Span` is made a child of `parent`.
     *
     * @param operationName the operation name for the returned Span
     * @param parent the context for the parent Span
     * @return a new Span given the parameters
     */
    Span joinTrace(String operationName, TraceContext parent);
}
