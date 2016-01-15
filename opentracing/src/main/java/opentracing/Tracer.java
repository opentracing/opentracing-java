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
 * Tracer is a simple, thin interface for Span creation.
 */
public interface Tracer {

  /**
   * Create, start, and return a new Span with the given `operationName`, all without specifying a
   * parent Span that can be used to incorporate the newly-returned Span into an existing trace.
   *
   * <p>Example:
   * <pre>{@code
   * Tracer tracer = ...
   * Span feed = tracer.startTrace("GetFeed");
   * Span http = tracer.startTrace("HandleHTTPRequest")
   *                   .setTag("user_agent", req.UserAgent)
   *                   .setTag("lucky_number", 42);
   * }</pre>
   */
  Span startTrace(String operationName);

  /**
   * Like {@link #startTrace(String)}, but the returned span is made a child of {@code parent}.
   */
  Span joinTrace(String operationName, TraceContext parent);

  /**
   * StartSpanWithContext returns a span with the given {@code operationName} and an association
   * with {@code context} (rather than creating a fresh root context like {@link
   * #startTrace(String)} or a fresh child context like {@link #joinTrace(String, TraceContext)}).
   *
   * <p>Note that the following calls are equivalent
   * <pre>{@code
   * Span feed = tracer.startSpanWithContext("GetFeed", traceContextSource.newRoot());
   * ...
   * Span feed = tracer.startTrace("GetFeed");
   * }</pre>
   */
  Span startSpanWithContext(String operationName, TraceContext context);
}
