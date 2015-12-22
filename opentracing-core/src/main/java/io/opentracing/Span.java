/**
 * Copyright 2015 The OpenTracing Authors
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
 * An active, not-yet-completed Span instance. RawSpan represents a completed Span.
 *
 * @see RawSpan
 */
public interface Span {
  /**
   * @return the associated TraceContext
   */
  TraceContext getTraceContext();

  /**
   * Creates and starts a child span.
   *
   * @param operationName the operation name for the child span
   * @return the child span instance
   */
  Span startChild(String operationName);

  /**
   * Sets an arbitrary key:value attribute for this Span instance.
   *
   * @param value XXX probably shouldn't be Object
   * @return the Span instance (for chaining)
   */
  Span setTag(String key, Object value);

  /**
   * `message` is a format string and can refer to fields in the payload by path, like so:
   *
   * "first transaction is worth ${transactions[0].amount} ${transactions[0].currency}"
   *
   * , and the payload might look something like
   *
   * { transactions: [ {amount: 10, currency: "USD"}, {amount: 11, currency: "USD"}, ], }
   */
  void info(String message, Object... payload);

  /**
   * Like info, but for errors :)
   */
  void error(String message, Object... payload);

  /**
   * Complete the span instance.
   *
   * finish() must be the last method call made to the span interface.
   */
  void finish();
}
