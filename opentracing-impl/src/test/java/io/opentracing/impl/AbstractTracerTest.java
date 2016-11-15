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
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.propagation.TextMapInjectAdapter;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.*;


public final class AbstractTracerTest {

    /**
     * Test of buildSpan method, of class AbstractTracer.
     */
    @Test
    public void testBuildSpan() {
        System.out.println("buildSpan");
        String operationName = "test-build-span-operation-name";
        AbstractTracer instance = new TestTracerImpl();
        Tracer.SpanBuilder result = instance.buildSpan(operationName);
        AbstractSpan span = (AbstractSpan) result.start();
        assertNotNull("Expected to create a valid Span", span);
        assertEquals("Expected to create a Span with operationName", operationName, span.getOperationName());
    }

    /**
     * Test of inject method, of class AbstractTracer.
     */
    @Test
    public void testInject() {
        System.out.println("inject");
        AbstractTracer instance = new TestTracerImpl();
        instance.register(Format.Builtin.TEXT_MAP, new TestTextMapInjectorImpl());

        AbstractSpanContext ctx = instance.createSpanContext(Collections.singletonMap("opname", "test-inject-span"));
        Span span = new TestSpanImpl("test-inject-span", ctx);
        Map<String,String> map = new HashMap<>();
        TextMap carrier = new TextMapInjectAdapter(map);
        instance.inject(span.context(), Format.Builtin.TEXT_MAP, carrier);

        assertEquals(
                "marker should have been injected into map",
                "test-inject-span", map.get("test-marker"));
    }

    /**
     * Test of extract method, of class AbstractTracer, with an empty carrier.
     */
    @Test
    public void testEmptyExtract() {
        System.out.println("empty extract");
        AbstractTracer instance = new TestTracerImpl();
        instance.register(Format.Builtin.TEXT_MAP, new TestTextMapExtractorImpl(instance));

        Map<String,String> map = Collections.singletonMap("garbageEntry", "garbageVal");
        TextMap carrier = new TextMapExtractAdapter(map);
        SpanContext emptyResult = instance.extract(Format.Builtin.TEXT_MAP, carrier);
        assertTrue("Should extract NoopSpanContext", emptyResult == NoopSpanContext.INSTANCE);
    }

    /**
     * Test of extract method, of class AbstractTracer, with a valid carrier.
     */
    @Test
    public void testNonEmptyExtract() {
        System.out.println("non-empty extract");
        AbstractTracer instance = new TestTracerImpl();
        instance.register(Format.Builtin.TEXT_MAP, new TestTextMapExtractorImpl(instance));

        Map<String,String> map = Collections.singletonMap("test-marker", "whatever");
        TextMap carrier = new TextMapExtractAdapter(map);
        SpanContext result = instance.extract(Format.Builtin.TEXT_MAP, carrier);
        assertTrue("Should NOT extract NoopSpanContext", result != NoopSpanContext.INSTANCE);
    }

    @Test
    public void testExtractAsParent() throws Exception {
        Map<String,String> map = Collections.singletonMap("test-marker", "whatever");
        TextMapExtractAdapter adapter = new TextMapExtractAdapter(map);
        AbstractTracer tracer = new TestTracerImpl();
        SpanContext parent = tracer.extract(Format.Builtin.TEXT_MAP, adapter);
        assert NoopSpan.INSTANCE != tracer.buildSpan("child").asChildOf(parent).start();
    }

    @Test
    public void testExtractOfNoParent() throws Exception {
        AbstractTracer tracer = new TestTracerImpl();
        assert NoopSpan.INSTANCE == tracer.buildSpan("child").asChildOf((Span)NoopSpan.INSTANCE).start();
        assert NoopSpan.INSTANCE == tracer.buildSpan("child").asChildOf(NoopSpanContext.INSTANCE).start();
        assert NoopSpan.INSTANCE == tracer.buildSpan("child").asChildOf((Span) NoopSpan.INSTANCE).start();
    }

    final class TestTracerImpl extends AbstractTracer {

        static final String OPERATION_NAME = "operation-name";

        @Override
        public AbstractSpanBuilder createSpanBuilder(String operationName) {
            return new AbstractSpanBuilder(operationName) {
                @Override
                protected AbstractSpan createSpan() {
                    return new TestSpanImpl(this.operationName, createSpanContext(emptyMap()));
                }
            };
        }
        
        @Override
        boolean isTraceState(String key, Object value) {
            return false;
        }

        @Override
        Map<String, Object> getTraceState(SpanContext spanContext) {
            return new HashMap<>(((AbstractSpan)spanContext).getBaggage());
        }

        @Override
        AbstractSpanContext createSpanContext(Map<String, Object> traceState) {
            return new AbstractSpanContext(traceState, this) {};
        }
    }

}
