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

import java.util.Formatter;

/**
 * Span represents an active, un-finished span in the opentracing system.
 *
 * <p>Spans are created by the {@link Tracer} interface and {@link #startChild(String)}.
 */
public interface Span {

  /**
   * Suitable for serializing over the wire, etc.
   */
  SpanContext childContext();

  /**
   * Sets the end timestamp and records the span.
   *
   * <p>This should be the last call made to any span instance, and to do otherwise leads to
   * undefined behavior.
   */
  void finish();

  /**
   * Adds a tag to the span.
   *
   * <p>Tag values can be of arbitrary types, however the treatment of complex types is dependent on
   * the underlying tracing system implementation. It is expected that most tracing systems will
   * handle primitive types like strings and numbers. If a tracing system cannot understand how to
   * handle a particular value type, it may ignore the tag, but shall not panic.
   *
   * <p>If there is a pre-existing tag set for {@code key}, it is overwritten.
   */
  // overloaded 3x to support the BasicType concern
  Span setTag(String key, String value);

  /** Same as {@link #setTag(String, String)}, but for boolean values. */
  Span setTag(String key, boolean value);

  /** Same as {@link #setTag(String, String)}, but for numeric values. */
  // numbers kindof suck.. we've no idea if this is a float, how many bits, etc.
  Span setTag(String key, Number value);

  /**
   * {@code message} is a format string and can refer to fields in the payload by path, like so:
   *
   * <pre>{@code
   *
   * span.info("first transaction is worth ${transactions[0].amount} ${transactions[0].currency}",
   *     ImmutableMap.of(
   *       "transactions", asList(
   *         Transaction.builder().amount(10).currency("USD").build(),
   *         Transaction.builder().amount(11).currency("USD").build(),
   *       )
   *     )
   * );
   * }</pre>
   *
   * @param message {@link Formatter format string} that can refer to fields in the args payload.
   * @param args arbitrary payload
   */
  // See https://github.com/opentracing/opentracing.github.io/issues/30 about parameterization
  Span info(String message, Object... args);

  /** Same as {@link #info}, but for warnings. */
  Span warning(String message, Object... args);

  /** Same as {@link #info}, but for errors. */
  Span error(String message, Object... args);
}
