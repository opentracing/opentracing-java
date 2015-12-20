package io.opentracing;

public interface Recorder extends ProcessIdentifier {
    void recordSpan(RawSpan span);
}