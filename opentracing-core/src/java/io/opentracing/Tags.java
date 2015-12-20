package io.opentracing;

public abstract class Tags {
    // XXX: This setTag/getTag setup is not the right way to do this in Java circa 2015: the below is morally equivalent to the Golang version, nothing more, nothing less.
    public abstract Tags setTag(String key, Object value);
    public abstract Object getTag(String key);
}