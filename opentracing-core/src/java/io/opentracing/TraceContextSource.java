package io.opentracing;

public interface TraceContextSource extends TraceContextMarshaler, TraceContextUnmarshaler {
    TraceContext newRootTraceContext();
}
