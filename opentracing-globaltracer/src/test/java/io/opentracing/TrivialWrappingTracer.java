/**
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
package io.opentracing;

import io.opentracing.propagation.Format;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A 'trivial' wrapping tracer that delegates all calls as-is.
 */
final class TrivialWrappingTracer implements Tracer {
    private static final Logger LOGGER = Logger.getLogger(TrivialWrappingTracer.class.getName());
    static final AtomicInteger SEQUENCER = new AtomicInteger(0);

    static final GlobalTracer.UpdateFunction WRAP = new GlobalTracer.UpdateFunction() {
        @Override
        public Tracer apply(Tracer current) {
            try {
                Tracer newWrapper = new TrivialWrappingTracer(current);
                Thread.sleep(100);
                return newWrapper;
            } catch (InterruptedException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    };

    final int nr = SEQUENCER.incrementAndGet();
    final Tracer wrapped;

    TrivialWrappingTracer(Tracer delegate) {
        if (delegate == null) throw new NullPointerException("Cannot wrap <null> Tracer.");
        this.wrapped = delegate;
        LOGGER.log(Level.FINE, "New {0} created.", this);
    }

    @Override
    public SpanBuilder buildSpan(String operationName) {
        return wrapped.buildSpan(operationName);
    }

    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
        wrapped.inject(spanContext, format, carrier);
    }

    @Override
    public <C> SpanContext extract(Format<C> format, C carrier) {
        return wrapped.extract(format, carrier);
    }

    List<Tracer> flatten() {
        List<Tracer> flatList = new ArrayList<Tracer>();
        flatList.add(this);
        if (wrapped instanceof TrivialWrappingTracer) {
            flatList.addAll(((TrivialWrappingTracer) wrapped).flatten());
        } else {
            flatList.add(wrapped);
        }
        return flatList;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{nr=" + nr + ", wrapped=" + wrapped + '}';
    }

}
