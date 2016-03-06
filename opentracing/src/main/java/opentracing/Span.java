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
package opentracing;

/**
 * Represents an in-flight span in the opentracing system.
 *
 * <p>Spans are created by the {@link Tracer#buildSpan} interface.
 */
public interface Span {

  /**
   * Sets the end timestamp and records the span.
   *
   * <p>This should be the last call made to any span instance, and to do otherwise leads to
   * undefined behavior.
   */
  void finish();

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
   * Set a Baggage item, represented as a simple string:string pair.
   *
   * Note that newly-set Baggage items are only guaranteed to propagate to future children of the given Span.
   */
  Span setBaggageItem(String key, String value);

  /** Get a Baggage item by key.
   *
   * Returns null if no entry found, or baggage is not supported in the current implementation.
   */
  String getBaggageItem(String key);

  /**
   * Create an event builder.
   *
   * The timestamp of this log event is by default the time the EventBuilder is constructed.
   **/
  EventBuilder buildEvent();

  interface EventBuilder {

    /**
     * The event name should be the stable identifier for some notable moment in the lifetime of a Span.
     * For instance,
     *      a Span representing a browser page load might add an event for each of the Performance.timing moments.
     *
     * While it is not a formal requirement,
     *  specific event names should apply to many Span instances:
     *      tracing systems can use these event names (and timestamps) to analyze Spans in the aggregate.
     */
    EventBuilder withName(String name);

    /**
      * Add the optional structured payload argument.
      * If specified, the payload argument may be of any type and arbitrary size,
      * though implementations are not required to retain all payload arguments
      * (or even all parts of all payload arguments).
      **/
    EventBuilder withPayload(Object payload);

    /**
     * Add a specific timestamp, in microseconds since epoch.
     **/
     EventBuilder withTimestamp(long microseconds);

     /** Build the log event, adding it to the Span, then return the Span. */
     Span log();
  }
}
