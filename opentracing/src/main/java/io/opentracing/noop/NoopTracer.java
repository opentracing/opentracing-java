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
package io.opentracing.noop;

import io.opentracing.Span;
import io.opentracing.Tracer;

public class NoopTracer implements Tracer {

    private SpanBuilder defaultSpanBuilder = new NoopSpanBuilder();

    @Override
    public SpanBuilder buildSpan(String operationName) {
        return defaultSpanBuilder;
    }

    @Override
    public <T> void inject(Span span, T carrier) {}

    @Override
    public <T> SpanBuilder join(T carrier) {
        return defaultSpanBuilder;
    }
}
