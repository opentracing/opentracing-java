package io.opentracing;

/**
 * @see TraceContextMarshaler
 */
public interface TraceContextUnmarshaler {
    TraceContext unmarshalTraceContextBinary(MarshaledPairBinary m);
    TraceContext unmarshalTraceContextKeyValue(MarshaledPairKeyValue m);
}
