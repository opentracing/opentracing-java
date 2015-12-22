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

import java.util.List;

public interface RawSpan {
  /**
   * @return the TraceContext associated with this raw span record
   */
  TraceContext getTraceContext();

  /**
   * @return the name of the span's operation (for use in downstream display, filtering,
   * aggregation, etc)
   */
  String getOperationName();

  /**
   * @return the span's start time in microseconds
   */
  SomeMicrosecondType getStart();

  /**
   * @return the span's duration in microseconds
   */
  SomeMicrosecondType getDuration();

  /**
   * @return a possibly empty list of log records associated with this span
   */
  List<RawLog> getLogs();

  /**
   * XXX: should be ImmutableTags or similar.
   *
   * @return the key:value tags associated with this span, if any
   */
  Tags getTags();
}
