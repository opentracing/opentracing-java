package io.opentracing;

public interface TraceContextUnmarshaler {
    TraceContext unmarshalTraceContextBinary(MarshaledPairBinary m);
    TraceContext unmarshalTraceContextKeyValue(MarshaledPairKeyValue m);
}
