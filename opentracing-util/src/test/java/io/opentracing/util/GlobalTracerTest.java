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
package io.opentracing.util;

import io.opentracing.NoopSpanBuilder;
import io.opentracing.NoopTracerFactory;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class GlobalTracerTest {

    private static void _setGlobal(Tracer tracer) {
        try {
            Field globalTracerField = GlobalTracer.class.getDeclaredField("globalTracer");
            globalTracerField.setAccessible(true);
            globalTracerField.set(null, tracer);
            globalTracerField.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException("Error reflecting globalTracer: " + e.getMessage(), e);
        }
    }

    @Before
    @After
    public void clearGlobalTracer() {
        _setGlobal(NoopTracerFactory.create());
    }

    @Test
    public void testGet_SingletonReference() {
        Tracer tracer = GlobalTracer.get();
        assertThat(tracer, is(instanceOf(GlobalTracer.class)));
        assertThat(tracer, is(sameInstance(GlobalTracer.get())));
    }

    @Test
    public void testMultipleRegistrations() {
        GlobalTracer.register(mock(Tracer.class));
        try {
            GlobalTracer.register(mock(Tracer.class));
            fail("Duplicate registration exception expected.");
        } catch (RuntimeException expected) {
            assertThat("Duplicate registration message", expected.getMessage(), is(notNullValue()));
        }
    }

    /**
     * Check leniency for duplicate registration with the same tracer by mistake.
     */
    @Test
    public void testMultipleRegistrations_sameTracer() {
        Tracer mockTracer = mock(Tracer.class);
        GlobalTracer.register(mockTracer);
        GlobalTracer.register(mockTracer);
    }

    @Test
    public void testNoopTracerByDefault() {
        Tracer.SpanBuilder spanBuilder = GlobalTracer.get().buildSpan("my-operation");
        assertThat(spanBuilder, is(instanceOf(NoopSpanBuilder.class)));
    }

    @Test
    public void testDelegation_buildSpan() {
        Tracer mockTracer = mock(Tracer.class);
        GlobalTracer.register(mockTracer);
        GlobalTracer.get().buildSpan("my-operation");

        verify(mockTracer).buildSpan(eq("my-operation"));
        verifyNoMoreInteractions(mockTracer);
    }

    @Test
    public void testDelegation_inject() {
        Tracer mockTracer = mock(Tracer.class);
        SpanContext mockContext = mock(SpanContext.class);
        Format<Object> mockFormat = mock(Format.class);
        Object mockCarrier = mock(Object.class);
        GlobalTracer.register(mockTracer);
        GlobalTracer.get().inject(mockContext, mockFormat, mockCarrier);

        verify(mockTracer).inject(eq(mockContext), eq(mockFormat), eq(mockCarrier));
        verifyNoMoreInteractions(mockTracer, mockContext, mockFormat, mockCarrier);
    }

    @Test
    public void testDelegation_extract() {
        Tracer mockTracer = mock(Tracer.class);
        Format<Object> mockFormat = mock(Format.class);
        Object mockCarrier = mock(Object.class);
        GlobalTracer.register(mockTracer);
        GlobalTracer.get().extract(mockFormat, mockCarrier);

        verify(mockTracer).extract(eq(mockFormat), eq(mockCarrier));
        verifyNoMoreInteractions(mockTracer, mockFormat, mockCarrier);
    }

}
