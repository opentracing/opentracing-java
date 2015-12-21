package io.opentracing;

public interface ProcessIdentifier {
    /**
     * @return a human-readable name for this process (for downstream display, filtering, grouping, etc)
     */
    String processName();

    /**
     * @param key the tag key
     * @param val the tag value (XXX: Object is probably not the right way to do this)
     * @return the ProcessIdentifier instance (for chaining)
     */
    ProcessIdentifier setTag(String key, Object val);
}
