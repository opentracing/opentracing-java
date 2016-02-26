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

import java.util.Map;

import io.opentracing.Tracer;

final class TextFormatExtractorImpl implements TextFormatExtractor {

    private final AbstractTracer tracer;

    TextFormatExtractorImpl(AbstractTracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Tracer.SpanBuilder join(Map carrier) {
        AbstractSpanBuilder builder = tracer.createSpanBuilder();
        for (Map.Entry<String, String> entry : ((Map<String, String>) carrier).entrySet()) {
            if (builder.isTraceState(entry.getKey(), entry.getValue())) {
                builder.traceState.put(entry.getKey(), entry.getValue());
            } else if (builder.isBaggage(entry.getKey(), entry.getValue())) {
                builder.baggage.put(entry.getKey(), entry.getValue());
            }
        }
        return builder;
    }

}