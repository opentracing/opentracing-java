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
   * @see Span#info(String, Object...)
   */
  String getMessage();

  /**
   * @return the payload array, or null if there is no payload
   * @see Span#info(String, Object...)
   */
  List<Object> getPayload();
}
