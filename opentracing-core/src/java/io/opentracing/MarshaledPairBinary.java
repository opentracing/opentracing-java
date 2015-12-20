package io.opentracing;

public interface MarshaledPairBinary {
    byte[] getTraceContextId();
    byte[] getTraceTags();
}