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
        String operationName = "test-inject-span";
        Span span = new AbstractSpan(operationName) {};
        Map<String,String> map = new HashMap<>();
        TextMapWriter carrier = new TextMapImpl(map);
        AbstractTracer instance = new TestTracerImpl();
        instance.inject(span, carrier);

        assertTrue(
                "operationName should have been injected into map",
                map.containsKey(TestTracerImpl.OPERATION_NAME));

        assertEquals(
                "operationName should have been injected into map",
                operationName, map.get(TestTracerImpl.OPERATION_NAME));
    }

    /**
     * Test of join method, of class AbstractTracer.
     */
    @Test
    public void testJoin() {
        System.out.println("join");
        String operationName = "test-join-span";
        Map<String,String> map = Collections.singletonMap(TestTracerImpl.OPERATION_NAME, operationName);
        TextMapReader carrier = new TextMapImpl(map);
        AbstractTracer instance = new TestTracerImpl();
        Tracer.SpanBuilder result = instance.join(carrier);
        AbstractSpan span = (AbstractSpan) result.start();
        assertNotNull("Expected to create a valid Span", span);
        assertEquals("Expected to create a Span with operationName", operationName, span.operationName);
    }

    /**
     * Test of register method, of class AbstractTracer.
     */
    @Test
    public void testRegister_Class_Injector() {
        System.out.println("register injector");
        AbstractTracer instance = new TestTracerImpl();
        Injector expResult = new TextFormatInjectorImpl(instance);
        Injector result = instance.register(TextMapWriter.class, expResult);

        assertNotEquals("TextMapWriter carrier should already be registered", expResult, result);
        assertNotNull("TextMapWriter carrier should already be registered", result);
    }

    @Test
    public void testRegister_Class_Injector_superclass_before_interface() {
        System.out.println("testRegister_Class_Injector_superclass_before_interface");
        String testKey = "testRegister_Class_Injector_superclass_before_interface";
        AbstractTracer instance = new TestTracerImpl();
        Map<String,String> map = new HashMap<>();
        TextMapImpl carrier = new TextMapImpl(map);

        Injector result = instance.register(
                TextMapImpl.class,
                (Span s, TextMapImpl c) -> { c.put(testKey, "true"); });

        assertNull("TextMapImpl carrier should get registered", result);

        String operationName = "test-register-superclass_before_interface-span";
        Span span = new AbstractSpan(operationName) {};
        instance.inject(span, carrier);

        assertTrue("test injector should have been used", map.containsKey(testKey));
        assertEquals("test injector should have been used", map.get(testKey), "true");
    }

    /**
     * Test of register method, of class AbstractTracer.
     */
    @Test
    public void testRegister_Class_Extractor() {
        System.out.println("register extractor");
        AbstractTracer instance = new TestTracerImpl();
        Extractor expResult = new TextFormatExtractorImpl(instance);
        Extractor result = instance.register(TextMapReader.class, expResult);

        assertNotEquals("TextMapReader carrier should already be registered", expResult, result);
        assertNotNull("TextMapReader carrier should already be registered", result);
    }


    @Test
    public void testRegister_Class_Extractor_superclass_before_interface() {
        System.out.println("testRegister_Class_Extractor_superclass_before_interface");
        String testKey = "testRegister_Class_Extractor_superclass_before_interface";
        final AbstractTracer instance = new TestTracerImpl();
        Map<String,String> map = new HashMap<>();
        TextMapImpl carrier = new TextMapImpl(map);

        Extractor result = instance.register(
                TextMapImpl.class,
                (TextMapImpl c) -> {
                    c.put(testKey, "true");
                    return instance.createSpanBuilder();
                });

        assertNull("TextMapImpl carrier should get registered", result);

        instance.join(carrier);

        assertTrue("test extractor should have been used", map.containsKey(testKey));
        assertEquals("test extractor should have been used", map.get(testKey), "true");
    }

    final class TestTracerImpl extends AbstractTracer {

        static final String OPERATION_NAME = "operation-name";

        @Override
        public AbstractSpanBuilder createSpanBuilder() {
            return new AbstractSpanBuilder() {
                @Override
                protected AbstractSpan createSpan() {
                    return new AbstractSpan(this.operationName) {
                    };
                }

                @Override
                boolean isTraceState(String key, Object value) {
                    return OPERATION_NAME.equals(key);
                }

                @Override
                boolean isBaggage(String key, Object value) {
                    return !isTraceState(key, value);
                }

                @Override
                SpanBuilder withStateItem(String key, Object value) {
                    assert isTraceState(key, value);
                    this.operationName = (String) value;
                    return this;
                }
            };
        }

        @Override
        public Map<String, String> getTraceState(Span span) {
            return Collections.singletonMap(OPERATION_NAME, ((AbstractSpan)span).operationName);
        }

        @Override
        public Map<String, String> getBaggage(Span span) {
            return Collections.emptyMap();
        }
    }

}
