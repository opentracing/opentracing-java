package io.opentracing;

/**
 * LogField represents a single key:value pair in a Span.log() record.
 *
 * The key must always be a String. All Tracer implementations must support values bool, numeric, and String values;
 * some may also support arbitrary Object values.
 */
public class LogField<V> {
    private final String key;
    private final V value;

    public LogField(String key, V value) {
        this.key = key;
        this.value = value;
    }

    public String key() { return key; }
    public V value() { return value; }
}
