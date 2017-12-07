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
import io.opentracing.Scope;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.util.ThreadLocalScopeManager;
import io.opentracing.v_030.ActiveSpan;
import io.opentracing.v_030.References;
import io.opentracing.v_030.Span;
import io.opentracing.v_030.SpanContext;
import io.opentracing.v_030.Tracer;
import io.opentracing.v_030.propagation.Format;
import io.opentracing.v_030.propagation.TextMapExtractAdapter;
import io.opentracing.v_030.propagation.TextMapInjectAdapter;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public final class BaseTracerShimTest {
    private final MockTracer mockTracer = new MockTracer(new ThreadLocalScopeManager(),
            Propagator.TEXT_MAP);
    private Tracer shim;

    final static class TestTracerShim extends BaseTracerShim {
        public TestTracerShim(io.opentracing.Tracer tracer) {
            super(tracer);
        }
    }

    @Before
    public void before() {
        mockTracer.reset();
        shim = new TestTracerShim(mockTracer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctorNullTracer() {
        new TestTracerShim(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void spanCapture() {
        shim.buildSpan("one").startActive().capture();
    }

    @Test
    public void activeSpan() {
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
        assertEquals("one", mockTracer.finishedSpans().get(0).operationName());
    }

    @Test
    public void activeSpanOnTheSide() {
        Scope scope = null;
        try {
            scope = mockTracer.buildSpan("one").startActive();
            assertNotNull(shim.activeSpan());
        } finally {
            scope.close();
        }

        assertNull(shim.activeSpan());
    }

    @Test
    public void activeSpanNone() {
        assertNull(shim.activeSpan());

        Span span = shim.buildSpan("one").startManual();
        assertNull(shim.activeSpan());
    }

    @Test(expected = IllegalArgumentException.class)
    public void makeActiveNull() {
        shim.makeActive(null);
    }

    @Test
    public void makeActive() {
        Span span = shim.buildSpan("one").startManual();
        ActiveSpan active = null;
        try {
            active = shim.makeActive(span);
            assertNotNull(active);
            assertNotNull(shim.activeSpan());
            assertEquals(0, mockTracer.finishedSpans().size());
        } finally {
            active.deactivate();
        }

        assertNull(shim.activeSpan());
        assertEquals(1, mockTracer.finishedSpans().size());
    }

    @Test
    public void injectExtractTextMap() {
        Map<String, String> injectMap = new HashMap<String, String>();

        Span span = shim.buildSpan("parent").startManual();
        span.finish();
        shim.inject(span.context(), Format.Builtin.TEXT_MAP, new TextMapInjectAdapter(injectMap));

        SpanContext extract = shim.extract(Format.Builtin.TEXT_MAP, new TextMapExtractAdapter(injectMap));
        shim.buildSpan("child").asChildOf(extract).startManual().finish();

        List<MockSpan> finishedSpans = mockTracer.finishedSpans();
        assertEquals(2, finishedSpans.size());
        assertEquals(finishedSpans.get(0).context().traceId(), finishedSpans.get(1).context().traceId());
    }

    @Test
    public void injectExtractHttp() {
        Map<String, String> injectMap = new HashMap<String, String>();

        Span span = shim.buildSpan("parent").startManual();
        span.finish();
        shim.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(injectMap));

        SpanContext extract = shim.extract(Format.Builtin.HTTP_HEADERS, new TextMapExtractAdapter(injectMap));
        shim.buildSpan("child").asChildOf(extract).startManual().finish();

        List<MockSpan> finishedSpans = mockTracer.finishedSpans();
        assertEquals(2, finishedSpans.size());
        assertEquals(finishedSpans.get(0).context().traceId(), finishedSpans.get(1).context().traceId());
    }

    @Test
    public void builderAsChildOfSpan() {
        Span parentSpan = shim.buildSpan("parent").startManual();
        Span childSpan = shim.buildSpan("child").asChildOf(parentSpan).startManual();
        childSpan.finish();
        parentSpan.finish();

        List<MockSpan> spans = mockTracer.finishedSpans();
        assertEquals(2, spans.size());
        assertEquals("child", spans.get(0).operationName());
        assertEquals("parent", spans.get(1).operationName());
        assertEquals(spans.get(0).parentId(), spans.get(1).context().spanId());
    }

    @Test
    public void builderAsChildOfActiveSpan() {
        ActiveSpan parentSpan, childSpan, childSpan2;
        parentSpan = childSpan = childSpan2 = null;
        try {
            parentSpan = shim.buildSpan("parent").startActive();
            try {
                childSpan = shim.buildSpan("child").startActive();
                try {
                    childSpan2 = shim.buildSpan("child2").asChildOf(parentSpan).startActive();
                } finally {
                    childSpan2.deactivate();
                }
            } finally {
                childSpan.deactivate();
            }
        } finally {
            parentSpan.deactivate();
        }

        List<MockSpan> spans = mockTracer.finishedSpans();
        assertEquals(3, spans.size());
        assertEquals("child2", spans.get(0).operationName());
        assertEquals("child", spans.get(1).operationName());
        assertEquals("parent", spans.get(2).operationName());
        assertEquals(spans.get(0).parentId(), spans.get(2).context().spanId());
    }

    @Test
    public void builderAsChildOfContext() {
        Span parentSpan = shim.buildSpan("parent").startManual();
        Span childSpan = shim.buildSpan("child").asChildOf(parentSpan.context()).startManual();
        childSpan.finish();
        parentSpan.finish();

        List<MockSpan> spans = mockTracer.finishedSpans();
        assertEquals(2, spans.size());
        assertEquals("child", spans.get(0).operationName());
        assertEquals("parent", spans.get(1).operationName());
        assertEquals(spans.get(0).parentId(), spans.get(1).context().spanId());
    }

    @Test
    public void builderIgnoreActiveSpan() {
        ActiveSpan parentSpan, childSpan;
        parentSpan = childSpan = null;
        try {
            parentSpan = shim.buildSpan("one").startActive();
            try {
                childSpan = shim.buildSpan("two").ignoreActiveSpan().startActive();
            } finally {
                childSpan.deactivate();
            }
        } finally {
            parentSpan.deactivate();
        }

        List<MockSpan> spans = mockTracer.finishedSpans();
        assertEquals(2, spans.size());
        assertEquals("two", spans.get(0).operationName());
        assertEquals("one", spans.get(1).operationName());
        assertNotEquals(spans.get(0).context().traceId(), spans.get(1).context().traceId());
    }

    @Test
    public void builderAddReference() {
        Span parentSpan = shim.buildSpan("parent").startManual();
        Span childSpan = shim.buildSpan("child").addReference(References.CHILD_OF, parentSpan.context()).startManual();
        childSpan.finish();
        parentSpan.finish();

        List<MockSpan> spans = mockTracer.finishedSpans();
        assertEquals(2, spans.size());
        assertEquals("child", spans.get(0).operationName());
        assertEquals("parent", spans.get(1).operationName());
        assertEquals(spans.get(0).parentId(), spans.get(1).context().spanId());
    }

    @Test
    public void builderWithTag() {
        shim.buildSpan("one")
            .withTag("string", "string")
            .withTag("boolean", true)
            .withTag("number", 13)
            .startManual()
            .finish();

        assertEquals(1, mockTracer.finishedSpans().size());
        
        Map<String, Object> tags = mockTracer.finishedSpans().get(0).tags();
        assertEquals("string", tags.get("string"));
        assertEquals(true, tags.get("boolean"));
        assertEquals(13, tags.get("number"));
    }

    @Test
    public void builderWithStartTimestamp() {
        shim.buildSpan("one")
            .withStartTimestamp(113)
            .startManual()
            .finish();

        assertEquals(1, mockTracer.finishedSpans().size());
        assertEquals(113, mockTracer.finishedSpans().get(0).startMicros());
    }

    @Test
    public void builderStart() {
        shim.buildSpan("one").start().finish();

        assertEquals(1, mockTracer.finishedSpans().size());
        assertEquals("one", mockTracer.finishedSpans().get(0).operationName());
    }
}
