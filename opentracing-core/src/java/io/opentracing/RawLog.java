package io.opentracing;

import java.util.Date;

public interface RawLog {
    SomeMicrosecondType getTimestampMicros();
    boolean isError();
    String getMessage();
    Object getPayload();
}
