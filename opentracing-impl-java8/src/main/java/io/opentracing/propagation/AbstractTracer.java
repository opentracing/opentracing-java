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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.Tracer.SpanBuilder;

abstract class AbstractTracer implements Tracer {

    private final PropagationRegistry registry = new PropagationRegistry();

    protected AbstractTracer() {}

    abstract AbstractSpanBuilder createSpanBuilder(String operationName);

    @Override
    public SpanBuilder buildSpan(String operationName){
        return createSpanBuilder(operationName);
    }

    @Override
    public void inject(SpanContext spanContext, Object carrier) {
        registry.getInjector(carrier.getClass()).inject(spanContext, carrier);
    }

    @Override
    public SpanContext extract(Object carrier) {
        return registry.getExtractor(carrier.getClass()).extract(carrier);
    }

    public Injector register(Class carrierType, Injector injector) {
        return registry.register(carrierType, injector);
    }

    public Extractor register(Class carrierType, Extractor extractor) {
        return registry.register(carrierType, extractor);
    }

    private static class PropagationRegistry {

        private final ConcurrentMap<Class, Injector> injectors = new ConcurrentHashMap<>();
        private final ConcurrentMap<Class, Extractor> extractors = new ConcurrentHashMap<>();

        public Injector getInjector(Class carrierType) {
            Class<?> c = carrierType;
            // match first on concrete classes
            do {
                if (injectors.containsKey(c)) {
                    return injectors.get(c);
                }
                c = c.getSuperclass();
            } while (c != null);
            // match second on interfaces
            for (Class<?> iface : carrierType.getInterfaces()) {
                if (injectors.containsKey(iface)) {
                    return injectors.get(iface);
                }
            }
            throw new AssertionError("no registered injector for " + carrierType.getName());
        }

        public Extractor getExtractor(Class carrierType) {
            Class<?> c = carrierType;
            // match first on concrete classes
            do {
                if (extractors.containsKey(c)) {
                    return extractors.get(c);
                }
                c = c.getSuperclass();
            } while (c != null);
            // match second on interfaces
            for (Class<?> iface : carrierType.getInterfaces()) {
                if (extractors.containsKey(iface)) {
                    return extractors.get(iface);
                }
            }
            throw new AssertionError("no registered extractor for " + carrierType.getName());
        }

        public Injector register(Class carrierType, Injector injector) {
            return injectors.putIfAbsent(carrierType, injector);
        }

        public Extractor register(Class carrierType, Extractor extractor) {
            return extractors.putIfAbsent(carrierType, extractor);
        }
    }

}
