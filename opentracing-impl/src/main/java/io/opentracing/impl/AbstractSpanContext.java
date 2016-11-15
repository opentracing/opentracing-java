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
package io.opentracing.impl;

import io.opentracing.SpanContext;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

public class AbstractSpanContext implements SpanContext {
    private final AbstractTracer tracer;
    protected final Map<String, String> baggage;
    protected final Map<String, Object> traceState;

    AbstractSpanContext(Map<String, Object> traceState, AbstractTracer tracer) {
        this(traceState, emptyMap(), tracer);
    }
    
    AbstractSpanContext(Map<String, Object> traceState, Map<String, String> baggage, AbstractTracer tracer) {
        this.traceState = unmodifiableMap(traceState);
        this.baggage = unmodifiableMap(new HashMap<>(baggage)); 
        this.tracer = tracer;
    }

    @Override
    public final Iterable<Map.Entry<String, String>> baggageItems() {
        return baggage.entrySet();
    }

    SpanContext withBaggage(Map<String, String> baggage) {
        return new AbstractSpanContext(traceState, baggage, tracer);
    }

    public AbstractSpanContext setBaggageItem(String key, String value) {
        Map<String, String> newBaggage = new HashMap<>(baggage);
        newBaggage.put(key, value);
        return new AbstractSpanContext(traceState, newBaggage, tracer);
    }

    public String getBaggageItem(String key) {
        return baggage.get(key);
    }
}
