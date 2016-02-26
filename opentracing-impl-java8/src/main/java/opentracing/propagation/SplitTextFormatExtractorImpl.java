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
package opentracing.propagation;

import java.util.HashMap;
import java.util.Map;

import io.opentracing.Span;

final class SplitTextFormatExtractorImpl implements SplitTextFormatExtractor {

    @Override
    public Span joinTrace(String operationName, Map carrier) {
        Map<String, String> traceState = new HashMap<>();
        for (Map.Entry<String, String> entry : ((Map<String, String>) carrier).entrySet()) {
            if (entry.getKey().startsWith(SplitTextFormatInjector.TRACE_IDENTIFIERS)) {
                traceState.put(entry.getKey().substring(SplitTextFormatInjector.TRACE_IDENTIFIERS.length()), entry.getValue());
            }
        }
        Span span = new SpanImpl(operationName, traceState);
        for (Map.Entry<String, String> entry : ((Map<String, String>) carrier).entrySet()) {
            if (entry.getKey().startsWith(SplitTextFormatInjector.TRACE_ATTRIBUTES)) {
                traceState.put(entry.getKey().substring(SplitTextFormatInjector.TRACE_ATTRIBUTES.length()), entry.getValue());
            }
        }
        return span;
    }

}
