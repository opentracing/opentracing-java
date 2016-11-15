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

import io.opentracing.NoopSpanContext;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;

import java.util.Collections;
import java.util.Map;

final class NoopTracer extends AbstractTracer implements io.opentracing.NoopTracer {

    static final NoopTracer INSTANCE = new NoopTracer();

    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {}

    @Override
    public <C> SpanContext extract(Format<C> format, C carrier) {
        return NoopSpanContext.INSTANCE;
    }

    @Override
    AbstractSpanBuilder createSpanBuilder(String operationName) {
        return NoopSpanBuilder.INSTANCE;
    }
    
    @Override
    AbstractSpanContext createSpanContext(Map<String, Object> traceState) {
        // TODO Noop tracer must not fail. But NoopSpanContext is in a separate module and it cannot extend AbstractSpanContext.
        // The best solution would be to use NoopTracer implementation which does not extend AbstractTracer - the one from
        // opentracing-noop - and remove this one.
        throw new UnsupportedOperationException("NoopTracer cannot create SpanContexts");
    }

    @Override
    Map<String, Object> getTraceState(SpanContext spanContext) {
        return Collections.emptyMap();
    }

    @Override
    boolean isTraceState(String key, Object value) {
        return false;
    }
}
