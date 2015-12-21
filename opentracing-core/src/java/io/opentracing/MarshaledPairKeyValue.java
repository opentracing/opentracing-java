package io.opentracing;

/**
 * MarshaledPairKeyValue represents the TraceContext id as well as any "trace tags" in an implementation-specific key-value format.
 *
 * XXX: this interface is not Java-esque... there needs to be a way to access the set of keys in each map, perhaps via an iterator. Whatever is idiomatic.
 *
 * @see TraceContextMarshaler
 * @see TraceContextUnmarshaler
 */
public interface MarshaledPairKeyValue {
    String getTraceContextIdValue(String key);
    String getTraceTagValue(String key);
}
