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
 * A simple interface for RawSpan recording. A requirement for every OpenTracing implementation.
 */
public interface Recorder extends ProcessIdentifier {
  /**
   * recordSpan "records" the fully-assembled/completed RawSpan `span`. Different implementations
   * will "record" in different ways (some may drop the RawSpan entirely).
   *
   * @param span the RawSpan instance to record
   */
  void recordSpan(RawSpan span);
}
