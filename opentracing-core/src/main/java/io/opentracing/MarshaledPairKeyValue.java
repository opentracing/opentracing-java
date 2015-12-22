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
 * MarshaledPairKeyValue represents the TraceContext id as well as any "trace tags" in an
 * implementation-specific key-value format.
 *
 * XXX: this interface is not Java-esque... there needs to be a way to access the set of keys in
 * each map, perhaps via an iterator. Whatever is idiomatic.
 *
 * @see TraceContextMarshaler
 * @see TraceContextUnmarshaler
 */
public interface MarshaledPairKeyValue {
  String getTraceContextIdValue(String key);

  String getTraceTagValue(String key);
}
