/*
 * Copyright 2016-2018 The OpenTracing Authors
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

import java.util.Map;

/**
 * SpanContext represents Span state that must propagate to descendant Spans and across process boundaries.
 *
 * SpanContext is logically divided into two pieces: (1) the user-level "Baggage" that propagates across Span
 * boundaries and (2) any Tracer-implementation-specific fields that are needed to identify or otherwise contextualize
 * the associated Span instance (e.g., a &lt;trace_id, span_id, sampled&gt; tuple).
 *
 * @see Span#setBaggageItem(String, String)
 * @see Span#getBaggageItem(String)
 */
public interface SpanContext {
    /**
     * Return the ID of the trace.
     *
     * Globally unique. Every span in a trace shares this ID.
     *
     * An empty String will be returned if there is no method implementation
     * or there is no trace ID present. null is an invalid return value.
     *
     * @return the trace ID for this context.
     */
    String toTraceId();

    /**
     * Return the ID of the associated Span.
     *
     * Unique within a trace. Each span within a trace contains a different ID.
     *
     * An empty String will be returned if there is no method implementation
     * or there is no span ID present. null is an invalid value return value.
     *
     * @return the Span ID for this context.
     */
    String toSpanId();

    /**
     * @return all zero or more baggage items propagating along with the associated Span
     *
     * @see Span#setBaggageItem(String, String)
     * @see Span#getBaggageItem(String)
     */
    Iterable<Map.Entry<String, String>> baggageItems();
}
