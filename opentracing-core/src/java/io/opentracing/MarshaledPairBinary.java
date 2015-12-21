package io.opentracing;

/**
 * MarshaledPairBinary represents the TraceContext id as well as any "trace tags" in an implementation-specific binary format.
 *
 * @see TraceContextMarshaler
 * @see TraceContextUnmarshaler
 */
public interface MarshaledPairBinary {
    byte[] getTraceContextId();
    byte[] getTraceTags();
}