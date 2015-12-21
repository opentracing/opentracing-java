package io.opentracing;

import java.util.Date;

public interface RawLog {
    /**
     * @return the log timestamp, in microseconds
     */
    SomeMicrosecondType getTimestampMicros();

    /**
     * @return whether the log line represents an error
     */
    boolean isError();

    /**
     * @return the raw message string, pre-substitution.
     *
     * @see Span#info(String, Object...)
     */
    String getMessage();

    /**
     * @return the payload array, or null if there is no payload
     *
     * @see Span#info(String, Object...)
     */
    List<Object> getPayload();
}
