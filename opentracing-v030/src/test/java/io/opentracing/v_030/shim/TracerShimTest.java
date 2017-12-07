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
package io.opentracing.v_030.shim;

import org.junit.Before;
import org.junit.Test;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.util.AutoFinishScopeManager;
import io.opentracing.util.ThreadLocalScopeManager;
import io.opentracing.v_030.ActiveSpan;
import io.opentracing.v_030.Span;
import io.opentracing.v_030.SpanContext;
import io.opentracing.v_030.Tracer;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public final class TracerShimTest {
    private final MockTracer mockTracer = new MockTracer(new AutoFinishScopeManager(),
            Propagator.TEXT_MAP);
    private Tracer shim;

    @Before
    public void before() {
        mockTracer.reset();
        shim = new TracerShim(mockTracer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctorNullTracer() {
        new TracerShim(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctorUnsupportedScopeManager() {
        MockTracer tracer = new MockTracer(new ThreadLocalScopeManager(),
                Propagator.TEXT_MAP);
        new TracerShim(tracer);
    }

    @Test
    public void simpleSpan() {
        ActiveSpan span = null;
        try {
            span = shim.buildSpan("one").startActive();
            assertNotNull(span);
            assertNotNull(shim.activeSpan());
            assertEquals(0, mockTracer.finishedSpans().size());
        } finally {
            span.deactivate();
        }

        assertNull(shim.activeSpan());
        assertEquals(1, mockTracer.finishedSpans().size());
    }

    @Test
    public void captureSpan() {
        int captureCount = 3;
        ActiveSpan.Continuation continuations[] = new ActiveSpan.Continuation[captureCount];

        ActiveSpan span = null;
        try {
            span = shim.buildSpan("one").startActive();
            assertNotNull(shim.activeSpan());
            assertEquals(0, mockTracer.finishedSpans().size());

            for (int i = 0; i < captureCount; i++) {
                ActiveSpan.Continuation cont = span.capture();
                continuations[i] = cont;
                assertNotNull(cont);
            }
        } finally {
            span.deactivate();
        }

        for (int i = 0; i < captureCount; i++) {
            assertNull(shim.activeSpan());
            assertEquals(0, mockTracer.finishedSpans().size());

            try {
                continuations[i].activate();
                assertNotNull(shim.activeSpan());
                shim.activeSpan().setTag(Integer.toString(i), "value");

            } finally {
                shim.activeSpan().deactivate();
            }
        }

        assertNull(shim.activeSpan());
        assertEquals(1, mockTracer.finishedSpans().size());

        Map<String, Object> tags = mockTracer.finishedSpans().get(0).tags();
        assertEquals(captureCount, tags.size());
    }
}
