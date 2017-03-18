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

import io.opentracing.propagation.Format;

public interface NoopTracer extends Tracer {
}

final class NoopTracerImpl implements NoopTracer {
    final static NoopTracer INSTANCE = new NoopTracerImpl();
    final static ThreadLocalScheduler SCHEDULER = new ThreadLocalScheduler();

    @Override
    public SpanBuilder buildSpan(String operationName) { return NoopSpanBuilderImpl.INSTANCE; }

    @Override
    public SpanScheduler spanScheduler() { return SCHEDULER; }

    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {}

    @Override
    public <C> SpanContext extract(Format<C> format, C carrier) { return NoopSpanBuilderImpl.INSTANCE; }

}

