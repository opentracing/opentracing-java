package io.opentracing;

public interface ProcessIdentifier {
    String processName();
    ProcessIdentifier setTag(String key, Object val);
}
