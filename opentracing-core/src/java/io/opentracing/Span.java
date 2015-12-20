package io.opentracing;

public interface Span {
    TraceContext getTraceContext();
    Span startChild(String operationName);

    Span setTag(String key, Object value);

    void info(String message, Object... payload);
    void error(String message, Object... payload);

    void finish();
}
