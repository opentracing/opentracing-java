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
 * Tracer is a simple, thin interface for Span creation, and Span propagation into different transport formats.
 */
public interface Tracer {

  /**
   * Create, start, and return a new Span with the given `operationName`.
   * An optional parent Span can be specified used to incorporate the newly-returned Span into an existing trace.
   *
   * <p>Example:
   * <pre>{@code
   * Tracer tracer = ...
   * Span feed = tracer.startTrace("GetFeed", null);
   * Span http = tracer.startTrace("HandleHTTPRequest", feed)
   *                   .setTag("user_agent", req.UserAgent)
   *                   .setTag("lucky_number", 42);
   * }</pre>
   */
  Span startSpan(String operationName, /* @Nullable */ Span parent);

  /**
   * Same as {@link #startSpan(String, Span)},
   * but allows to specify a past timestamp in microseconds when the Span was created.
   */
  Span startSpan(String operationName, long microseconds, /* @Nullable */ Span parent);

  /** Takes two arguments:
   *    a Span instance, and
   *    a “carrier” object in which to inject that Span for cross-process propagation.
   *
   */
  <T> void inject(Span span, T carrier);

  /**  Takes two arguments:
   *    the operation name for the Span it’s about to create, and
   *    a “carrier” object from which to extract identifying information needed by the new Span instance.
   *
   * Unless there’s an error, it returns a freshly-started Span which can be used in the host process like any other.
   * (Note that some OpenTracing implementations consider the Spans on either side of an RPC to have the same identity,
   * and others consider the caller to be the parent and the receiver to be the child)
   */
  <T> Span join(String operationName, T carrier);
}
