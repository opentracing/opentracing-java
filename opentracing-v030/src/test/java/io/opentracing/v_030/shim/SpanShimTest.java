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
import io.opentracing.v_030.Span;
import io.opentracing.v_030.SpanContext;
import io.opentracing.v_030.Tracer;
import io.opentracing.v_030.shim.TracerShim;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SpanShimTest {
    private final MockTracer mockTracer = new MockTracer(new AutoFinishScopeManager(),
            Propagator.TEXT_MAP);
    private Tracer shim;

    @Before
    public void before() {
        mockTracer.reset();
        shim = new TracerShim(mockTracer);
    }

    @Test
    public void finishMicros() {
        shim.buildSpan("one").startManual()
            .finish(10100);

        List<MockSpan> finishedSpans = mockTracer.finishedSpans();
        assertEquals(1, finishedSpans.size());
        assertEquals(10100, finishedSpans.get(0).finishMicros());
    }

    @Test
    public void log() {
        shim.buildSpan("one").startManual()
            .log("myEvent")
            .finish();

        List<MockSpan> finishedSpans = mockTracer.finishedSpans();
        assertEquals(1, finishedSpans.size());

        List<MockSpan.LogEntry> logs = finishedSpans.get(0).logEntries();
        assertEquals(1, logs.size());
        assertEquals(1, logs.get(0).fields().size());
        assertEquals("myEvent", logs.get(0).fields().get("event"));
    }

    @Test
    public void setBaggageItem() {
        shim.buildSpan("one").startManual()
            .setBaggageItem("foobag", "foovalue")
            .finish();

        List<MockSpan> finishedSpans = mockTracer.finishedSpans();
        assertEquals(1, finishedSpans.size());
        assertEquals("foovalue", finishedSpans.get(0).getBaggageItem("foobag"));
    }

    @Test
    public void setOperationName() {
        shim.buildSpan("one").startManual()
            .setOperationName("1")
            .finish();

        List<MockSpan> finishedSpans = mockTracer.finishedSpans();
        assertEquals(1, finishedSpans.size());
        assertEquals("1", finishedSpans.get(0).operationName());
    }

    @Test
    public void setTag() {
        shim.buildSpan("one").startManual()
            .setTag("string", "string")
            .setTag("boolean", true)
            .setTag("number", 13)
            .finish();

        List<MockSpan> finishedSpans = mockTracer.finishedSpans();
        assertEquals(1, finishedSpans.size());

        Map<String, Object> tags = mockTracer.finishedSpans().get(0).tags();
        assertEquals("string", tags.get("string"));
        assertEquals(true, tags.get("boolean"));
        assertEquals(13, tags.get("number"));
    }
}
