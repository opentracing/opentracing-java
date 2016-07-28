/**
 * Copyright 2016 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.opentracing;

/**
 * Represents an in-flight span in the opentracing system.
 *
 * <p>Spans are created by the {@link Tracer#buildSpan} interface.
 */
public interface Span extends AutoCloseable {
    /**
     * Retrieve the associated SpanContext.
     *
     * @return the SpanContext that encapsulates Span state that should propagate across process boundaries.
     */
    SpanContext context();

    /**
     * Sets the end timestamp and records the span.
     *
     * <p>This should be the last call made to any span instance, and to do otherwise leads to
     * undefined behavior.
     */
    void finish();

    void close();

    /**
     * Set a key:value tag on the Span.
     */
    // overloaded 3x to support the BasicType concern
    Span setTag(String key, String value);

    /** Same as {@link #setTag(String, String)}, but for boolean values. */
    Span setTag(String key, boolean value);

    /** Same as {@link #setTag(String, String)}, but for numeric values. */
    Span setTag(String key, Number value);

    /**
     * Add a new log event to the Span, accepting an event name string and an optional structured payload argument.
     *
     * If specified, the payload argument may be of any type and arbitrary size, though implementations are not
     * required to retain all payload arguments (or even all parts of all payload arguments).
     *
     * The timestamp of this log event is the current time.
     **/
    Span log(String eventName, /* @Nullable */ Object payload);

    /**
     * Add a new log event to the Span, accepting an event name string and an optional structured payload argument.
     *
     * If specified, the payload argument may be of any type and arbitrary size, though implementations are not
     * required to retain all payload arguments (or even all parts of all payload arguments).
     *
     * The timestamp is specified manually here to represent a past log event.
     * The timestamp in microseconds in UTC time.
     **/
    Span log(long timestampMicroseconds, String eventName, /* @Nullable */ Object payload);
}
