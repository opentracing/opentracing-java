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
package io.opentracing.mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Adapters;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.propagation.TextMapInjectAdapter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class MockTracerTest {
    @Test
    public void testRootSpan() {
        // Create and finish a root Span.
        MockTracer tracer = new MockTracer();
        {
            Span span = tracer.buildSpan("tester").withStartTimestamp(1000).start();
            span.setTag("string", "foo");
            span.setTag("int", 7);
            span.log("foo");
            Map<String, Object> fields = new HashMap<>();
            fields.put("f1", 4);
            fields.put("f2", "two");
            span.log(1002, fields);
            span.log(1003, "event name");
            span.finish(2000);
        }
        List<MockSpan> finishedSpans = tracer.finishedSpans();

        // Check that the Span looks right.
        assertEquals(1, finishedSpans.size());
        MockSpan finishedSpan = finishedSpans.get(0);
        assertEquals("tester", finishedSpan.operationName());
        assertEquals(0, finishedSpan.parentId());
        assertNotEquals(0, finishedSpan.context().traceId());
        assertNotEquals(0, finishedSpan.context().spanId());
        assertEquals(1000, finishedSpan.startMicros());
        assertEquals(2000, finishedSpan.finishMicros());
        Map<String, Object> tags = finishedSpan.tags();
        assertEquals(2, tags.size());
        assertEquals(7, tags.get("int"));
        assertEquals("foo", tags.get("string"));
        List<MockSpan.LogEntry> logs = finishedSpan.logEntries();
        assertEquals(3, logs.size());
        {
            MockSpan.LogEntry log = logs.get(0);
            assertEquals(1, log.fields().size());
            assertEquals("foo", log.fields().get("event"));
        }
        {
            MockSpan.LogEntry log = logs.get(1);
            assertEquals(1002, log.timestampMicros());
            assertEquals(4, log.fields().get("f1"));
            assertEquals("two", log.fields().get("f2"));
        }
        {
            MockSpan.LogEntry log = logs.get(2);
            assertEquals(1003, log.timestampMicros());
            assertEquals("event name", log.fields().get("event"));
        }
    }

    @Test
    public void testChildSpan() {
        // Create and finish a root Span.
        MockTracer tracer = new MockTracer();
        {
            Span parent = tracer.buildSpan("parent").withStartTimestamp(1000).start();
            Span child = tracer.buildSpan("child").withStartTimestamp(1100).asChildOf(parent).start();
            child.finish(1900);
            parent.finish(2000);
        }
        List<MockSpan> finishedSpans = tracer.finishedSpans();

        // Check that the Spans look right.
        assertEquals(2, finishedSpans.size());
        MockSpan child = finishedSpans.get(0);
        MockSpan parent = finishedSpans.get(1);
        assertEquals("child", child.operationName());
        assertEquals("parent", parent.operationName());
        assertEquals(parent.context().spanId(), child.parentId());
        assertEquals(parent.context().traceId(), child.context().traceId());

    }

    @Test
    public void testStartTimestamp() throws InterruptedException {
        MockTracer tracer = new MockTracer();
        long startMicros;
        {
            Tracer.SpanBuilder fooSpan = tracer.buildSpan("foo");
            Thread.sleep(2);
            startMicros = System.currentTimeMillis() * 1000;
            fooSpan.start().finish();
        }
        List<MockSpan> finishedSpans = tracer.finishedSpans();

        Assert.assertEquals(1, finishedSpans.size());
        MockSpan span = finishedSpans.get(0);
        Assert.assertTrue(startMicros <= span.startMicros());
        Assert.assertTrue(System.currentTimeMillis() * 1000 >= span.finishMicros());
    }

    @Test
    public void testStartExplicitTimestamp() throws InterruptedException {
        MockTracer tracer = new MockTracer();
        long startMicros = 2000;
        {
            tracer.buildSpan("foo")
                    .withStartTimestamp(startMicros)
                    .start()
                    .finish();
        }
        List<MockSpan> finishedSpans = tracer.finishedSpans();

        Assert.assertEquals(1, finishedSpans.size());
        Assert.assertEquals(startMicros, finishedSpans.get(0).startMicros());
    }

    @Test
    public void testTextMapPropagatorTextMap() {
        MockTracer tracer = new MockTracer(MockTracer.Propagator.TEXT_MAP);
        HashMap<String, String> injectMap = new HashMap<>();
        injectMap.put("foobag", "donttouch");
        {
            Span parentSpan = tracer.buildSpan("foo")
                    .start();
            parentSpan.setBaggageItem("foobag", "fooitem");
            parentSpan.finish();

            tracer.inject(parentSpan.context(), Format.Builtin.TEXT_MAP,
                    new TextMapInjectAdapter(injectMap));

            SpanContext extract = tracer.extract(Format.Builtin.TEXT_MAP, new TextMapExtractAdapter(injectMap));

            Span childSpan = tracer.buildSpan("bar")
                    .asChildOf(extract)
                    .start();
            childSpan.setBaggageItem("barbag", "baritem");
            childSpan.finish();
        }
        List<MockSpan> finishedSpans = tracer.finishedSpans();

        Assert.assertEquals(2, finishedSpans.size());
        Assert.assertEquals(finishedSpans.get(0).context().traceId(), finishedSpans.get(1).context().traceId());
        Assert.assertEquals(finishedSpans.get(0).context().spanId(), finishedSpans.get(1).parentId());
        Assert.assertEquals("fooitem", finishedSpans.get(0).getBaggageItem("foobag"));
        Assert.assertNull(finishedSpans.get(0).getBaggageItem("barbag"));
        Assert.assertEquals("fooitem", finishedSpans.get(1).getBaggageItem("foobag"));
        Assert.assertEquals("baritem", finishedSpans.get(1).getBaggageItem("barbag"));
        Assert.assertEquals("donttouch", injectMap.get("foobag"));
    }

    @Test
    public void testTextMapPropagatorHttpHeaders() {
        MockTracer tracer = new MockTracer(MockTracer.Propagator.TEXT_MAP);
        {
            Span parentSpan = tracer.buildSpan("foo")
                    .start();
            parentSpan.finish();

            HashMap<String, String> injectMap = new HashMap<>();
            tracer.inject(parentSpan.context(), Format.Builtin.HTTP_HEADERS,
                    new TextMapInjectAdapter(injectMap));

            SpanContext extract = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapExtractAdapter(injectMap));

            tracer.buildSpan("bar")
                    .asChildOf(extract)
                    .start()
                    .finish();
        }
        List<MockSpan> finishedSpans = tracer.finishedSpans();

        Assert.assertEquals(2, finishedSpans.size());
        Assert.assertEquals(finishedSpans.get(0).context().traceId(), finishedSpans.get(1).context().traceId());
        Assert.assertEquals(finishedSpans.get(0).context().spanId(), finishedSpans.get(1).parentId());
    }

    @Test
    public void testBinaryPropagator() {
        MockTracer tracer = new MockTracer(MockTracer.Propagator.BINARY);
        {
            Span parentSpan = tracer.buildSpan("foo")
                    .start();
            parentSpan.setBaggageItem("foobag", "fooitem");
            parentSpan.finish();

            ByteArrayOutputStream injectStream = new ByteArrayOutputStream();
            tracer.inject(parentSpan.context(), Format.Builtin.BINARY,
                    Adapters.injectBinary(injectStream));

            ByteArrayInputStream extractStream = new ByteArrayInputStream(injectStream.toByteArray());
            SpanContext extract = tracer.extract(Format.Builtin.BINARY, Adapters.extractBinary(extractStream));

            Span childSpan = tracer.buildSpan("bar")
                    .asChildOf(extract)
                    .start();
            childSpan.setBaggageItem("barbag", "baritem");
            childSpan.finish();
        }
        List<MockSpan> finishedSpans = tracer.finishedSpans();

        Assert.assertEquals(2, finishedSpans.size());
        Assert.assertEquals(finishedSpans.get(0).context().traceId(), finishedSpans.get(1).context().traceId());
        Assert.assertEquals(finishedSpans.get(0).context().spanId(), finishedSpans.get(1).parentId());
        Assert.assertEquals("fooitem", finishedSpans.get(0).getBaggageItem("foobag"));
        Assert.assertNull(finishedSpans.get(0).getBaggageItem("barbag"));
        Assert.assertEquals("fooitem", finishedSpans.get(1).getBaggageItem("foobag"));
        Assert.assertEquals("baritem", finishedSpans.get(1).getBaggageItem("barbag"));
    }
  
    @Test
    public void testActiveSpan() {
        MockTracer mockTracer = new MockTracer();
        Assert.assertNull(mockTracer.activeSpan());

        Scope scope = null;
        try {
            scope = mockTracer.buildSpan("foo").startActive(true);
            Assert.assertEquals(mockTracer.scopeManager().active().span(), mockTracer.activeSpan());
        } finally {
            scope.close();
        }

        Assert.assertNull(mockTracer.activeSpan());
    }

    @Test
    public void testReset() {
        MockTracer mockTracer = new MockTracer();

        mockTracer.buildSpan("foo")
            .startManual()
            .finish();

        assertEquals(1, mockTracer.finishedSpans().size());
        mockTracer.reset();
        assertEquals(0, mockTracer.finishedSpans().size());
    }
}
