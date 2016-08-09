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

import java.util.HashMap;
import java.util.Map;

public class TestSpanContextImpl implements SpanContext {
    final String marker;
    protected final Map<String, String> baggage;

    public TestSpanContextImpl(String marker) {
        this(marker, new HashMap<>());
    }

    public TestSpanContextImpl(String marker, Map<String, String> adoptedBaggage) {
        this.marker = marker;
        this.baggage = adoptedBaggage;
    }

    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
        return baggage.entrySet();
    }

    ///////////////////////////////////////////////////////////////////////////////////
    // Implementation-specific extensions (mainly to support the immutable idiom here).
    ///////////////////////////////////////////////////////////////////////////////////

    public TestSpanContextImpl withBaggageItem(String key, String val) {
        Map<String, String> baggageCopy = new HashMap<>(baggage);
        baggageCopy.put(key, val);
        return new TestSpanContextImpl(marker, baggageCopy);
    }

    public String getBaggageItem(String key) {
        return baggage.get(key);
    }

    public String getMarker() { return marker; }
}
