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
package io.opentracing.propagation;

import java.util.HashMap;
import java.util.Map;

import io.opentracing.SpanContext;

public class TestSpanContextImpl implements SpanContext {
    final String marker;
    protected Map<String, String> baggage = new HashMap<String, String>();

    public TestSpanContextImpl(String marker) {
        this.marker = marker;
    }

    public String getMarker() { return marker; }

    @Override
    public synchronized SpanContext setBaggageItem(String key, String value) {
        this.baggage.put(key, value);
        return this;
    }

    @Override
    public synchronized String getBaggageItem(String key) {
        return this.baggage.get(key);
    }
}
