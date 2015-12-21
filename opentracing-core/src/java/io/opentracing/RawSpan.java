package io.opentracing;

import java.util.List;

public interface RawSpan {
    /**
     * @return the TraceContext associated with this raw span record
     */
    TraceContext getTraceContext();

    /**
     * @return the name of the span's operation (for use in downstream display, filtering, aggregation, etc)
     */
    String getOperationName();

    /**
     * @return the span's start time in microseconds
     */
    SomeMicrosecondType getStart();

    /**
     * @return the span's duration in microseconds
     */
    SomeMicrosecondType getDuration();

    /**
     * @return a possibly empty list of log records associated with this span
     */
    List<RawLog> getLogs();

    /**
     * XXX: should be ImmutableTags or similar.
     *
     * @return the key:value tags associated with this span, if any
     */
    Tags getTags();
}