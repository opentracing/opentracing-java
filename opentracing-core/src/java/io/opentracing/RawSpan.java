package io.opentracing;

import java.util.List;

public interface RawSpan {
    TraceContext getTraceContext();
    String getOperationName();
    SomeMicrosecondType getStart();
    SomeMicrosecondType getDuration();
    List<RawLog> getLogs();
    // XXX: should be ImmutableTags or similar.
    Tags getTags();
}