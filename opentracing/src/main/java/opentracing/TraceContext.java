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

import java.util.Map;

/**
 * Long-lived interface that knows how to create a root {@link SpanContext} and encode/decode
 * any other.
 */
public interface TraceContext {

  /**
   * Encodes or Decodes a {@link SpanContext trace context} in binary or text formats.
   */
  TraceContextCodec codec();

  /**
   * Create a SpanContext which has no parent (and thus begins its own trace).
   *
   * <p>A TraceContextSource must always return the same type in successive calls to
   * NewRootTraceContext().
   */
  SpanContext newRoot();

  /**
   * Creates a child context for {@code parent}, and returns both that child's own
   * TraceContext as well as any Tags that should be added to the child's Span.
   */
  ChildTraceContext newChild(SpanContext parent);

  interface ChildTraceContext {
    SpanContext child();

    Map<String, Object> tags();
  }
}
