/*
 * Copyright 2016-2017 The OpenTracing Authors
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
package io.opentracing.util.v_030;

import io.opentracing.ScopeManager;
import io.opentracing.Scope;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.v_030.ActiveSpan;
import io.opentracing.v_030.BaseSpan;
import io.opentracing.v_030.Span;
import io.opentracing.v_030.Tracer;

public class TracerShim implements Tracer {
    io.opentracing.Tracer tracer;

    public TracerShim(io.opentracing.Tracer tracer) {
        this.tracer = tracer;
    }

    protected ActiveSpanShim createActiveSpanShim(Scope scope) {
        return new ActiveSpanShim(scope);
    }

    @Override
    public ActiveSpan activeSpan() {
        if (tracer.scopeManager().active() == null)
            return null;

        return createActiveSpanShim(tracer.scopeManager().active());
    }

    @Override
    public ActiveSpan makeActive(Span span) {
        io.opentracing.Span wrappedSpan = ((SpanWrapper)span).span();
        return createActiveSpanShim(tracer.scopeManager().activate(wrappedSpan));
    }

    @Override
    public SpanBuilder buildSpan(String operationName) {
        return new SpanBuilderShim(tracer.buildSpan(operationName));
    }

    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
        tracer.inject(spanContext, format, carrier);
    }

    @Override
    public <C> SpanContext extract(Format<C> format, C carrier) {
        return tracer.extract(format, carrier);
    }

    private final class SpanBuilderShim implements Tracer.SpanBuilder {
        io.opentracing.Tracer.SpanBuilder builder;

        public SpanBuilderShim(io.opentracing.Tracer.SpanBuilder builder) {
            this.builder = builder;
        }

        @Override
        public SpanBuilderShim asChildOf(SpanContext parent) {
            builder.asChildOf(parent);
            return this;
        }

        @Override
        public SpanBuilderShim asChildOf(BaseSpan<?> parent) {
            builder.asChildOf(((SpanWrapper)parent).span());
            return this;
        }

        @Override
        public SpanBuilderShim addReference(String referenceType, SpanContext referencedContext) {
            builder.addReference(referenceType, referencedContext);
            return this;
        }

        @Override
        public SpanBuilderShim ignoreActiveSpan() {
            builder.ignoreActiveSpan();
            return this;
        }

        @Override
        public SpanBuilderShim withTag(String key, String value) {
            builder.withTag(key, value);
            return this;
        }

        @Override
        public SpanBuilderShim withTag(String key, boolean value) {
            builder.withTag(key, value);
            return this;
        }

        @Override
        public SpanBuilderShim withTag(String key, Number value) {
            builder.withTag(key, value);
            return this;
        }

        @Override
        public SpanBuilderShim withStartTimestamp(long microseconds) {
            builder.withStartTimestamp(microseconds);
            return this;
        }

        @Override
        public ActiveSpan startActive() {
            Scope scope = builder.startActive();
            return createActiveSpanShim(scope);
        }

        @Override
        public Span startManual() {
            return new SpanShim(builder.startManual());
        }

        @Override
        public Span start() {
            return new SpanShim(builder.start());
        }
    }
}
