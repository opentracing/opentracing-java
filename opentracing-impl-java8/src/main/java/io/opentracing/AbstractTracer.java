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

import io.opentracing.propagation.Extractor;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.Injector;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

abstract class AbstractTracer implements Tracer {

    private final PropagationRegistry registry = new PropagationRegistry();

    protected AbstractTracer() {}

    abstract AbstractSpanBuilder createSpanBuilder(String operationName);

    @Override
    public SpanBuilder buildSpan(String operationName){
        return createSpanBuilder(operationName);
    }

    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
        registry.getInjector((Class<C>)carrier.getClass()).inject(spanContext, carrier);
    }

    @Override
    public <C> SpanContext extract(Format<C> format, C carrier) {
        return registry.getExtractor((Class<C>)carrier.getClass()).extract(carrier);
    }

    public <C> Injector<C> register(Class<C> carrierType, Injector<C> injector) {
        return registry.register(carrierType, injector);
    }

    public <C> Extractor<C> register(Class<C> carrierType, Extractor<C> extractor) {
        return registry.register(carrierType, extractor);
    }

    private static class PropagationRegistry {

        private final ConcurrentMap<Class, Injector> injectors = new ConcurrentHashMap<>();
        private final ConcurrentMap<Class, Extractor> extractors = new ConcurrentHashMap<>();

        public <C> Injector<C> getInjector(Class<C> carrierType) {
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

        public <C> Extractor<C> getExtractor(Class<C> carrierType) {
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

        public <C> Injector<C> register(Class<C> carrierType, Injector<C> injector) {
            return injectors.putIfAbsent(carrierType, injector);
        }

        public <C> Extractor<C> register(Class<C> carrierType, Extractor<C> extractor) {
            return extractors.putIfAbsent(carrierType, extractor);
        }
    }

}
