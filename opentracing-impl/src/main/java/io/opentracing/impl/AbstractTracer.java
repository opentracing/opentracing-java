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

import io.opentracing.SpanScheduler;
import io.opentracing.SpanContext;
import io.opentracing.ThreadLocalScheduler;
import io.opentracing.Tracer;
import io.opentracing.propagation.Extractor;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.Injector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

abstract class AbstractTracer implements Tracer {

    static final boolean BAGGAGE_ENABLED = !Boolean.getBoolean("opentracing.propagation.dropBaggage");

    private final PropagationRegistry registry = new PropagationRegistry();
    private SpanScheduler scheduler;


    protected AbstractTracer() {
        this(new ThreadLocalScheduler());
    }

    protected AbstractTracer(SpanScheduler scheduler) {
        this.scheduler = scheduler;
        registry.register(Format.Builtin.TEXT_MAP, new TextMapInjectorImpl(this));
        registry.register(Format.Builtin.TEXT_MAP, new TextMapExtractorImpl(this));
    }

    abstract AbstractSpanBuilder createSpanBuilder(String operationName);

    @Override
    public SpanScheduler spanScheduler() {
        return this.scheduler;
    }

    @Override
    public SpanBuilder buildSpan(String operationName){
        return createSpanBuilder(operationName);
    }

    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
        registry.getInjector(format).inject(spanContext, carrier);
    }

     @Override
    public <C> SpanBuilder extract(Format<C> format, C carrier) {
        return registry.getExtractor(format).extract(carrier);
    }

    public <C> Injector<C> register(Format<C> format, Injector<C> injector) {
        return registry.register(format, injector);
    }

    public <C> Extractor<C> register(Format<C> format, Extractor<C> extractor) {
        return registry.register(format, extractor);
    }

    /** @return the minimal set of properties required to propagate this span */
    abstract Map<String,Object> getTraceState(SpanContext spanContext);

    private static class PropagationRegistry {

        private final ConcurrentMap<Format, Injector> injectors = new ConcurrentHashMap<>();
        private final ConcurrentMap<Format, Extractor> extractors = new ConcurrentHashMap<>();

        public <C> Injector<C> getInjector(Format<C> format) {
            if (injectors.containsKey(format)) {
                return injectors.get(format);
            }
            throw new AssertionError("no registered injector for " + format);
        }

        public <C> Extractor<C> getExtractor(Format<C> format) {
            if (extractors.containsKey(format)) {
                return extractors.get(format);
            }
            throw new AssertionError("no registered extractor for " + format);
        }

        public <C> Injector<C> register(Format<C> format, Injector<C> injector) {
            return injectors.put(format, injector);
        }

        public <C> Extractor<C> register(Format<C> format, Extractor<C> extractor) {
            return extractors.put(format, extractor);
        }
    }

}
