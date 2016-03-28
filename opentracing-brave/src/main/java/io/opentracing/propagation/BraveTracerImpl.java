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

import com.github.kristofa.brave.Brave;
import io.opentracing.Span;
import java.util.HashMap;
import java.util.Map;

public final class BraveTracerImpl extends AbstractTracer {

    private final Brave brave = new Brave.Builder().build();

    @Override
    BraveSpanBuilderImpl createSpanBuilder() {
        return new BraveSpanBuilderImpl(brave);
    }

    @Override
    Map<String, String> getTraceState(Span span) {
        return new HashMap<String,String>() {{
                put("traceId", String.valueOf(((BraveSpanImpl)span).spanId.getTraceId()));
                put("spanId", String.valueOf(((BraveSpanImpl)span).spanId.getSpanId()));
            }};
    }

    @Override
    Map<String, String> getBaggage(Span span) {
        return ((BraveSpanImpl)span).baggage;
    }

    @Override
    public <T> void inject(Span span, T carrier) {
        brave.clientTracer().setClientSent();
        super.inject(span, carrier);
        ((BraveSpanImpl)span).setClientTracer(brave.clientTracer());
    }

    @Override
    public <T> BraveSpanBuilderImpl join(T carrier) {
        BraveSpanBuilderImpl builder = (BraveSpanBuilderImpl) super.join( carrier);
        brave.serverTracer().setStateCurrentTrace(builder.traceId, builder.spanId, null, builder.operationName);
        brave.serverTracer().setServerReceived();
        builder.withServerTracer(brave.serverTracer());
        return builder;
    }

}
