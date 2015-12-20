package io.opentracing;

public interface MarshaledPairKeyValue {
    // XXX: this interface is not right... there needs to be a way to access the set of keys in each map, perhaps via an iterator. Whatever is idiomatic.
    String getTraceContextIdValue(String key);
    String getTraceTagValue(String key);
}
