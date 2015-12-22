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

public interface OpenTracer extends TraceContextSource {
  /*
   * Create, start, and return a new Span with the given `operationName`, all without specifying a parent Span that can be used to incorporate the newly-returned Span into an existing trace.
   *
   * @param operationName the operation name for the returned Span
   */
  Span startTrace(String operationName);

  /**
   * Like `StartTrace`, but the return `Span` is made a child of `parent`.
   *
   * @param operationName the operation name for the returned Span
   * @param parent the context for the parent Span
   * @return a new Span given the parameters
   */
  Span joinTrace(String operationName, TraceContext parent);
}
