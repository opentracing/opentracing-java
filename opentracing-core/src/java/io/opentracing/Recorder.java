package io.opentracing;

/**
 * A simple interface for RawSpan recording. A requirement for every OpenTracing implementation.
 */
public interface Recorder extends ProcessIdentifier {
    /**
     * recordSpan "records" the fully-assembled/completed RawSpan `span`. Different implementations will "record" in different ways (some may drop the RawSpan entirely).
     *
     * @param span the RawSpan instance to record
     */
    void recordSpan(RawSpan span);
}