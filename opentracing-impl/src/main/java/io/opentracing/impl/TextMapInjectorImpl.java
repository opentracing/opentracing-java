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
import java.util.Map;

import io.opentracing.propagation.Injector;
import io.opentracing.propagation.TextMap;

final class TextMapInjectorImpl implements Injector<TextMap> {

    private final AbstractTracer tracer;
    private boolean baggageEnabled = AbstractTracer.BAGGAGE_ENABLED;

    TextMapInjectorImpl(AbstractTracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void inject(SpanContext spanContext, TextMap carrier) {

        for (Map.Entry<String,Object> entry : tracer.getTraceState(spanContext).entrySet()) {
            carrier.put(entry.getKey(), entry.getValue().toString());
        }
        if (baggageEnabled) {
            for (Map.Entry<String,String> entry : ((AbstractSpan)spanContext).baggageItems()) {
                carrier.put(entry.getKey(), entry.getValue());
            }
        }
    }

    void setBaggageEnabled(boolean baggageEnabled) {
        this.baggageEnabled = baggageEnabled;
    }

}
