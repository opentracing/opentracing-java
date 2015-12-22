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

public interface ProcessIdentifier {
  /**
   * @return a human-readable name for this process (for downstream display, filtering, grouping,
   * etc)
   */
  String processName();

  /**
   * @param key the tag key
   * @param val the tag value (XXX: Object is probably not the right way to do this)
   * @return the ProcessIdentifier instance (for chaining)
   */
  ProcessIdentifier setTag(String key, Object val);
}
