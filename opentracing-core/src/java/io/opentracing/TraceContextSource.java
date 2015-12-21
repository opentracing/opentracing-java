package io.opentracing;

/**
 * Essentially a factory for root TraceContext instances which also knows how to marshal/unmarshal any (root or non-root) TraceContext instance.
 */
public interface TraceContextSource extends TraceContextMarshaler, TraceContextUnmarshaler {
    TraceContext newRootTraceContext();
}
