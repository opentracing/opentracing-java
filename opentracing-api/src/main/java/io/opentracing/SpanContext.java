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
package io.opentracing;

/**
 * SpanContext represents Span state that must propagate to descendant Spans and across process boundaries.
 *
 * SpanContext is logically divided into two pieces: (1) the user-level "Baggage" (see set_baggage_item and
 * get_baggage_item) that propagates across Span boundaries and (2) any Tracer-implementation-specific fields that are
 * needed to identify or otherwise contextualize the associated Span instance (e.g., a <trace_id, span_id, sampled>
 * tuple).
 */
public interface SpanContext {
    /**
     * Sets a baggage item in the SpanContext as a key/value pair.
     *
     * Baggage enables powerful distributed context propagation functionality where arbitrary application data can be
     * carried along the full path of request execution throughout the system.
     *
     * Note 1: Baggage is only propagated to the future (recursive) children of this SpanContext.
     *
     * Note 2: Baggage is sent in-band with every subsequent local and remote calls, so this feature must be used with
     * care.
     *
     * @return this SpanContext instance, for chaining
     */
    SpanContext setBaggageItem(String key, String value);

    /**
     * @return the value of the baggage item identified by the given key, or null if no such item could be found
     */
    String getBaggageItem(String key);
}
