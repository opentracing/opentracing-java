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

import java.util.Iterator;
import java.util.Map;

import io.opentracing.propagation.TextMapReader;
import io.opentracing.SpanContext;

public class TestTextMapExtractorImpl implements Extractor<TextMapReader> {
    public SpanContext extract(TextMapReader carrier) {
        String marker = null;
        for (Iterator<Map.Entry<String,String>> iter = carrier.getEntries(); iter.hasNext();) {
            Map.Entry<String, String> entry = iter.next();
            if (entry.getKey().equals("test-marker")) {
                marker = entry.getValue();
            }
        }
        if (marker == null) {
            return null;
        }
        return new TestSpanContextImpl(marker);
    }
}
