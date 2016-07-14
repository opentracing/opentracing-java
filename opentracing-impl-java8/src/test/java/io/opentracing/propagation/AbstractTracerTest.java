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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


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
        assertEquals("Expected to create a Span with operationName", operationName, span.operationName);
    }

    /**
     * Test of inject method, of class AbstractTracer.
     */
    @Test
    public void testInject() {
        System.out.println("inject");
        AbstractTracer instance = new TestTracerImpl();
        instance.register(TextMapWriter.class, new TestTextMapInjectorImpl());

        String operationName = "test-inject-span";
        Span span = new AbstractSpan(operationName, new TestSpanContextImpl("whatever")) {};
        Map<String,String> map = new HashMap<>();
        TextMapWriter carrier = new TextMapImpl(map);
        instance.inject(span.context(), carrier);

        assertEquals(
                "marker should have been injected into map",
                "whatever", map.get("test-marker"));
    }

    /**
     * Test of extract method, of class AbstractTracer, with an empty carrier.
     */
    @Test
    public void testEmptyExtract() {
        System.out.println("empty extract");
        AbstractTracer instance = new TestTracerImpl();
        instance.register(TextMapReader.class, new TestTextMapExtractorImpl());

        Map<String,String> map = Collections.singletonMap("garbageEntry", "garbageVal");
        TextMapReader carrier = new TextMapImpl(map);
        SpanContext emptyResult = instance.extract(carrier);
        assertNull("Should be nothing to extract", emptyResult);
    }

    /**
     * Test of extract method, of class AbstractTracer, with a valid carrier.
     */
    @Test
    public void testNonEmptyExtract() {
        System.out.println("non-empty extract");
        AbstractTracer instance = new TestTracerImpl();
        instance.register(TextMapReader.class, new TestTextMapExtractorImpl());

        Map<String,String> map = Collections.singletonMap("test-marker", "whatever");
        TextMapReader carrier = new TextMapImpl(map);
        SpanContext result = instance.extract(carrier);
        assertNotNull("Should be something to extract", result);
        assertEquals("Should find the marker", "whatever", ((TestSpanContextImpl)result).getMarker());
    }

    final class TestTracerImpl extends AbstractTracer {

        static final String OPERATION_NAME = "operation-name";

        @Override
        public AbstractSpanBuilder createSpanBuilder(String operationName) {
            return new AbstractSpanBuilder(operationName) {
                @Override
                protected AbstractSpan createSpan() {
                    return new AbstractSpan(this.operationName, new TestSpanContextImpl("op=" + operationName)) {
                    };
                }
            };
        }
    }

}
